package com.ecommerce.common.kafka;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "outbox_events",
        indexes = {
                @Index(
                        name = "idx_outbox_published_id",
                        columnList = "published,id"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false)
    private Long aggregateId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime publishedAt;
}