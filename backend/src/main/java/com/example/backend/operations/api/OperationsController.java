package com.example.backend.operations.api;

import com.example.backend.operations.api.dto.AssignCourierRequest;
import com.example.backend.operations.api.dto.CourierResponse;
import com.example.backend.operations.api.dto.ScanRequest;
import com.example.backend.operations.api.dto.TaskResponse;
import com.example.backend.operations.application.CourierService;
import com.example.backend.operations.application.OperationsService;
import com.example.backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/operations")
@RequiredArgsConstructor
public class OperationsController {

    private final OperationsService operationsService;
    private final CourierService courierService;

    @PostMapping("/scan")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<Void> scan(@Valid @RequestBody ScanRequest request,
                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        operationsService.scanShipment(request, userDetails.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('COURIER')")
    public ResponseEntity<List<TaskResponse>> getMyTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(operationsService.getMyCourierTasks(userDetails.getId()));
    }

    @GetMapping("/unassigned-tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TaskResponse>> getUnassignedTasks() {
        return ResponseEntity.ok(operationsService.getUnassignedTasks());
    }

    @PostMapping("/couriers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierResponse> assignCourier(@Valid @RequestBody AssignCourierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courierService.assignCourier(request));
    }

    @GetMapping("/couriers/{courierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CourierResponse> getCourier(@PathVariable UUID courierId) {
        return ResponseEntity.ok(courierService.getCourierById(courierId));
    }
}