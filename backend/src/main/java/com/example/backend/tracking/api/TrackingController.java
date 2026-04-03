package com.example.backend.tracking.api;

import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.tracking.application.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @GetMapping("/{trackingNumber}/history")
    public ResponseEntity<List<TrackingEventResponse>> getHistory(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getHistory(trackingNumber));
    }
}