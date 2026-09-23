package com.example.product_service.repository;

import com.example.product_service.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findAllByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    Optional<ProductImage> findByIdAndProductId(Long imageId, Long productId);

    boolean existsByIdAndProductId(Long imageId, Long productId);

    long countByProductId(Long productId);

    @Query("""
            select coalesce(max(pi.displayOrder), -1)
            from ProductImage pi
            where pi.product.id = :productId
            """)
    int findMaxDisplayOrderByProductId(@Param("productId") Long productId);

    @Modifying
    @Query("""
            update ProductImage pi
            set pi.primaryImage = false
            where pi.product.id = :productId
            """)
    int clearPrimaryImages(@Param("productId") Long productId);

    Optional<ProductImage> findFirstByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    Optional<ProductImage> findFirstByProductIdAndPrimaryImageTrue(Long productId);
}