package com.example.backend.shipping.domain.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum ShipmentSize {
    S(BigDecimal.valueOf(15.00)),
    M(BigDecimal.valueOf(20.00)),
    L(BigDecimal.valueOf(30.00)),
    XL(BigDecimal.valueOf(50.00));

    private final BigDecimal basePrice;

    ShipmentSize(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

}
