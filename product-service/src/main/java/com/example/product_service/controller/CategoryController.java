package com.example.product_service.controller;

import com.example.product_service.dto.CategoryResponse;
import com.example.product_service.dto.CreateCategoryRequest;
import com.example.product_service.dto.UpdateCategoryRequest;
import com.example.product_service.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(service.createCategory(request));
    }

    @GetMapping
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {

        return ResponseEntity.ok(
                service.getAllCategories()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasAnyRole(authentication, 'ADMIN', 'CUSTOMER')")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {

        return ResponseEntity.ok(
                service.getCategory(id)
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id,
                                                           @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(
                service.updateCategory(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@roleSecurity.hasRole(authentication, 'ADMIN')")
    public ResponseEntity<Void> deactivateCategory(@PathVariable Long id) {

        service.deactivateCategory(id);

        return ResponseEntity.noContent().build();
    }
}