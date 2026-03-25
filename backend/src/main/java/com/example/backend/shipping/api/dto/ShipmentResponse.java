package com.example.backend.shipping.api.dto;

import com.example.backend.shipping.domain.model.ShipmentSize;
import com.example.backend.shipping.domain.model.ShipmentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class ShipmentResponse {

    private UUID id;
    private String trackingNumber;
    private UUID senderId;
    private AddressDto senderAddress;
    private AddressDto receiverAddress;
    private ShipmentStatus status;
    private ShipmentSize size;
    private BigDecimal price;
    private String currency;
    private String labelUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
}