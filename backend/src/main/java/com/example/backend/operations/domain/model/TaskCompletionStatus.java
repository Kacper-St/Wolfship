package com.example.backend.operations.domain.model;

public enum TaskCompletionStatus {
    PICKED_UP,
    IN_HUB,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED
}