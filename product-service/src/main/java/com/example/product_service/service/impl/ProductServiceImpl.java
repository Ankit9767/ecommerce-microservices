package com.example.product_service.service.impl;

import com.ecommerce.common.dto.ProductResponse;
import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.entity.Category;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.CategoryNotFoundException;
import com.example.product_service.exception.DuplicateSkuException;
import com.example.product_service.exception.ProductNotFoundException;
import com.ecommerce.common.events.ProductCreatedEvent;
import com.ecommerce.common.events.ProductDeletedEvent;
import com.ecommerce.common.events.ProductEvent;
import com.ecommerce.common.events.ProductUpdatedEvent;
import com.example.product_service.kafka.ProductEventProducer;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;

    private final CategoryRepository categoryRepository;

    private final ProductMapper mapper;

    private final ProductMetrics metrics;

    private final ProductEventProducer productEventProducer;

    public ProductServiceImpl(ProductRepository repository, CategoryRepository categoryRepository,
                              ProductMapper mapper, ProductMetrics metrics,
                              ProductEventProducer productEventProducer) {

        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.mapper = mapper;
        this.metrics = metrics;
        this.productEventProducer = productEventProducer;
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

        ProductResponse response = mapper.toResponse(saved);

        publishProductEvent(
                ProductCreatedEvent.of(
                        response.getId(),
                        response.getName(),
                        response.getSku(),
                        response.getCategory(),
                        response.getPrice(),
                        response.getActive()
                )
        );

        return response;
    }

    private void publishProductEvent(ProductEvent event) {
        try {
            productEventProducer.publish(event);
        } catch (Exception ex) {
            metrics.productEventPublishFailed();
            log.error("Failed to publish product event '{}'", event.getEventType(), ex);
        }
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

        ProductResponse response = mapper.toResponse(repository.save(existing));

        publishProductEvent(
                ProductUpdatedEvent.of(
                        response.getId(),
                        response.getName(),
                        response.getSku(),
                        response.getCategory(),
                        response.getPrice(),
                        response.getActive()
                )
        );

        return response;
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

        publishProductEvent(
                ProductDeletedEvent.of(
                        product.getId()
                )
        );
    }
}