package com.example.backend.routing.domain.repository;

import com.example.backend.routing.domain.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, UUID> {

    @Query(value = """
            SELECT z.* FROM zones z
            WHERE ST_Contains(z.boundary,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326))
            LIMIT 1
            """, nativeQuery = true)
    Optional<Zone> findZoneByCoordinates(double lon, double lat);
}