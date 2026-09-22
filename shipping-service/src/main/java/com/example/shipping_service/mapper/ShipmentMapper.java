package com.example.shipping_service.mapper;

import com.example.shipping_service.dto.ShipmentResponse;
import com.example.shipping_service.entity.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    ShipmentResponse toResponse(Shipment shipment);
}