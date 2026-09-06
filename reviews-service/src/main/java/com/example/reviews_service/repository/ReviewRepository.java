package com.example.reviews_service.repository;

import com.example.reviews_service.entity.Review;
import com.example.reviews_service.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserIdAndProductIdAndOrderId(Long userId,
                                                        Long productId,
                                                        Long orderId);

    boolean existsByUserIdAndProductIdAndOrderId(Long userId,
                                                 Long productId,
                                                 Long orderId);

    Page<Review> findByProductIdAndStatus(Long productId,
                                          ReviewStatus status, Pageable pageable);

    Page<Review> findByUserIdAndStatus(Long userId,
                                       ReviewStatus status, Pageable pageable);

    long countByProductIdAndStatus(Long productId,
                                   ReviewStatus status);
}