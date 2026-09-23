package com.example.product_service.service;

import com.example.product_service.dto.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductImageService {

    ProductImageResponse uploadImage(Long productId, MultipartFile file,
                                     Integer displayOrder, Boolean primaryImage);

    List<ProductImageResponse> getProductImages(Long productId);

    ProductImageResponse getProductImage(Long productId, Long imageId);

    byte[] getImageContent(Long productId, Long imageId);

    String getImageContentType(Long productId, Long imageId);

    void deleteImage(Long productId, Long imageId);

    ProductImageResponse setPrimaryImage(Long productId, Long imageId);
}