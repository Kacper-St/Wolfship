package com.example.backend.operations.application;

import com.example.backend.operations.api.dto.TaskResponse;
import com.example.backend.operations.api.dto.ScanRequest;

import java.util.List;
import java.util.UUID;

public interface OperationsService {
    void scanShipment(ScanRequest request, UUID courierId);
    List<TaskResponse> getMyCourierTasks(UUID courierId);
    List<TaskResponse> getUnassignedTasks();
}