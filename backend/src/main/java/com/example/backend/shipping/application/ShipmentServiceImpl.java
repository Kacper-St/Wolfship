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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
    private final ShipmentPersistenceService shipmentPersistenceService;

    @Override
    public ShipmentResponse createShipment(ShipmentRequest request, UUID senderId) {
        log.info("Creating shipment for sender: {}", senderId);

        Shipment shipment = buildShipment(request, senderId);
        geocodeAddresses(shipment);

        String labelUrl = labelService.generateAndUploadLabel(shipment);
        shipment.setLabelUrl(labelUrl);

        Shipment saved = shipmentPersistenceService.save(shipment);

        publishShipmentCreatedEvent(saved);

        log.info("Shipment created successfully: {}", saved.getTrackingNumber());
        return shipmentMapper.toResponse(saved);
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
    public Page<ShipmentResponse> getMyShipments(UUID senderId, Pageable pageable) {
        log.info("Getting shipments for sender: {}, page: {}", senderId, pageable.getPageNumber());

        Page<Shipment> shipments = shipmentRepository.findAllBySenderId(senderId, pageable);

        return shipments.map(shipmentMapper::toResponse);
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

    private Shipment buildShipment(ShipmentRequest request, UUID senderId) {
        Shipment shipment = shipmentMapper.toEntity(request);
        shipment.setSenderId(senderId);
        shipment.setTrackingNumber(trackingNumberGenerator.generate());
        return shipment;
    }

    private void geocodeAddresses(Shipment shipment) {
        Address sender = shipment.getSenderAddress();
        Address receiver = shipment.getReceiverAddress();

        CompletableFuture<Point> senderFuture = CompletableFuture.supplyAsync(
                () -> geocodingService.geocode(
                        sender.getStreet(),
                        sender.getHouseNumber(),
                        sender.getCity(),
                        sender.getZipCode(),
                        sender.getCountry()
                ));

        CompletableFuture<Point> receiverFuture = CompletableFuture.supplyAsync(
                () -> geocodingService.geocode(
                        receiver.getStreet(),
                        receiver.getHouseNumber(),
                        receiver.getCity(),
                        receiver.getZipCode(),
                        receiver.getCountry()
                ));

        sender.setCoordinates(senderFuture.join());
        receiver.setCoordinates(receiverFuture.join());
    }

    private void publishShipmentCreatedEvent(Shipment shipment) {
        Point senderPoint = shipment.getSenderAddress().getCoordinates();
        Point receiverPoint = shipment.getReceiverAddress().getCoordinates();

        eventPublisher.publishEvent(new ShipmentCreatedEvent(
                shipment.getId(),
                shipment.getTrackingNumber(),
                shipment.getSenderId(),
                shipment.getReceiverAddress().getEmail(),
                senderPoint.getY(),
                senderPoint.getX(),
                receiverPoint.getY(),
                receiverPoint.getX(),
                shipment.getLabelUrl()
        ));
    }
}