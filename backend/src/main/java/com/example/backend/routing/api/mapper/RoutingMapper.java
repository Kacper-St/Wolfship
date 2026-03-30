package com.example.backend.routing.api.mapper;

import com.example.backend.routing.api.dto.HubResponse;
import com.example.backend.routing.api.dto.RouteResponse;
import com.example.backend.routing.api.dto.ZoneResponse;
import com.example.backend.routing.domain.model.Hub;
import com.example.backend.routing.domain.model.ShipmentRoute;
import com.example.backend.routing.domain.model.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoutingMapper {
    HubResponse toHubResponse(Hub hub);
    ZoneResponse toZoneResponse(Zone zone);
    RouteResponse toRouteResponse(ShipmentRoute route);
}