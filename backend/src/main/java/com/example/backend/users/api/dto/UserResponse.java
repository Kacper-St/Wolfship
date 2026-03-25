package com.example.backend.users.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Getter @Setter
public class UserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private boolean active;
    private boolean forcePasswordChange;
    private Set<String> roles;
    private Instant createdAt;
}
