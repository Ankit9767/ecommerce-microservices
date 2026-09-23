package com.example.product_service.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProductImageResponse {

    Long id;

    String imageUrl;

    String contentType;

    Long fileSize;

    String originalFilename;

    Integer displayOrder;

    Boolean primaryImage;
}