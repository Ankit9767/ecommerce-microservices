package com.example.order_service.Mapper;

import com.example.order_service.Dto.CreateOrderRequest;
import com.ecommerce.common.dto.OrderResponse;
import com.example.order_service.Entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(CreateOrderRequest request);

    OrderResponse toResponse(Order order);
}
