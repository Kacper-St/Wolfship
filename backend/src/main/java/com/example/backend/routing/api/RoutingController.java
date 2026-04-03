package com.example.backend.routing.api;

import com.example.backend.routing.api.dto.RouteResponse;
import com.example.backend.routing.application.RoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/routing")
@RequiredArgsConstructor
public class RoutingController {

    private final RoutingService routingService;

    @GetMapping("/shipments/{shipmentId}/route")
    public ResponseEntity<RouteResponse> getRoute(@PathVariable UUID shipmentId) {
        return ResponseEntity.ok(routingService.getRouteByShipmentId(shipmentId));
    }
}