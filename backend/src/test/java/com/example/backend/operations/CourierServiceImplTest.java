package com.example.backend.operations;

import com.example.backend.operations.api.dto.AssignCourierRequest;
import com.example.backend.operations.api.mapper.OperationsMapper;
import com.example.backend.operations.application.CourierServiceImpl;
import com.example.backend.operations.domain.model.*;
import com.example.backend.operations.domain.repository.CourierRepository;
import com.example.backend.operations.domain.repository.TaskRepository;
import com.example.backend.routing.application.event.RouteCalculatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourierServiceImpl")
class CourierServiceImplTest {

    @Mock private CourierRepository courierRepository;
    @Mock private TaskRepository taskRepository;
    @Mock private OperationsMapper operationsMapper;

    private CourierServiceImpl courierService;

    private final UUID shipmentId = UUID.randomUUID();
    private final UUID sourceZoneId = UUID.randomUUID();
    private final UUID targetZoneId = UUID.randomUUID();
    private final UUID hubWAW = UUID.randomUUID();
    private final UUID hubLOD = UUID.randomUUID();
    private final UUID hubKTW = UUID.randomUUID();
    private final UUID hubKRK = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        courierService = new CourierServiceImpl(
                courierRepository, taskRepository, operationsMapper);
    }

    private Courier anyCourier() {
        return Courier.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("createTasksForRoute")
    class CreateTasks {

        @Test
        @DisplayName("should create 7 tasks for 4-hub route with correct sequence")
        void shouldCreate7TasksForFourHubRoute() {
            // given
            List<UUID> hubSequence = List.of(hubWAW, hubLOD, hubKTW, hubKRK);
            RouteCalculatedEvent event = new RouteCalculatedEvent(
                    shipmentId, "WLF-123",
                    sourceZoneId, targetZoneId,
                    hubWAW, hubKRK,
                    hubSequence, "receiver@test.com");

            when(courierRepository.findByZoneIdAndActiveTrueOrderByWorkload(any()))
                    .thenReturn(List.of(anyCourier()));
            when(courierRepository.findBySourceHubIdAndTargetHubIdAndActiveTrue(any(), any()))
                    .thenReturn(Optional.of(anyCourier()));

            // when
            courierService.createTasksForRoute(event);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
            verify(taskRepository).saveAll(captor.capture());

            List<Task> tasks = captor.getValue();

            assertThat(tasks).hasSize(7);

            assertThat(tasks).extracting(Task::getTaskType).containsExactly(
                    TaskType.PICKUP,
                    TaskType.HUB_DROPOFF,
                    TaskType.LINE_HAUL,
                    TaskType.LINE_HAUL,
                    TaskType.LINE_HAUL,
                    TaskType.HUB_PICKUP,
                    TaskType.DELIVERY);

            assertThat(tasks).extracting(Task::getSequenceOrder)
                    .containsExactly(1, 2, 3, 4, 5, 6, 7);

            assertThat(tasks).allMatch(t -> t.getTaskStatus() == TaskStatus.PENDING);
        }

        @Test
        @DisplayName("should create 5 tasks for 2-hub route")
        void shouldCreate5TasksForTwoHubRoute() {
            // given
            List<UUID> hubSequence = List.of(hubWAW, hubLOD);
            RouteCalculatedEvent event = new RouteCalculatedEvent(
                    shipmentId, "WLF-456",
                    sourceZoneId, targetZoneId,
                    hubWAW, hubLOD,
                    hubSequence, "receiver@test.com");

            when(courierRepository.findByZoneIdAndActiveTrueOrderByWorkload(any()))
                    .thenReturn(List.of(anyCourier()));
            when(courierRepository.findBySourceHubIdAndTargetHubIdAndActiveTrue(any(), any()))
                    .thenReturn(Optional.of(anyCourier()));

            // when
            courierService.createTasksForRoute(event);

            // then
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Task>> captor = ArgumentCaptor.forClass(List.class);
            verify(taskRepository).saveAll(captor.capture());

            List<Task> tasks = captor.getValue();

            assertThat(tasks).hasSize(5);
            assertThat(tasks).extracting(Task::getTaskType).containsExactly(
                    TaskType.PICKUP,
                    TaskType.HUB_DROPOFF,
                    TaskType.LINE_HAUL,
                    TaskType.HUB_PICKUP,
                    TaskType.DELIVERY);
        }
    }

    @Nested
    @DisplayName("assignCourier — validation")
    class AssignCourierValidation {

        @Test
        @DisplayName("should reject zone courier without zoneId")
        void shouldRejectZoneCourierWithoutZone() {
            AssignCourierRequest request = new AssignCourierRequest(
                    UUID.randomUUID(), CourierType.ZONE_COURIER,
                    null, null, null);

            assertThatThrownBy(() -> courierService.assignCourier(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("zoneId");

            verify(courierRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject line haul courier without hubs")
        void shouldRejectLineHaulWithoutHubs() {
            AssignCourierRequest request = new AssignCourierRequest(
                    UUID.randomUUID(), CourierType.LINE_HAUL_COURIER,
                    null, null, null);

            assertThatThrownBy(() -> courierService.assignCourier(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("sourceHubId");

            verify(courierRepository, never()).save(any());
        }
    }
}