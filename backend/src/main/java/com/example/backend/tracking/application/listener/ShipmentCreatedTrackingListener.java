package com.example.backend.tracking.application.listener;

import com.example.backend.shipping.application.event.ShipmentCreatedEvent;
import com.example.backend.shipping.domain.model.ShipmentStatus;
import com.example.backend.tracking.application.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentCreatedTrackingListener {

    private final TrackingService trackingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public void onShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Recording CREATED tracking event for: {}", event.trackingNumber());

        trackingService.recordEvent(
                event.shipmentId(),
                event.trackingNumber(),
                ShipmentStatus.CREATED,
                "Paczka przyjęta w systemie",
                null
        );
    }
}