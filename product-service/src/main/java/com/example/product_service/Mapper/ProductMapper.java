package com.example.product_service.Mapper;

import com.example.product_service.Dto.CreateProductRequest;
import com.example.product_service.Dto.ProductResponse;
import com.example.product_service.Entity.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);
}
