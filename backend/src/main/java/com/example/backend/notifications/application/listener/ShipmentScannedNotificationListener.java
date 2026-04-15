package com.example.backend.notifications.application.listener;

import com.example.backend.operations.application.event.ShipmentScannedEvent;
import com.example.backend.notifications.application.NotificationService;
import com.example.backend.notifications.domain.model.NotificationStatus;
import com.example.backend.operations.domain.model.TaskCompletionStatus;
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
public class ShipmentScannedNotificationListener {

    private final NotificationService notificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onShipmentScanned(ShipmentScannedEvent event) {
        log.info("Sending status notification for: {}", event.trackingNumber());

        NotificationStatus status = mapToNotificationStatus(event.completionStatus());

        notificationService.notifyStatusChanged(
                event.receiverEmail(),
                event.trackingNumber(),
                status,
                event.description()
        );
    }

    private NotificationStatus mapToNotificationStatus(TaskCompletionStatus status) {
        return switch (status) {
            case PICKED_UP -> NotificationStatus.PICKED_UP;
            case IN_HUB -> NotificationStatus.IN_HUB;
            case IN_TRANSIT -> NotificationStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> NotificationStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> NotificationStatus.DELIVERED;
            case CANCELLED -> NotificationStatus.CANCELLED;
        };
    }
}