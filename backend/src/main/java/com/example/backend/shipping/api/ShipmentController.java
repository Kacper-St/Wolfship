package com.example.backend.shipping.api;

import com.example.backend.security.CustomUserDetails;
import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.application.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping
    public ResponseEntity<Page<ShipmentResponse>> getMyShipments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                 @RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "20") int size,
                                                                 @RequestParam(defaultValue = "createdAt") String sortBy,
                                                                 @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(shipmentService.getMyShipments(userDetails.getId(), pageable));
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