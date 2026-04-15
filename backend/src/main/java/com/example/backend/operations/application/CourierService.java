package com.example.backend.operations.application;

import com.example.backend.operations.api.dto.AssignCourierRequest;
import com.example.backend.operations.api.dto.CourierResponse;
import com.example.backend.operations.domain.model.Courier;
import com.example.backend.routing.application.event.RouteCalculatedEvent;

import java.util.UUID;

public interface CourierService {
    void createTasksForRoute(RouteCalculatedEvent event);
    Courier getCourierByUserId(UUID userId);
    CourierResponse getCourierById(UUID courierId);
    CourierResponse assignCourier(AssignCourierRequest request);
}