package com.example.backend.shipping.api.dto;

import com.example.backend.shipping.domain.model.ShipmentSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShipmentRequest {

    @NotNull(message = "Sender address is required")
    @Valid
    private AddressDto senderAddress;

    @NotNull(message = "Receiver address is required")
    @Valid
    private AddressDto receiverAddress;

    @NotNull(message = "Size is required")
    private ShipmentSize size;
}