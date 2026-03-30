package com.example.backend.routing.api.dto;

import java.util.UUID;

public record ZoneResponse(
        UUID id,
        String name,
        String terytCode,
        HubResponse hub
) {}