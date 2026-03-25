package com.example.backend.shipping.domain.repository;

import com.example.backend.shipping.domain.model.Shipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @EntityGraph(attributePaths = {"senderAddress", "receiverAddress"})
    Optional<Shipment> findByTrackingNumber(String trackingNumber);

    @EntityGraph(attributePaths = {"senderAddress", "receiverAddress"})
    List<Shipment> findAllBySenderId(UUID senderId);
}
