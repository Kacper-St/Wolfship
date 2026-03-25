package com.example.backend.shipping.application.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ShipmentCancelledEvent {
    private final UUID shipmentId;
    private final String trackingNumber;
    private final String receiverEmail;
}