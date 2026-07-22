package com.example.backend.shipping.api.mapper;

import com.example.backend.shipping.api.dto.AddressDto;
import com.example.backend.shipping.api.dto.ShipmentRequest;
import com.example.backend.shipping.api.dto.ShipmentResponse;
import com.example.backend.shipping.domain.model.Address;
import com.example.backend.shipping.domain.model.Money;
import com.example.backend.shipping.domain.model.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = Money.class)
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
    @Mapping(target = "price", expression = "java(com.example.backend.shipping.domain.model.Money.ofPln(request.getSize().getBasePrice()))")
    @Mapping(target = "labelUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pickedUpAt", ignore = true)
    @Mapping(target = "deliveredAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    Shipment toEntity(ShipmentRequest request);

    @Mapping(target = "price", source = "price.amount")
    @Mapping(target = "currency", source = "price.currency")
    @Mapping(target = "labelDownloadUrl",
            expression = "java(\"/api/v1/shipments/\" + shipment.getTrackingNumber() + \"/label\")")
    ShipmentResponse toResponse(Shipment shipment);
}