package com.example.backend.routing.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "shipment_routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID shipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_zone_id", nullable = false)
    private Zone sourceZone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_zone_id", nullable = false)
    private Zone targetZone;

    @ElementCollection
    @CollectionTable(
            name = "shipment_route_steps",
            joinColumns = @JoinColumn(name = "shipment_route_id")
    )
    @OrderColumn(name = "step_index")
    @Column(name = "hub_id", nullable = false)
    @Builder.Default
    private List<UUID> hubSequence = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RouteStatus status = RouteStatus.PLANNED;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;
}