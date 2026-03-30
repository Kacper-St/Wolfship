package com.example.backend.routing.domain.repository;

import com.example.backend.routing.domain.model.ShipmentRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRouteRepository extends JpaRepository<ShipmentRoute, UUID> {

    @Query("SELECT sr FROM ShipmentRoute sr " +
            "JOIN FETCH sr.sourceZone sz " +
            "JOIN FETCH sz.hub " +
            "JOIN FETCH sr.targetZone tz " +
            "JOIN FETCH tz.hub " +
            "JOIN FETCH sr.hubSequence " +
            "WHERE sr.shipmentId = :shipmentId")
    Optional<ShipmentRoute> findByShipmentId(UUID shipmentId);
}