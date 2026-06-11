package com.example.backend.routing.integration;

import com.example.backend.integration.BaseIntegrationTest;
import com.example.backend.routing.domain.model.Zone;
import com.example.backend.routing.domain.repository.ZoneRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ZoneRepository — PostGIS integration")
class ZoneRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ZoneRepository zoneRepository;

    @Test
    @DisplayName("should resolve zone for coordinates within Poland")
    void shouldResolveZoneForWarsawCoordinates() {
        Optional<Zone> zone = zoneRepository.findZoneByCoordinates(21.0122, 52.2297);

        assertThat(zone).isPresent();
        assertThat(zone.get().getHub()).isNotNull();
        assertThat(zone.get().getName()).isNotBlank();
    }

    @Test
    @DisplayName("should resolve zone for Kraków coordinates")
    void shouldResolveZoneForKrakowCoordinates() {
        Optional<Zone> zone = zoneRepository.findZoneByCoordinates(19.9366, 50.0614);

        assertThat(zone).isPresent();
        assertThat(zone.get().getHub()).isNotNull();
    }

    @Test
    @DisplayName("should return empty for coordinates outside any zone")
    void shouldReturnEmptyForOceanCoordinates() {
        Optional<Zone> zone = zoneRepository.findZoneByCoordinates(-30.0, 0.0);

        assertThat(zone).isEmpty();
    }
}