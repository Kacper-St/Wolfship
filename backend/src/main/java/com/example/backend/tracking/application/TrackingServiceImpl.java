package com.example.backend.tracking.application;

import com.example.backend.shipping.domain.model.ShipmentStatus;
import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.tracking.api.mapper.TrackingMapper;
import com.example.backend.tracking.domain.exception.TrackingEventNotFoundException;
import com.example.backend.tracking.domain.model.TrackingEvent;
import com.example.backend.tracking.domain.repository.TrackingEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final TrackingEventRepository trackingEventRepository;
    private final TrackingMapper trackingMapper;

    @Override
    @Transactional
    public void recordEvent(UUID shipmentId, String trackingNumber, ShipmentStatus status, String description,
                            String location) {

        log.info("Recording tracking event for shipment: {} status: {}", trackingNumber, status);

        TrackingEvent event = TrackingEvent.builder()
                .shipmentId(shipmentId)
                .trackingNumber(trackingNumber)
                .status(status)
                .description(description)
                .location(location)
                .build();

        trackingEventRepository.save(event);

        log.info("Tracking event recorded successfully for: {}", trackingNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackingEventResponse> getHistory(String trackingNumber) {
        log.info("Getting tracking history for: {}", trackingNumber);

        List<TrackingEvent> events = trackingEventRepository.findAllByTrackingNumberOrderByCreatedAtDesc(trackingNumber);

        if (events.isEmpty()) {
            throw new TrackingEventNotFoundException(trackingNumber);
        }

        return events.stream()
                .map(trackingMapper::toResponse)
                .toList();
    }
}