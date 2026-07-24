package com.example.backend.tracking.application;

import com.example.backend.tracking.api.dto.TrackingEventResponse;

import java.util.List;

public interface TrackingService {
    void recordEvent(TrackingEventCommand command);
    List<TrackingEventResponse> getHistory(String trackingNumber);
}