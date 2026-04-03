package com.example.backend.tracking.application;

import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.shipping.domain.model.ShipmentStatus;

import java.util.List;
import java.util.UUID;

public interface TrackingService {
    void recordEvent(UUID shipmentId, String trackingNumber, ShipmentStatus status, String description, String location);
    List<TrackingEventResponse> getHistory(String trackingNumber);
}