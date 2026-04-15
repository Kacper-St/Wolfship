package com.example.backend.operations.api.mapper;

import com.example.backend.operations.api.dto.CourierResponse;
import com.example.backend.operations.api.dto.TaskResponse;
import com.example.backend.operations.domain.model.Courier;
import com.example.backend.operations.domain.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OperationsMapper {

    CourierResponse toCourierResponse(Courier courier);

    @Mapping(target = "courier", source = "courier")
    TaskResponse toTaskResponse(Task task);
}