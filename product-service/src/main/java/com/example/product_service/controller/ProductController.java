package com.example.product_service.controller;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(Pageable pageable) {

        return ResponseEntity.ok(
                service.getAllProducts(pageable)
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication,'ADMIN', 'CUSTOMER')")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getProduct(id)
        );
    }

    @PostMapping
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductResponse response = service.createProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {

        return ResponseEntity.ok(
                service.updateProduct(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {

        service.deactivateProduct(id);

        return ResponseEntity.noContent().build();
    }
}