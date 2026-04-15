package com.example.backend.operations.application.event;

import com.example.backend.operations.domain.model.TaskCompletionStatus;

import java.util.UUID;

public record ShipmentScannedEvent(
        UUID shipmentId,
        String trackingNumber,
        TaskCompletionStatus completionStatus,
        UUID courierId,
        String location,
        String description,
        String receiverEmail
) {}