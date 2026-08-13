package com.example.product_service.mapper;

import com.ecommerce.common.dto.ProductResponse;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(
            target = "category",
            ignore = true
    )
    Product toEntity(CreateProductRequest request);

    @Mapping(
            target = "categoryId",
            source = "category.id"
    )
    @Mapping(
            target = "category",
            source = "category.name"
    )
    ProductResponse toResponse(Product product);

    @Mapping(
            target = "category",
            ignore = true
    )
    void updateEntity(UpdateProductRequest request,
            @MappingTarget Product product
    );
}