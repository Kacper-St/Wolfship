package com.example.backend.operations.domain.repository;

import com.example.backend.operations.domain.model.Task;
import com.example.backend.operations.domain.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("SELECT t FROM Task t " +
           "JOIN FETCH t.courier " +
           "WHERE t.courier.id = :courierId " +
           "AND t.taskStatus IN ('PENDING', 'IN_PROGRESS') " +
           "ORDER BY t.sequenceOrder ASC")
    List<Task> findActiveByCourierId(UUID courierId);

    Optional<Task> findFirstByShipmentIdAndTaskStatusOrderBySequenceOrder(UUID shipmentId, TaskStatus taskStatus);
    List<Task> findAllByCourierIsNullAndTaskStatus(TaskStatus taskStatus);
    Optional<Task> findByShipmentIdAndSequenceOrder(UUID shipmentId, Integer sequenceOrder);
}