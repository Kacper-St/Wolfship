package com.example.backend.routing.application;

import com.example.backend.routing.domain.exception.ZoneNotFoundException;
import com.example.backend.routing.domain.model.Zone;
import com.example.backend.routing.domain.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoneResolverServiceImpl implements ZoneResolverService {

    private final ZoneRepository zoneRepository;

    @Override
    @Transactional(readOnly = true)
    public Zone resolveZone(double lat, double lon) {
        log.info("Resolving zone for coordinates: lat={}, lon={}", lat, lon);

        Zone zone = zoneRepository.findZoneByCoordinates(lon, lat)
                .orElseThrow(() -> {
                    log.warn("No zone found for coordinates: lat={}, lon={}", lat, lon);
                    return new ZoneNotFoundException("No zone found for coordinates: lat=" + lat + ", lon=" + lon);
                });

        log.info("Resolved zone: {} ({}), hub: {}", zone.getName(), zone.getTerytCode(), zone.getHub().getName());

        return zone;
    }
}