package com.example.backend.routing.domain.repository;

import com.example.backend.routing.domain.model.Hub;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HubRepository extends JpaRepository<Hub, UUID> {
}