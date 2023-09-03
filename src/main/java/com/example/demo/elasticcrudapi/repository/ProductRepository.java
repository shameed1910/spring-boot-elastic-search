package com.example.demo.elasticcrudapi.repository;

import com.example.demo.elasticcrudapi.domain.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {
}
