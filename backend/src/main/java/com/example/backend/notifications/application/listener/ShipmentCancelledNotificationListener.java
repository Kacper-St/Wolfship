package com.example.backend.notifications.application.listener;

import com.example.backend.shipping.application.event.ShipmentCancelledEvent;
import com.example.backend.notifications.application.NotificationService;
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
public class ShipmentCancelledNotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShipmentCancelled(ShipmentCancelledEvent event) {
        log.info("Sending cancellation notification for: {}", event.trackingNumber());

        notificationService.notifyShipmentCancelled(
                event.receiverEmail(),
                event.trackingNumber()
        );
    }
}