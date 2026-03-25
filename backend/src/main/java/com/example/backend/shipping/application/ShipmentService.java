package com.example.backend.shipping.application;

import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;

import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request, UUID senderId);
    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);
    List<ShipmentResponse> getMyShipments(UUID senderId);
    void cancelShipment(UUID id, UUID requesterId);
}