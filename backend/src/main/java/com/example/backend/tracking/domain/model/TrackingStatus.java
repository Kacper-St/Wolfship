package com.example.backend.tracking.domain.model;

public enum TrackingStatus {
    CREATED,
    PICKED_UP,
    IN_HUB,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}