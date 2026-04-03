package com.example.backend.tracking.domain.repository;

import com.example.backend.tracking.domain.model.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, UUID> {
    List<TrackingEvent> findAllByTrackingNumberOrderByCreatedAtDesc(String trackingNumber);
    List<TrackingEvent> findAllByShipmentIdOrderByCreatedAtDesc(UUID shipmentId);
}