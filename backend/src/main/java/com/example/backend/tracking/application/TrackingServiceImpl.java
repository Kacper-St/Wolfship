package com.example.backend.tracking.application;

import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.tracking.api.mapper.TrackingMapper;
import com.example.backend.tracking.domain.exception.TrackingEventNotFoundException;
import com.example.backend.tracking.domain.model.TrackingEvent;
import com.example.backend.tracking.domain.model.TrackingStatus;
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
    public void recordEvent(TrackingEventCommand command) {

        log.info("Recording tracking event for shipment: {} status: {}", command.trackingNumber(), command.status());

        TrackingEvent event = TrackingEvent.builder()
                .shipmentId(command.shipmentId())
                .trackingNumber(command.trackingNumber())
                .status(command.status())
                .description(command.description())
                .location(command.location())
                .build();

        trackingEventRepository.save(event);

        log.info("Tracking event recorded successfully for: {}", command.trackingNumber());
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