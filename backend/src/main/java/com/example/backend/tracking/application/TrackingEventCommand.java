package com.example.backend.tracking.application;

import com.example.backend.tracking.domain.model.TrackingStatus;
import lombok.Builder;

import java.util.UUID;

@Builder
public record TrackingEventCommand(
        UUID shipmentId,
        String trackingNumber,
        TrackingStatus status,
        String description,
        String location
) {
    public TrackingEventCommand {
        if (shipmentId == null) {
            throw new IllegalArgumentException("Shipment id is required");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("Tracking number is required");
        }
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}