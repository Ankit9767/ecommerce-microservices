package com.example.search_service.document;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "reviews")
public class ReviewSearchDocument {

    @Id
    private Long reviewId;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Integer)
    private Integer rating;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String comment;
}