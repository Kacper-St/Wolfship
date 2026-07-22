package com.example.backend.shipping.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Money(

        @Column(name = "price", nullable = false)
        BigDecimal amount,

        @Column(name = "currency", length = 3, nullable = false)
        String currency
) {
    private static final String DEFAULT_CURRENCY = "PLN";

    public Money {
        if (amount == null) {
            throw new IllegalArgumentException("Amount is required");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be positive, was: " + amount);
        }
        if (currency == null || currency.length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter code, was: " + currency);
        }
        currency = currency.toUpperCase();
    }

    public static Money ofPln(BigDecimal amount) {
        return new Money(amount, DEFAULT_CURRENCY);
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.amount.multiply(factor), this.currency);
    }

    private void requireSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Currency mismatch: " + this.currency + " vs " + other.currency);
        }
    }
}