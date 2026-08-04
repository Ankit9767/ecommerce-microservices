package com.example.product_service.Service;

import com.example.product_service.Entity.Product;
import com.example.product_service.Exception.ProductNotFoundException;
import com.example.product_service.Repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService{

    private final ProductRepository repository;

    public ProductServiceImpl(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public Product createProduct(Product product) {
        return repository.save(product);
    }

    @Override
    public Product getProduct(Long id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(id));
    }

    @Override
    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product updated) {
        Product existing =
                repository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(id));

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        existing.setCategory(updated.getCategory());

        return repository.save(existing);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if(!repository.existsById(id)){
            throw new ProductNotFoundException(id);
        }
        repository.deleteById(id);
    }

}
