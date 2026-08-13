package com.example.product_service.service;

import com.example.product_service.dto.CategoryResponse;
import com.example.product_service.dto.CreateCategoryRequest;
import com.example.product_service.dto.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse getCategory(Long id);

    List<CategoryResponse> getAllCategories();

    CategoryResponse updateCategory(Long id,
                                    UpdateCategoryRequest request);

    void deactivateCategory(Long id);
}