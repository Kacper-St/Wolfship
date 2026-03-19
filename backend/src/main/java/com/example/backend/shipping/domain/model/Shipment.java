package com.example.backend.shipping.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shipments", indexes = {
        @Index(name = "idx_tracking", columnList = "trackingNumber"),
        @Index(name = "idx_sender", columnList = "senderId"),
        @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String trackingNumber;

    @NotNull
    @Column(nullable = false)
    private UUID senderId;

    private UUID courierId;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sender_address_id")
    private Address senderAddress;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "receiver_address_id")
    private Address receiverAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShipmentSize size;

    @NotNull
    @Positive(message = "Price must be positive")
    @Column(nullable = false)
    private BigDecimal price;

    @Column(length = 3, nullable = false)
    @Builder.Default
    private String currency = "PLN";

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime pickedUpAt;

    private LocalDateTime deliveredAt;

    private LocalDateTime cancelledAt;
}
