package com.example.backend.tracking.application.listener;

import com.example.backend.operations.application.event.ShipmentScannedEvent;
import com.example.backend.operations.domain.model.TaskCompletionStatus;
import com.example.backend.tracking.application.TrackingService;
import com.example.backend.tracking.domain.model.TrackingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentScannedTrackingListener {

    private final TrackingService trackingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShipmentScanned(ShipmentScannedEvent event) {
        log.info("Recording tracking event for: {}", event.trackingNumber());

        trackingService.recordEvent(
                event.shipmentId(),
                event.trackingNumber(),
                mapToTrackingStatus(event.completionStatus()),
                event.description(),
                event.location()
        );
    }

    private TrackingStatus mapToTrackingStatus(TaskCompletionStatus status) {
        return switch (status) {
            case PICKED_UP -> TrackingStatus.PICKED_UP;
            case IN_HUB -> TrackingStatus.IN_HUB;
            case IN_TRANSIT -> TrackingStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> TrackingStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> TrackingStatus.DELIVERED;
            case CANCELLED -> TrackingStatus.CANCELLED;
        };
    }
}