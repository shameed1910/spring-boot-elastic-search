package com.example.demo.elasticcrudapi.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;


@Document(indexName = "products")
@Data
public class Product {
    @Id
    private String id;
    private String name;
    @Field(type = FieldType.Keyword) // Use a keyword field for "category"
    private String category;
    private BigDecimal price;
    private Boolean inStock;
}