package com.example.backend.routing.application.listener;

import com.example.backend.routing.application.RoutingService;
import com.example.backend.shipping.application.event.ShipmentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShipmentCreatedEventListener {

    private final RoutingService routingService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onShipmentCreated(ShipmentCreatedEvent event) {
        log.info("Processing routing for shipment: {}", event.trackingNumber());
        
        try {
            routingService.calculateAndSaveRoute(
                    event.shipmentId(),
                    event.senderLat(),
                    event.senderLon(),
                    event.receiverLat(),
                    event.receiverLon()
            );
        } catch (Exception e) {
            handleRoutingFailure(event, e);
        }
    }

    private void handleRoutingFailure(ShipmentCreatedEvent event, Exception e) {
        log.error("Permanent failure to calculate route for shipment: {}. Manual intervention required.", 
                  event.trackingNumber(), e);
    }
}