package com.example.backend.routing.application.event;

import java.util.List;
import java.util.UUID;

public record RouteCalculatedEvent(
        UUID shipmentId,
        UUID sourceHubId,
        UUID targetHubId,
        List<UUID> hubSequence
) {}