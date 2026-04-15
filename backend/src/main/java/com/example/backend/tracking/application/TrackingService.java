package com.example.backend.tracking.application;

import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.tracking.domain.model.TrackingStatus;

import java.util.List;
import java.util.UUID;

public interface TrackingService {
    void recordEvent(UUID shipmentId, String trackingNumber, TrackingStatus status, String description, String location);
    List<TrackingEventResponse> getHistory(String trackingNumber);
}