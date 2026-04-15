package com.example.backend.operations.api.dto;

import com.example.backend.operations.domain.model.CourierType;

import java.util.UUID;

public record CourierResponse(
        UUID id,
        UUID userId,
        CourierType courierType,
        UUID zoneId,
        UUID sourceHubId,
        UUID targetHubId,
        boolean active
) {}