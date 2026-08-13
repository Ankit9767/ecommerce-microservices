package com.example.product_service.service.impl;

import com.example.product_service.dto.CategoryResponse;
import com.example.product_service.dto.CreateCategoryRequest;
import com.example.product_service.dto.UpdateCategoryRequest;
import com.example.product_service.entity.Category;
import com.example.product_service.exception.CategoryNotFoundException;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    public CategoryServiceImpl(CategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {

        String name = request.getName().trim();
        String slug = request.getSlug().trim().toLowerCase();

        if (repository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException(
                    "Category already exists: " + name
            );
        }

        if (repository.existsBySlug(slug)) {
            throw new IllegalArgumentException(
                    "Category slug already exists: " + slug
            );
        }

        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(request.getDescription())
                .active(true)
                .build();

        Category saved = repository.save(category);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getCategory(Long id) {

        Category category = repository.findById(id)
                .filter(Category::getActive)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        return toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {

        return repository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id,
                                           UpdateCategoryRequest request) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        String name = request.getName().trim();
        String slug = request.getSlug().trim().toLowerCase();

        if (!category.getName().equalsIgnoreCase(name)
                && repository.existsByNameIgnoreCase(name)) {

            throw new IllegalArgumentException(
                    "Category already exists: " + name
            );
        }

        if (!category.getSlug().equals(slug)
                && repository.existsBySlug(slug)) {

            throw new IllegalArgumentException(
                    "Category slug already exists: " + slug
            );
        }

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(request.getDescription());

        return toResponse(repository.save(category));
    }

    @Override
    @Transactional
    public void deactivateCategory(Long id) {

        Category category = repository.findById(id)
                .orElseThrow(() ->
                        new CategoryNotFoundException(id)
                );

        category.setActive(false);

        repository.save(category);
    }

    private CategoryResponse toResponse(Category category) {

        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .active(category.getActive())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}