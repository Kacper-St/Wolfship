package com.example.backend.tracking.api.dto;

import com.example.backend.tracking.domain.model.TrackingStatus;

import java.time.Instant;
import java.util.UUID;

public record TrackingEventResponse(
        UUID id,
        UUID shipmentId,
        String trackingNumber,
        TrackingStatus status,
        String description,
        String location,
        Instant createdAt
) {}