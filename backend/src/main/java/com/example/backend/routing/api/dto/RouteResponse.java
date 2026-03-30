package com.example.backend.routing.api.dto;

import com.example.backend.routing.domain.model.RouteStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        UUID shipmentId,
        ZoneResponse sourceZone,
        ZoneResponse targetZone,
        List<UUID> hubSequence,
        RouteStatus status,
        Instant createdAt,
        Instant updatedAt
) {}