package com.example.backend.shipping.application;

import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface ShipmentService {
    ShipmentResponse createShipment(ShipmentRequest request, UUID senderId);
    ShipmentResponse getShipmentByTrackingNumber(String trackingNumber);
    void cancelShipment(String trackingNumber, UUID requesterId);
    InputStream getLabelStream(String trackingNumber);
    Page<ShipmentResponse> getMyShipments(UUID senderId, Pageable pageable);
}