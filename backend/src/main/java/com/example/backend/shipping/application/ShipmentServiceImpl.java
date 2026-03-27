package com.example.backend.shipping.application;

import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.api.mapper.ShipmentMapper;
import com.example.backend.shipping.application.event.ShipmentCancelledEvent;
import com.example.backend.shipping.application.event.ShipmentCreatedEvent;
import com.example.backend.shipping.domain.exception.LabelGenerationException;
import com.example.backend.shipping.domain.exception.ShipmentCannotBeCancelledException;
import com.example.backend.shipping.domain.exception.ShipmentNotFoundException;
import com.example.backend.shipping.domain.model.Address;
import com.example.backend.shipping.domain.model.Shipment;
import com.example.backend.shipping.domain.model.ShipmentStatus;
import com.example.backend.shipping.domain.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final GeocodingService geocodingService;
    private final LabelService labelService;
    private final TrackingNumberGenerator trackingNumberGenerator;
    private final ShipmentMapper shipmentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ShipmentResponse createShipment(ShipmentRequest request, UUID senderId) {
        log.info("Creating shipment for sender: {}", senderId);

        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setSenderId(senderId);
        shipment.setTrackingNumber(trackingNumberGenerator.generate());

        Address senderAddress = shipment.getSenderAddress();
        Point senderPoint = geocodingService.geocode(
                senderAddress.getStreet(),
                senderAddress.getHouseNumber(),
                senderAddress.getCity(),
                senderAddress.getZipCode(),
                senderAddress.getCountry()
        );
        senderAddress.setCoordinates(senderPoint);

        Address receiverAddress = shipment.getReceiverAddress();
        Point receiverPoint = geocodingService.geocode(
                receiverAddress.getStreet(),
                receiverAddress.getHouseNumber(),
                receiverAddress.getCity(),
                receiverAddress.getZipCode(),
                receiverAddress.getCountry()
        );
        receiverAddress.setCoordinates(receiverPoint);

        Shipment saved = shipmentRepository.saveAndFlush(shipment);

        String labelUrl = labelService.generateAndUploadLabel(saved);
        saved.setLabelUrl(labelUrl);

        Shipment updated = shipmentRepository.save(saved);

        eventPublisher.publishEvent(new ShipmentCreatedEvent(
                updated.getId(),
                updated.getTrackingNumber(),
                updated.getSenderId(),
                updated.getReceiverAddress().getEmail(),
                receiverPoint.getY(),
                receiverPoint.getX(),
                senderPoint.getY(),
                senderPoint.getX(),
                updated.getLabelUrl()
        ));

        log.info("Shipment created successfully: {}", updated.getTrackingNumber());
        return shipmentMapper.toResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByTrackingNumber(String trackingNumber) {
        log.info("Getting shipment by tracking number: {}", trackingNumber);

        Shipment shipment = shipmentRepository
                .findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "Shipment not found: " + trackingNumber));

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShipmentResponse> getMyShipments(UUID senderId) {
        log.info("Getting shipments for sender: {}", senderId);

        return shipmentRepository.findAllBySenderId(senderId)
                .stream()
                .map(shipmentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelShipment(String trackingNumber, UUID requesterId) {
        log.info("Cancelling shipment: {} by: {}", trackingNumber, requesterId);

        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "Shipment not found: " + trackingNumber));

        if (shipment.getStatus() != ShipmentStatus.CREATED) {
            throw new ShipmentCannotBeCancelledException(
                    shipment.getTrackingNumber(),
                    shipment.getStatus().name()
            );
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipmentRepository.save(shipment);

        eventPublisher.publishEvent(new ShipmentCancelledEvent(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getReceiverAddress().getEmail()
        ));

        log.info("Shipment {} cancelled successfully", trackingNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream getLabelStream(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ShipmentNotFoundException(
                        "Shipment not found: " + trackingNumber));
        try {
            return labelService.getLabelStream(shipment.getLabelUrl());
        } catch (Exception e) {
            log.error("Failed to get label stream for: {}", trackingNumber, e);
            throw new LabelGenerationException(trackingNumber);
        }
    }
}