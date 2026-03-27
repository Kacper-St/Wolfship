package com.example.backend.shipping.application.event;

import java.util.UUID;

public record ShipmentCreatedEvent(
        UUID shipmentId,
        String trackingNumber,
        UUID senderId,
        String receiverEmail,
        double receiverLat,
        double receiverLon,
        double senderLat,
        double senderLon,
        String labelUrl
) {}
