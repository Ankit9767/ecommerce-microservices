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
import com.example.product_service.metrics.ProductMetrics;
import com.example.product_service.repository.CategoryRepository;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import com.example.product_service.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final CategoryRepository categoryRepository;

    private final ProductMapper mapper;

    private final ProductMetrics metrics;

    public ProductServiceImpl(ProductRepository repository, CategoryRepository categoryRepository,
                              ProductMapper mapper, ProductMetrics metrics) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.metrics = metrics;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        String normalizedSku =
                request.getSku()
                        .trim()
                        .toUpperCase();

        if (repository.existsBySku(normalizedSku)) {
            metrics.duplicateSku();
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

        metrics.productCreated();

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {

        Product product = repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> {
                    metrics.productNotFound();
                    return new ProductNotFoundException(id);
                });

        metrics.productViewed();

        return mapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(String search, String category,
                                                Pageable pageable) {

        Specification<Product> specification = ProductSpecification.active();

        if (search != null && !search.isBlank()) {

            specification =
                    specification.and(
                            ProductSpecification.search(search)
                    );
        }

        if (category != null && !category.isBlank()) {

            specification =
                    specification.and(
                            ProductSpecification.category(category)
                    );
        }

        return repository
                .findAll(specification, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest request) {

        Product existing = repository.findById(id)
                .orElseThrow(() -> {
                            metrics.productNotFound();
                            return new ProductNotFoundException(id);
                        }
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

        metrics.productDeactivated();
    }
}