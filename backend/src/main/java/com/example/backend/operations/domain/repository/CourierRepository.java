package com.example.backend.operations.domain.repository;

import com.example.backend.operations.domain.model.Courier;
import com.example.backend.operations.domain.model.CourierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourierRepository extends JpaRepository<Courier, UUID> {
    Optional<Courier> findBySourceHubIdAndTargetHubIdAndActiveTrue(UUID sourceHubId, UUID targetHubId);
    Optional<Courier> findByUserId(UUID userId);
    boolean existsByCourierType(CourierType courierType);

    @Query("SELECT c FROM Courier c WHERE c.zoneId = :zoneId AND c.active = true " +
            "ORDER BY (SELECT COUNT(t) FROM Task t WHERE t.courier = c " +
            "AND t.taskStatus IN ('PENDING', 'IN_PROGRESS')) ASC")
    List<Courier> findByZoneIdAndActiveTrueOrderByWorkload(UUID zoneId);
}