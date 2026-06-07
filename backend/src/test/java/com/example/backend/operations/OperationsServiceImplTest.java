package com.example.backend.operations;

import com.example.backend.operations.api.dto.ScanRequest;
import com.example.backend.operations.api.mapper.OperationsMapper;
import com.example.backend.operations.application.CourierService;
import com.example.backend.operations.application.OperationsServiceImpl;
import com.example.backend.operations.application.event.ShipmentScannedEvent;
import com.example.backend.operations.domain.exception.TaskNotCompletedException;
import com.example.backend.operations.domain.exception.TaskNotFoundException;
import com.example.backend.operations.domain.exception.UnauthorizedScanException;
import com.example.backend.operations.domain.model.*;
import com.example.backend.operations.domain.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OperationsServiceImpl")
class OperationsServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private CourierService courierService;
    @Mock private OperationsMapper operationsMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    private OperationsServiceImpl operationsService;

    private final UUID userId = UUID.randomUUID();
    private final UUID courierId = UUID.randomUUID();
    private final UUID shipmentId = UUID.randomUUID();
    private final String trackingNumber = "WLF-20260506-ABC";

    @BeforeEach
    void setUp() {
        operationsService = new OperationsServiceImpl(
                taskRepository, courierService, operationsMapper, eventPublisher);
    }

    private Courier courier(UUID id) {
        return Courier.builder()
                .id(id)
                .userId(UUID.randomUUID())
                .courierType(CourierType.ZONE_COURIER)
                .active(true)
                .build();
    }

    private Task task(Courier courier, TaskType type, int sequenceOrder, TaskStatus status) {
        return Task.builder()
                .id(UUID.randomUUID())
                .shipmentId(shipmentId)
                .trackingNumber(trackingNumber)
                .courier(courier)
                .taskType(type)
                .taskStatus(status)
                .sequenceOrder(sequenceOrder)
                .receiverEmail("receiver@test.com")
                .build();
    }

    private void mockScanSetup(Task task, Courier scanningCourier) {
        when(courierService.getCourierByUserId(userId)).thenReturn(scanningCourier);
        when(taskRepository.findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(
                shipmentId, TaskStatus.PENDING)).thenReturn(Optional.of(task));

        if (task.getSequenceOrder() > 1) {
            Task previous = Task.builder()
                    .taskType(TaskType.PICKUP)
                    .taskStatus(TaskStatus.COMPLETED)
                    .sequenceOrder(task.getSequenceOrder() - 1)
                    .build();
            when(taskRepository.findByShipmentIdAndSequenceOrder(
                    shipmentId, task.getSequenceOrder() - 1))
                    .thenReturn(Optional.of(previous));
        }
    }

    @Nested
    @DisplayName("scanShipment — success")
    class ScanSuccess {

        @Test
        @DisplayName("should complete first task and publish PICKED_UP event")
        void shouldCompleteFirstTask() {
            // given
            Courier courier = courier(courierId);
            Task task = task(courier, TaskType.PICKUP, 1, TaskStatus.PENDING);
            mockScanSetup(task, courier);

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when
            operationsService.scanShipment(request, userId);

            // then
            assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.COMPLETED);
            verify(taskRepository).save(task);

            // then
            ArgumentCaptor<ShipmentScannedEvent> captor =
                    ArgumentCaptor.forClass(ShipmentScannedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());

            ShipmentScannedEvent event = captor.getValue();
            assertThat(event.shipmentId()).isEqualTo(shipmentId);
            assertThat(event.trackingNumber()).isEqualTo(trackingNumber);
            assertThat(event.completionStatus()).isEqualTo(TaskCompletionStatus.PICKED_UP);
            assertThat(event.courierId()).isEqualTo(courierId);
            assertThat(event.receiverEmail()).isEqualTo("receiver@test.com");
        }

        @Test
        @DisplayName("should complete later task when previous is completed")
        void shouldCompleteTaskWhenPreviousCompleted() {
            // given
            Courier courier = courier(courierId);
            Task task = task(courier, TaskType.LINE_HAUL, 3, TaskStatus.PENDING);
            mockScanSetup(task, courier);

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when
            operationsService.scanShipment(request, userId);

            // then
            assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.COMPLETED);

            ArgumentCaptor<ShipmentScannedEvent> captor = ArgumentCaptor.forClass(ShipmentScannedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().completionStatus()).isEqualTo(TaskCompletionStatus.IN_TRANSIT);
        }
    }

    @Nested
    @DisplayName("scanShipment — validation errors")
    class ScanErrors {

        @Test
        @DisplayName("should throw when no pending task exists")
        void shouldThrowWhenNoPendingTask() {
            // given
            Courier courier = courier(courierId);
            when(courierService.getCourierByUserId(userId)).thenReturn(courier);
            when(taskRepository.findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(
                    shipmentId, TaskStatus.PENDING)).thenReturn(Optional.empty());

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when & then
            assertThatThrownBy(() -> operationsService.scanShipment(request, userId))
                    .isInstanceOf(TaskNotFoundException.class);

            verify(taskRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("should throw when previous task not completed")
        void shouldThrowWhenPreviousNotCompleted() {
            // given
            Courier courier = courier(courierId);
            Task task = task(courier, TaskType.LINE_HAUL, 3, TaskStatus.PENDING);

            when(courierService.getCourierByUserId(userId)).thenReturn(courier);
            when(taskRepository.findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(
                    shipmentId, TaskStatus.PENDING)).thenReturn(Optional.of(task));

            Task previousPending = Task.builder()
                    .taskType(TaskType.HUB_DROPOFF)
                    .taskStatus(TaskStatus.PENDING)
                    .sequenceOrder(2)
                    .build();
            when(taskRepository.findByShipmentIdAndSequenceOrder(shipmentId, 2))
                    .thenReturn(Optional.of(previousPending));

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when & then
            assertThatThrownBy(() -> operationsService.scanShipment(request, userId))
                    .isInstanceOf(TaskNotCompletedException.class);

            verify(taskRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("should throw when courier is not authorized for the task")
        void shouldThrowWhenCourierNotAuthorized() {
            // given
            Courier scanningCourier = courier(courierId);
            Courier assignedCourier = courier(UUID.randomUUID());
            Task task = task(assignedCourier, TaskType.PICKUP, 1, TaskStatus.PENDING);

            when(courierService.getCourierByUserId(userId)).thenReturn(scanningCourier);
            when(taskRepository.findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(
                    shipmentId, TaskStatus.PENDING)).thenReturn(Optional.of(task));

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when & then
            assertThatThrownBy(() -> operationsService.scanShipment(request, userId))
                    .isInstanceOf(UnauthorizedScanException.class);

            verify(taskRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw when task has no courier assigned")
        void shouldThrowWhenTaskHasNoCourier() {
            // given
            Courier scanningCourier = courier(courierId);
            Task task = task(null, TaskType.PICKUP, 1, TaskStatus.PENDING);

            when(courierService.getCourierByUserId(userId)).thenReturn(scanningCourier);
            when(taskRepository.findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(
                    shipmentId, TaskStatus.PENDING)).thenReturn(Optional.of(task));

            ScanRequest request = new ScanRequest(shipmentId, trackingNumber);

            // when & then
            assertThatThrownBy(() -> operationsService.scanShipment(request, userId))
                    .isInstanceOf(UnauthorizedScanException.class);

            verify(taskRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("completion status resolution")
    class StatusResolution {

        private TaskCompletionStatus scanAndCaptureStatus(TaskType type, int sequenceOrder) {
            Courier courier = courier(courierId);
            Task task = task(courier, type, sequenceOrder, TaskStatus.PENDING);
            mockScanSetup(task, courier);

            operationsService.scanShipment(new ScanRequest(shipmentId, trackingNumber), userId);

            ArgumentCaptor<ShipmentScannedEvent> captor =
                    ArgumentCaptor.forClass(ShipmentScannedEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            return captor.getValue().completionStatus();
        }

        @Test
        @DisplayName("PICKUP → PICKED_UP")
        void pickup() {
            assertThat(scanAndCaptureStatus(TaskType.PICKUP, 1))
                    .isEqualTo(TaskCompletionStatus.PICKED_UP);
        }

        @Test
        @DisplayName("HUB_DROPOFF → IN_HUB")
        void hubDropoff() {
            assertThat(scanAndCaptureStatus(TaskType.HUB_DROPOFF, 2))
                    .isEqualTo(TaskCompletionStatus.IN_HUB);
        }

        @Test
        @DisplayName("LINE_HAUL → IN_TRANSIT")
        void lineHaul() {
            assertThat(scanAndCaptureStatus(TaskType.LINE_HAUL, 3))
                    .isEqualTo(TaskCompletionStatus.IN_TRANSIT);
        }

        @Test
        @DisplayName("HUB_PICKUP → IN_HUB")
        void hubPickup() {
            assertThat(scanAndCaptureStatus(TaskType.HUB_PICKUP, 4))
                    .isEqualTo(TaskCompletionStatus.IN_HUB);
        }

        @Test
        @DisplayName("DELIVERY → DELIVERED")
        void delivery() {
            assertThat(scanAndCaptureStatus(TaskType.DELIVERY, 5))
                    .isEqualTo(TaskCompletionStatus.DELIVERED);
        }
    }
}