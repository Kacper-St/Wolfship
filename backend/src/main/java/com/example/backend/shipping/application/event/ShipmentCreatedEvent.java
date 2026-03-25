package com.example.backend.shipping.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ShipmentCreatedEvent {
    private final UUID id;
    private final String trackingNumber;
    private final UUID senderId;
    private final String receiverEmail;
    private final double receiverLat;
    private final double receiverLon;
    private final String labelUrl;
}
