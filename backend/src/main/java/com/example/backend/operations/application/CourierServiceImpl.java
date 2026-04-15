package com.example.backend.operations.application;

import com.example.backend.operations.api.dto.AssignCourierRequest;
import com.example.backend.operations.api.dto.CourierResponse;
import com.example.backend.operations.api.mapper.OperationsMapper;
import com.example.backend.operations.domain.exception.CourierNotFoundException;
import com.example.backend.operations.domain.model.*;
import com.example.backend.operations.domain.repository.CourierRepository;
import com.example.backend.operations.domain.repository.TaskRepository;
import com.example.backend.routing.application.event.RouteCalculatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourierServiceImpl implements CourierService {

    private final CourierRepository courierRepository;
    private final TaskRepository taskRepository;
    private final OperationsMapper operationsMapper;

    @Override
    @Transactional
    public void createTasksForRoute(RouteCalculatedEvent event) {
        log.info("Creating tasks for shipment: {}", event.trackingNumber());

        List<Task> tasks = new ArrayList<>();
        int sequence = 1;

        Courier zoneCourierSource = findZoneCourier(event.sourceZoneId());

        tasks.add(Task.builder()
                .shipmentId(event.shipmentId())
                .trackingNumber(event.trackingNumber())
                .courier(zoneCourierSource)
                .taskType(TaskType.PICKUP)
                .taskStatus(TaskStatus.PENDING)
                .receiverEmail(event.receiverEmail())
                .sequenceOrder(sequence++)
                .build());

        tasks.add(Task.builder()
                .shipmentId(event.shipmentId())
                .trackingNumber(event.trackingNumber())
                .courier(zoneCourierSource)
                .taskType(TaskType.HUB_DROPOFF)
                .taskStatus(TaskStatus.PENDING)
                .targetHubId(event.sourceHubId())
                .receiverEmail(event.receiverEmail())
                .sequenceOrder(sequence++)
                .build());

        List<UUID> hubs = event.hubSequence();
        for (int i = 0; i < hubs.size() - 1; i++) {
            UUID sourceHub = hubs.get(i);
            UUID targetHub = hubs.get(i + 1);

            Courier lineHaulCourier = findLineHaulCourier(sourceHub, targetHub);

            tasks.add(Task.builder()
                    .shipmentId(event.shipmentId())
                    .trackingNumber(event.trackingNumber())
                    .courier(lineHaulCourier)
                    .taskType(TaskType.LINE_HAUL)
                    .taskStatus(TaskStatus.PENDING)
                    .sourceHubId(sourceHub)
                    .targetHubId(targetHub)
                    .receiverEmail(event.receiverEmail())
                    .sequenceOrder(sequence++)
                    .build());
        }

        Courier zoneCourierTarget = findZoneCourier(event.targetZoneId());

        tasks.add(Task.builder()
                .shipmentId(event.shipmentId())
                .trackingNumber(event.trackingNumber())
                .courier(zoneCourierTarget)
                .taskType(TaskType.HUB_PICKUP)
                .taskStatus(TaskStatus.PENDING)
                .sourceHubId(event.targetHubId())
                .receiverEmail(event.receiverEmail())
                .sequenceOrder(sequence++)
                .build());

        tasks.add(Task.builder()
                .shipmentId(event.shipmentId())
                .trackingNumber(event.trackingNumber())
                .courier(zoneCourierTarget)
                .taskType(TaskType.DELIVERY)
                .taskStatus(TaskStatus.PENDING)
                .receiverEmail(event.receiverEmail())
                .sequenceOrder(sequence)
                .build());

        taskRepository.saveAll(tasks);
        log.info("Created {} tasks for shipment: {}", tasks.size(), event.trackingNumber());
    }

    @Override
    @Transactional
    public CourierResponse assignCourier(AssignCourierRequest request) {
        log.info("Assigning courier profile for userId: {}", request.userId());

        if (request.courierType() == CourierType.ZONE_COURIER && request.zoneId() == null) {
            throw new IllegalArgumentException("zoneId is required for ZONE_COURIER");
        }

        if (request.courierType() == CourierType.LINE_HAUL_COURIER && (request.sourceHubId() == null || request.targetHubId() == null)) {
            throw new IllegalArgumentException("sourceHubId and targetHubId are required for LINE_HAUL_COURIER");
        }

        Courier courier = Courier.builder()
                .userId(request.userId())
                .courierType(request.courierType())
                .zoneId(request.zoneId())
                .sourceHubId(request.sourceHubId())
                .targetHubId(request.targetHubId())
                .active(true)
                .build();

        Courier saved = courierRepository.save(courier);
        log.info("Courier profile created for userId: {}", request.userId());

        return operationsMapper.toCourierResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Courier getCourierByUserId(UUID userId) {
        return courierRepository.findByUserId(userId)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found for user: " + userId));
    }

    @Override
    @Transactional(readOnly = true)
    public CourierResponse getCourierById(UUID courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new CourierNotFoundException("Courier not found: " + courierId));
        return operationsMapper.toCourierResponse(courier);
    }

    private Courier findZoneCourier(UUID zoneId) {
        return courierRepository
                .findByZoneIdAndActiveTrueOrderByWorkload(zoneId)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    log.warn("No active courier found for zone: {}", zoneId);
                    return null;
                });
    }

    private Courier findLineHaulCourier(UUID sourceHubId, UUID targetHubId) {
        return courierRepository.findBySourceHubIdAndTargetHubIdAndActiveTrue(sourceHubId, targetHubId)
                .orElseGet(() -> {
                    log.warn("No active line-haul courier found for route: {} → {}", sourceHubId, targetHubId);
                    return null;
                });
    }
}