package com.example.order_service.mapper;

import com.example.order_service.dto.CreateOrderRequest;
import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(CreateOrderRequest request);

    OrderResponse toResponse(Order order);
}
