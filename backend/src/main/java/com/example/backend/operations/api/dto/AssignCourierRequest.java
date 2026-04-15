package com.example.backend.operations.api.dto;

import com.example.backend.operations.domain.model.CourierType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignCourierRequest(
        @NotNull UUID userId,
        @NotNull CourierType courierType,
        UUID zoneId,
        UUID sourceHubId,
        UUID targetHubId
) {}