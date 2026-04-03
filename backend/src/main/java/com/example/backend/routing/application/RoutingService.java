package com.example.backend.routing.application;

import com.example.backend.routing.api.dto.RouteResponse;
import com.example.backend.routing.domain.model.ShipmentRoute;

import java.util.UUID;

public interface RoutingService {
    ShipmentRoute calculateAndSaveRoute(UUID shipmentId, double senderLat, double senderLon,
                                        double receiverLat, double receiverLon);
    RouteResponse getRouteByShipmentId(UUID shipmentId);
}