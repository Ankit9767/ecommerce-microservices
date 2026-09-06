package com.example.reviews_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_user_product_order",
                        columnNames = {
                                "user_id",
                                "product_id",
                                "order_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_review_product",
                        columnList = "product_id"
                ),
                @Index(
                        name = "idx_review_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_review_order",
                        columnList = "order_id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 150)
    private String title;

    @Column(length = 5000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    @Version
    @Column(nullable = false)
    private Long version;
}