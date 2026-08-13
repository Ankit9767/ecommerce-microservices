package com.example.product_service.entity;

import com.ecommerce.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(
                        name = "idx_category_slug",
                        columnList = "slug"
                ),
                @Index(
                        name = "idx_category_active",
                        columnList = "active"
                )
        }
)
public class Category extends BaseEntity {

    @Column(
            nullable = false,
            unique = true,
            length = 100
    )
    private String name;

    @Column(
            nullable = false,
            unique = true,
            length = 120
    )
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean active = true;
}