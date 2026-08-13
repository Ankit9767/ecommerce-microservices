package com.example.product_service.service.impl;

import com.example.product_service.dto.CreateProductRequest;
import com.example.product_service.dto.ProductResponse;
import com.example.product_service.dto.UpdateProductRequest;
import com.example.product_service.entity.Product;
import com.example.product_service.exception.DuplicateSkuException;
import com.example.product_service.exception.ProductNotFoundException;
import com.example.product_service.mapper.ProductMapper;
import com.example.product_service.repository.ProductRepository;
import com.example.product_service.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductServiceImpl(ProductRepository repository,
                              ProductMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {

        if (repository.existsBySku(request.getSku())) {
            throw new DuplicateSkuException(request.getSku());
        }

        Product product = mapper.toEntity(request);

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
    public List<ProductResponse> getAllProducts() {

        return repository.findByActiveTrue()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id,
                                         UpdateProductRequest request) {

        Product existing = repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id)
                );

        mapper.updateEntity(request, existing);

        return mapper.toResponse(
                repository.save(existing)
        );
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