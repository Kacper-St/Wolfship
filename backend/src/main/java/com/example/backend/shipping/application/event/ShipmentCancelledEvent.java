package com.example.backend.shipping.application.event;

import java.util.UUID;

public record ShipmentCancelledEvent(
        UUID shipmentId,
        String trackingNumber,
        String receiverEmail
) {}