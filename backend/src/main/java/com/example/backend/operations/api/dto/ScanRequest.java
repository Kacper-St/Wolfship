package com.example.backend.operations.api.dto;

import java.util.UUID;

public record ScanRequest(
        UUID shipmentId,
        String trackingNumber
) {}