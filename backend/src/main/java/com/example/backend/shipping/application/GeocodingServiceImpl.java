package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.exception.InvalidAddressException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeocodingServiceImpl implements GeocodingService {

    private final WebClient nominatimWebClient;

    private final GeometryFactory geometryFactory = 
            new GeometryFactory(new PrecisionModel(), 4326);

    @Override
    public Point geocode(String street, String houseNumber,
                         String city, String zipCode, String country) {

        String query = String.format("%s %s, %s, %s, %s",
                street, houseNumber, zipCode, city, country);

        log.info("Geocoding address: {}", query);

        List<Map> results = nominatimWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();

        if (results == null || results.isEmpty()) {
            log.warn("Geocoding failed for address: {}", query);
            throw new InvalidAddressException(
                    "Cannot find coordinates for address: " + query);
        }

        Map result = results.get(0);
        double lat = Double.parseDouble((String) result.get("lat"));
        double lon = Double.parseDouble((String) result.get("lon"));

        log.info("Geocoding successful: lat={}, lon={}", lat, lon);

        return geometryFactory.createPoint(new Coordinate(lon, lat));
    }
}