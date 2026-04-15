package com.example.backend.operations.application.listener;

import com.example.backend.operations.application.CourierService;
import com.example.backend.routing.application.event.RouteCalculatedEvent;
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
public class RouteCalculatedEventListener {

    private final CourierService courierService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onRouteCalculated(RouteCalculatedEvent event) {
        log.info("Received RouteCalculatedEvent for shipment: {}", event.trackingNumber());

        try {
            courierService.createTasksForRoute(event);
            log.info("Tasks created successfully for shipment: {}", event.trackingNumber());
        } catch (Exception e) {
            log.error("Failed to create tasks for shipment: {}. Error: {}", event.trackingNumber(), e.getMessage(), e);
        }
    }
}