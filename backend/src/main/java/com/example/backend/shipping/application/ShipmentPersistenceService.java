package com.example.backend.shipping.application;

import com.example.backend.shipping.domain.model.Shipment;
import com.example.backend.shipping.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShipmentPersistenceService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public Shipment save(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }
}