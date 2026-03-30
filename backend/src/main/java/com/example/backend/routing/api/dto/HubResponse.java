package com.example.backend.routing.api.dto;

import java.util.UUID;

public record HubResponse(
        UUID id,
        String name,
        String code
) {}