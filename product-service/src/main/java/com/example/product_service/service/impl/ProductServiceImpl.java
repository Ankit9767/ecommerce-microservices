package com.example.product_service.service.impl;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.entity.Category;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.CategoryNotFoundException;
import com.example.product_service.exception.DuplicateSkuException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final CategoryRepository categoryRepository;

    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository, CategoryRepository categoryRepository,
                              ProductMapper mapper) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        String normalizedSku =
                request.getSku()
                        .trim()
                        .toUpperCase();

        if (repository.existsBySku(normalizedSku)) {
            throw new DuplicateSkuException(normalizedSku);
        }

        Product product = mapper.toEntity(request);

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                request.getCategoryId()
                        )
                );

        if (!category.getActive()) {
            throw new IllegalStateException(
                    "Category is inactive"
            );
        }

        product.setCategory(category);
        product.setSku(normalizedSku);
        product.setActive(true);

        Product saved = repository.save(product);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {

        Product product = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        return mapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {

        return repository.findByActiveTrue(pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest request) {

        Product existing = repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        Category category = categoryRepository
                .findById(request.getCategoryId())
                .orElseThrow(() ->
                        new CategoryNotFoundException(
                                request.getCategoryId()
                        )
                );

        if (!category.getActive()) {
            throw new IllegalStateException(
                    "Category is inactive"
            );
        }

        mapper.updateEntity(request, existing);

        existing.setCategory(category);

        return mapper.toResponse(repository.save(existing));
    }

    @Override
    @Transactional
    public void deactivateProduct(Long id) {

        Product product = repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        product.setActive(false);

        repository.save(product);
    }
}