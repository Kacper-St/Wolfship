package com.example.backend.routing.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hub_connections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_hub_id", nullable = false)
    private Hub sourceHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_hub_id", nullable = false)
    private Hub targetHub;

    @Column(nullable = false)
    private Double distanceKm;

    @Column(nullable = false)
    private Integer travelTimeMinutes;
}