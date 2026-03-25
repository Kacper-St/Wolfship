package com.example.backend.shipping.domain.model;

public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_HUB,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}
