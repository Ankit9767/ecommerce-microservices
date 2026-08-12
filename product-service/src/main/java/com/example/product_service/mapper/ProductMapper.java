package com.example.product_service.mapper;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(CreateProductRequest request);

    ProductResponse toResponse(Product product);

    void updateEntity(UpdateProductRequest request,
            @MappingTarget Product product
    );
}