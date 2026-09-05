package com.example.search_service.document;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private Long productId;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(
                            suffix = "keyword",
                            type = FieldType.Keyword
                    )
            }
    )
    private String name;

    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Boolean)
    private Boolean active;
}