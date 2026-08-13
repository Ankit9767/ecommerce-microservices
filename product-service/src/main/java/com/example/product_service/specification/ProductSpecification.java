package com.example.product_service.specification;

import com.example.product_service.entity.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> active() {

        return (root, query, cb) ->
                cb.isTrue(root.get("active"));
    }

    public static Specification<Product> search(String search) {

        return (root, query, cb) -> {

            String pattern =
                    "%" + search.trim().toLowerCase() + "%";

            return cb.or(
                    cb.like(
                            cb.lower(root.get("name")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("description")),
                            pattern
                    ),
                    cb.like(
                            cb.lower(root.get("sku")),
                            pattern
                    )
            );
        };
    }

    public static Specification<Product> category(String category) {

        return (root, query, cb) ->
                cb.equal(
                        cb.lower(
                                root.get("category")
                                        .get("name")
                        ),
                        category.trim().toLowerCase()
                );
    }
}