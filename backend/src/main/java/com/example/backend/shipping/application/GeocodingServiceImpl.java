package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.exception.GeocodingException;
import com.example.backend.shipping.domain.exception.InvalidAddressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final RestClient nominatimRestClient;
    private final GeometryFactory geometryFactory;

    @Retryable(
            retryFor = {RestClientException.class, ResourceAccessException.class},
            noRetryFor = {InvalidAddressException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )

    @Cacheable(value = "geocoding", key = "#street + ':' + #houseNumber + ':' + #city + ':' + #zipCode + ':' + #country")

    @Override
    public Point geocode(String street, String houseNumber,
                         String city, String zipCode, String country) {

        String query = String.format("%s %s, %s, %s, %s",
                street, houseNumber, zipCode, city, country);

        log.info("Geocoding address: {}", query);

        List<Map<String, Object>> results = nominatimRestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (results == null || results.isEmpty()) {
            log.warn("Geocoding failed for address: {}", query);
            throw new InvalidAddressException("Cannot find coordinates for address: " + query);
        }

        Map<String, Object> result = results.get(0);
        double lat = Double.parseDouble(result.get("lat").toString());
        double lon = Double.parseDouble(result.get("lon").toString());

        log.info("Geocoding successful: lat={}, lon={}", lat, lon);

        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }

    @Recover
    public Point recoverGeocode(RestClientException e, String street, String houseNumber,
                                String city, String zipCode, String country) {
        String query = String.format("%s %s, %s, %s, %s", street, houseNumber, zipCode, city, country);
        log.error("Geocoding failed permanently after 3 attempts for: {}. Error: {}", query, e.getMessage());
        throw new GeocodingException("Geocoding service unavailable for address: " + query);
    }
}