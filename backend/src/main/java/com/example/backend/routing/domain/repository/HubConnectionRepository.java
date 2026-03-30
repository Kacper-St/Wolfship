package com.example.backend.routing.domain.repository;

import com.example.backend.routing.domain.model.HubConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HubConnectionRepository extends JpaRepository<HubConnection, UUID> {

    @Query("SELECT hc FROM HubConnection hc " +
           "JOIN FETCH hc.sourceHub " +
           "JOIN FETCH hc.targetHub")
    List<HubConnection> findAllWithHubs();
}