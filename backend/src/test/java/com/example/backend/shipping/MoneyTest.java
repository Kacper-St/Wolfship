package com.example.backend.shipping;

import com.example.backend.shipping.domain.model.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Money value object")
class MoneyTest {

    @Nested
    @DisplayName("creation")
    class Creation {

        @Test
        @DisplayName("should create money with valid amount and currency")
        void shouldCreateValidMoney() {
            Money money = new Money(BigDecimal.valueOf(20.00), "PLN");

            assertThat(money.amount()).isEqualByComparingTo("20.00");
            assertThat(money.currency()).isEqualTo("PLN");
        }

        @Test
        @DisplayName("should normalize currency to uppercase")
        void shouldNormalizeCurrency() {
            Money money = new Money(BigDecimal.TEN, "pln");

            assertThat(money.currency()).isEqualTo("PLN");
        }

        @Test
        @DisplayName("should reject negative amount")
        void shouldRejectNegativeAmount() {
            assertThatThrownBy(() -> new Money(BigDecimal.valueOf(-1), "PLN"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("positive");
        }

        @Test
        @DisplayName("should reject zero amount")
        void shouldRejectZeroAmount() {
            assertThatThrownBy(() -> new Money(BigDecimal.ZERO, "PLN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("should reject invalid currency code")
        void shouldRejectInvalidCurrency() {
            assertThatThrownBy(() -> new Money(BigDecimal.TEN, "ZLOTY"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("3-letter");
        }
    }

    @Nested
    @DisplayName("arithmetic")
    class Arithmetic {

        @Test
        @DisplayName("should add two amounts in the same currency")
        void shouldAddSameCurrency() {
            Money result = Money.ofPln(BigDecimal.valueOf(20))
                    .add(Money.ofPln(BigDecimal.valueOf(30)));

            assertThat(result.amount()).isEqualByComparingTo("50");
            assertThat(result.currency()).isEqualTo("PLN");
        }

        @Test
        @DisplayName("should reject adding different currencies")
        void shouldRejectCurrencyMismatch() {
            Money pln = Money.ofPln(BigDecimal.TEN);
            Money eur = new Money(BigDecimal.TEN, "EUR");

            assertThatThrownBy(() -> pln.add(eur))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mismatch");
        }

        @Test
        @DisplayName("should multiply amount by factor")
        void shouldMultiply() {
            Money result = Money.ofPln(BigDecimal.valueOf(20))
                    .multiply(BigDecimal.valueOf(3));

            assertThat(result.amount()).isEqualByComparingTo("60");
        }

        @Test
        @DisplayName("should treat equal values as equal (value semantics)")
        void shouldHaveValueSemantics() {
            assertThat(Money.ofPln(BigDecimal.valueOf(20)))
                    .isEqualTo(Money.ofPln(BigDecimal.valueOf(20)));
        }
    }
}