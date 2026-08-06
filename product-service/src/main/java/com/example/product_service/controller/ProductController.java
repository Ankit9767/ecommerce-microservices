package com.example.product_service.controller;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.entity.Product;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;
    private final ProductMapper mapper;

    public ProductController(ProductService service, ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasAnyRole('ADMIN','USER')")
    public ResponseEntity<List<ProductResponse>> getAllProducts( @RequestHeader("X-Authenticated-User") String username,
                                                                 @RequestHeader("X-User-Role") String role) {
        System.out.println(username);
        System.out.println(role);
        List<ProductResponse> responses = service.getAllProducts()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        Product product = service.getProduct(id);
        return ResponseEntity.ok(mapper.toResponse(product));
    }

    @PostMapping
    @PreAuthorize("@roleSecurity.hasRole('ADMIN')")
    public ResponseEntity<ProductResponse> createProduct(
            @RequestBody CreateProductRequest request) {

        Product product = mapper.toEntity(request);
        Product saved = service.createProduct(product);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody Product product) {

        return ResponseEntity.ok(
                service.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id) {

        service.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}
