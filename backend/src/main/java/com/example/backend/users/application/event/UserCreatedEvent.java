package com.example.backend.users.application.event;

import java.util.UUID;

public record UserCreatedEvent(
        UUID userId,
        String email,
        String tempPassword
) {}