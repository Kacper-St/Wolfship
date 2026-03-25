package com.example.backend.shipping.api.mapper;

import com.example.backend.shipping.api.dto.AddressDto;
import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.domain.model.Address;
import com.example.backend.shipping.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "coordinates", ignore = true)
    Address toEntity(AddressDto dto);

    AddressDto toDto(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trackingNumber", ignore = true)
    @Mapping(target = "senderId", ignore = true)
    @Mapping(target = "courierId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "price", expression = "java(request.getSize().getBasePrice())")
    @Mapping(target = "currency", ignore = true)
    @Mapping(target = "labelUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pickedUpAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    Shipment toEntity(ShipmentRequest request);

    ShipmentResponse toResponse(Shipment shipment);
}