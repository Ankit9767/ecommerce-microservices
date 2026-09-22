package com.example.order_service.mapper;

import com.ecommerce.common.dto.OrderItemResponse;
import com.ecommerce.common.dto.OrderResponse;
import com.ecommerce.common.dto.ShippingAddress;
import com.example.order_service.entity.Order;
import com.example.order_service.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = ".")
    OrderResponse toResponse(Order order);

    @Mapping(target = "recipientName", source = "shippingRecipientName")
    @Mapping(target = "phone", source = "shippingPhone")
    @Mapping(target = "addressLine1", source = "shippingAddressLine1")
    @Mapping(target = "addressLine2", source = "shippingAddressLine2")
    @Mapping(target = "city", source = "shippingCity")
    @Mapping(target = "state", source = "shippingState")
    @Mapping(target = "postalCode", source = "shippingPostalCode")
    @Mapping(target = "country", source = "shippingCountry")
    ShippingAddress toShippingAddress(Order order);

    OrderItemResponse toItemResponse(OrderItem item);
}