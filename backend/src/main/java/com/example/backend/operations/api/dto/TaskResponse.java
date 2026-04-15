package com.example.backend.operations.api.dto;

import com.example.backend.operations.domain.model.TaskStatus;
import com.example.backend.operations.domain.model.TaskType;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        UUID shipmentId,
        String trackingNumber,
        CourierResponse courier,
        TaskType taskType,
        TaskStatus taskStatus,
        UUID sourceHubId,
        UUID targetHubId,
        Integer sequenceOrder,
        Instant createdAt,
        Instant updatedAt
) {}