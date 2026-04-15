package com.example.backend.operations.application;

import com.example.backend.operations.api.dto.TaskResponse;
import com.example.backend.operations.api.dto.ScanRequest;
import com.example.backend.operations.api.mapper.OperationsMapper;
import com.example.backend.operations.application.event.ShipmentScannedEvent;
import com.example.backend.operations.domain.exception.TaskNotCompletedException;
import com.example.backend.operations.domain.exception.TaskNotFoundException;
import com.example.backend.operations.domain.exception.UnauthorizedScanException;
import com.example.backend.operations.domain.model.Courier;
import com.example.backend.operations.domain.model.Task;
import com.example.backend.operations.domain.model.TaskCompletionStatus;
import com.example.backend.operations.domain.model.TaskStatus;
import com.example.backend.operations.domain.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationsServiceImpl implements OperationsService {

    private final TaskRepository taskRepository;
    private final CourierService courierService;
    private final OperationsMapper operationsMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public void scanShipment(ScanRequest request, UUID userId) {
        log.info("Courier scanning shipment: {}", request.trackingNumber());

        Courier courier = courierService.getCourierByUserId(userId);

        Task task = taskRepository
                .findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(request.shipmentId(), TaskStatus.PENDING)
                .orElseThrow(() -> new TaskNotFoundException("No pending task found for shipment: "
                        + request.trackingNumber()));

        validatePreviousTaskCompleted(task, request.trackingNumber());

        if (task.getCourier() == null || !task.getCourier().getId().equals(courier.getId())) {
            log.warn("Courier {} tried to scan shipment {} but is not authorized", courier.getId(), request.trackingNumber());
            throw new UnauthorizedScanException(request.trackingNumber());
        }

        task.setTaskStatus(TaskStatus.COMPLETED);
        taskRepository.save(task);

        TaskCompletionStatus completionStatus = resolveCompletionStatus(task);

        eventPublisher.publishEvent(new ShipmentScannedEvent(
                task.getShipmentId(),
                task.getTrackingNumber(),
                completionStatus,
                courier.getId(),
                resolveLocation(task),
                resolveDescription(task, completionStatus),
                task.getReceiverEmail()
        ));

        log.info("Shipment {} scanned successfully.", request.trackingNumber());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyCourierTasks(UUID userId) {
        log.info("Getting tasks for courier with userId: {}", userId);

        Courier courier = courierService.getCourierByUserId(userId);

        return taskRepository.findActiveByCourierId(courier.getId())
                .stream()
                .map(operationsMapper::toTaskResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getUnassignedTasks() {
        log.info("Getting unassigned tasks");

        return taskRepository.findAllByCourierIsNullAndTaskStatus(TaskStatus.PENDING)
                .stream()
                .map(operationsMapper::toTaskResponse)
                .toList();
    }

    private TaskCompletionStatus resolveCompletionStatus(Task task) {
        return switch (task.getTaskType()) {
            case PICKUP -> TaskCompletionStatus.PICKED_UP;
            case HUB_DROPOFF -> TaskCompletionStatus.IN_HUB;
            case LINE_HAUL -> TaskCompletionStatus.IN_TRANSIT;
            case HUB_PICKUP -> TaskCompletionStatus.IN_HUB;
            case DELIVERY -> TaskCompletionStatus.DELIVERED;
        };
    }

    private String resolveLocation(Task task) {
        return switch (task.getTaskType()) {
            case PICKUP -> "Strefa nadawcy";
            case HUB_DROPOFF, HUB_PICKUP -> "Hub " + task.getTargetHubId();
            case LINE_HAUL -> "Trasa " + task.getSourceHubId() + " → " + task.getTargetHubId();
            case DELIVERY -> "Strefa odbiorcy";
        };
    }

    private String resolveDescription(Task task, TaskCompletionStatus status) {
        return switch (status) {
            case PICKED_UP -> "Paczka odebrana przez kuriera";
            case IN_HUB -> "Paczka w centrum logistycznym";
            case IN_TRANSIT -> "Paczka w transporcie między hubami";
            case OUT_FOR_DELIVERY -> "Kurier jedzie do odbiorcy";
            case DELIVERED -> "Paczka doręczona";
            case CANCELLED -> "Paczka anulowana";
        };
    }

    private void validatePreviousTaskCompleted(Task currentTask, String trackingNumber) {
        if (currentTask.getSequenceOrder() == 1) {
            return;
        }

        Task previousTask = taskRepository
                .findByShipmentIdAndSequenceOrder(currentTask.getShipmentId(),
                        currentTask.getSequenceOrder() - 1)
                .orElseThrow(() -> new TaskNotFoundException("Previous task not found for shipment: " + trackingNumber));

        if (previousTask.getTaskStatus() != TaskStatus.COMPLETED) {
            log.warn("Cannot scan shipment {} — previous task {} is still {}", trackingNumber, previousTask.getTaskType(),
                    previousTask.getTaskStatus());

            throw new TaskNotCompletedException(trackingNumber, previousTask.getTaskType().name());
        }
    }
}