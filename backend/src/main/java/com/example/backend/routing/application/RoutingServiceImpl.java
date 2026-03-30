package com.example.backend.routing.application;

import com.example.backend.routing.application.graph.HubGraphService;
import com.example.backend.routing.domain.exception.RouteNotFoundException;
import com.example.backend.routing.domain.model.ShipmentRoute;
import com.example.backend.routing.domain.model.Zone;
import com.example.backend.routing.domain.repository.ShipmentRouteRepository;
import com.example.backend.routing.application.event.RouteCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutingServiceImpl implements RoutingService {

    private final ZoneResolverService zoneResolverService;
    private final HubGraphService hubGraphService;
    private final ShipmentRouteRepository shipmentRouteRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ShipmentRoute calculateAndSaveRoute(UUID shipmentId, double senderLat, double senderLon,
                                               double receiverLat, double receiverLon) {
        log.info("Calculating route for shipment: {}", shipmentId);

        Zone sourceZone = zoneResolverService.resolveZone(senderLat, senderLon);
        Zone targetZone = zoneResolverService.resolveZone(receiverLat, receiverLon);

        log.info("Source zone: {} → Hub: {}", sourceZone.getName(), sourceZone.getHub().getName());
        log.info("Target zone: {} → Hub: {}", targetZone.getName(), targetZone.getHub().getName());

        List<UUID> hubSequence = hubGraphService.findShortestPath(sourceZone.getHub().getId(), targetZone.getHub().getId()
        );

        ShipmentRoute route = ShipmentRoute.builder()
                .shipmentId(shipmentId)
                .sourceZone(sourceZone)
                .targetZone(targetZone)
                .hubSequence(hubSequence)
                .build();

        ShipmentRoute saved = shipmentRouteRepository.save(route);
        log.info("Route saved for shipment: {}. Hub sequence: {}", shipmentId, hubSequence.size());

        eventPublisher.publishEvent(new RouteCalculatedEvent(
                shipmentId,
                sourceZone.getHub().getId(),
                targetZone.getHub().getId(),
                hubSequence
        ));

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public ShipmentRoute getRouteByShipmentId(UUID shipmentId) {
        log.info("Getting route for shipment: {}", shipmentId);

        return shipmentRouteRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for shipment: " + shipmentId));
    }
}