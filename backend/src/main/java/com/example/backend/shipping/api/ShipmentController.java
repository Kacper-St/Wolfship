package com.example.backend.shipping.api;

import com.example.backend.security.CustomUserDetails;
import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.application.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody ShipmentRequest request,
                                                           @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID senderId = userDetails.getId();
        ShipmentResponse response = shipmentService.createShipment(request, senderId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> trackShipment(@PathVariable String trackingNumber) {

        ShipmentResponse response = shipmentService.getShipmentByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping()
    public ResponseEntity<List<ShipmentResponse>> getMyShipments(@AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID senderId = userDetails.getId();
        List<ShipmentResponse> response = shipmentService.getMyShipments(senderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{trackingNumber}")
    public ResponseEntity<Void> cancelShipment(@PathVariable String trackingNumber,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {

        UUID requesterId = userDetails.getId();
        shipmentService.cancelShipment(trackingNumber, requesterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{trackingNumber}/label")
    public ResponseEntity<InputStreamResource> getLabel(@PathVariable String trackingNumber) {

        InputStream stream = shipmentService.getLabelStream(trackingNumber);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + trackingNumber + ".pdf\"")
                .body(new InputStreamResource(stream));
    }
}