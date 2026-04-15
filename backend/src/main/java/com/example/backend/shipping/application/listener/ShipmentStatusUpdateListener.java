package com.example.backend.shipping.application.listener;

import com.example.backend.operations.application.event.ShipmentScannedEvent;
import com.example.backend.operations.domain.model.TaskCompletionStatus;
import com.example.backend.shipping.domain.exception.ShipmentNotFoundException;
import com.example.backend.shipping.domain.model.Shipment;
import com.example.backend.shipping.domain.model.ShipmentStatus;
import com.example.backend.shipping.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentStatusUpdateListener {

    private final ShipmentRepository shipmentRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShipmentScanned(ShipmentScannedEvent event) {
        log.info("Updating shipment status: {} → {}", event.trackingNumber(), event.completionStatus());

        Shipment shipment = shipmentRepository
                .findByTrackingNumber(event.trackingNumber())
                .orElseThrow(() -> new ShipmentNotFoundException("Shipment not found: " + event.trackingNumber()));

        ShipmentStatus newStatus = mapToShipmentStatus(event.completionStatus());
        shipment.setStatus(newStatus);

        switch (newStatus) {
            case PICKED_UP -> shipment.setPickedUpAt(Instant.now());
            case DELIVERED -> shipment.setDeliveredAt(Instant.now());
            case CANCELLED -> shipment.setCancelledAt(Instant.now());
            default -> {}
        }

        shipmentRepository.save(shipment);
        log.info("Shipment {} status updated to: {}", event.trackingNumber(), newStatus);
    }

    private ShipmentStatus mapToShipmentStatus(TaskCompletionStatus status) {
        return switch (status) {
            case PICKED_UP -> ShipmentStatus.PICKED_UP;
            case IN_HUB -> ShipmentStatus.IN_HUB;
            case IN_TRANSIT -> ShipmentStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> ShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> ShipmentStatus.DELIVERED;
            case CANCELLED -> ShipmentStatus.CANCELLED;
        };
    }
}