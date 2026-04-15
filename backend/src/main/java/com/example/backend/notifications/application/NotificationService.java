package com.example.backend.notifications.application;

import com.example.backend.notifications.domain.model.NotificationStatus;

public interface NotificationService {
    void notifyShipmentCreated(String to, String trackingNumber, String labelUrl);
    void notifyStatusChanged(String to, String trackingNumber, NotificationStatus status, String description);
    void notifyShipmentCancelled(String to, String trackingNumber);
    void notifyAccountCreated(String to, String tempPassword);
}