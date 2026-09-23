package com.example.product_service.controller;

import com.example.product_service.dto.ProductImageResponse;
import com.example.product_service.dto.ProductImageUploadRequest;
import com.example.product_service.service.ProductImageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/images")
public class ProductImageController {

    private final ProductImageService service;

    public ProductImageController(ProductImageService service) {
        this.service = service;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductImageResponse> uploadImage(
            @PathVariable Long productId,
            @RequestPart("file") MultipartFile file,
            @Valid @ModelAttribute ProductImageUploadRequest request) {

        ProductImageResponse response = service.uploadImage(
                productId,
                file,
                request.getDisplayOrder(),
                request.getPrimaryImage()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<List<ProductImageResponse>> getProductImages(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                service.getProductImages(productId)
        );
    }

    @GetMapping("/{imageId}")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<ProductImageResponse> getProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        return ResponseEntity.ok(
                service.getProductImage(productId, imageId)
        );
    }

    @GetMapping("/{imageId}/content")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public ResponseEntity<byte[]> getImageContent(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        byte[] content = service.getImageContent(
                productId,
                imageId
        );

        String contentType = service.getImageContentType(
                productId,
                imageId
        );

        MediaType mediaType;

        try {

            mediaType = MediaType.parseMediaType(contentType);

        } catch (IllegalArgumentException ex) {

            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .contentType(mediaType)
                .body(content);
    }

    @PutMapping("/{imageId}/primary")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ProductImageResponse> setPrimaryImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        return ResponseEntity.ok(
                service.setPrimaryImage(
                        productId,
                        imageId
                )
        );
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {

        service.deleteImage(productId, imageId);

        return ResponseEntity.noContent().build();
    }
}