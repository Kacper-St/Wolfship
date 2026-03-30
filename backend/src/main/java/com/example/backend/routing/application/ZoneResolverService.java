package com.example.backend.routing.application;

import com.example.backend.routing.domain.model.Zone;

public interface ZoneResolverService {
    Zone resolveZone(double lat, double lon);
}