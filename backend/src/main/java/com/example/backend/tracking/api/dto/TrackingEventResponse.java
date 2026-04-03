package com.example.backend.tracking.api.dto;

import com.example.backend.shipping.domain.model.ShipmentStatus;

import java.time.Instant;
import java.util.UUID;

public record TrackingEventResponse(
        UUID id,
        UUID shipmentId,
        String trackingNumber,
        ShipmentStatus status,
        String description,
        String location,
        Instant createdAt
) {}