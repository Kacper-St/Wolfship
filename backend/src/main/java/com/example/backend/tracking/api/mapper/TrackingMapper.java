package com.example.backend.tracking.api.mapper;

import com.example.backend.tracking.api.dto.TrackingEventResponse;
import com.example.backend.tracking.domain.model.TrackingEvent;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrackingMapper {
    TrackingEventResponse toResponse(TrackingEvent event);
}