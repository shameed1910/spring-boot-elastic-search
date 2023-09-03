package com.example.demo.elasticcrudapi.service;

import com.example.demo.elasticcrudapi.domain.Product;
import com.example.demo.elasticcrudapi.repository.ProductRepository;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final String PRODUCT_INDEX = "products";
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;


    public Product save( Product product ) {

        try {
            return productRepository.save(product);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Optional<Product> findById( String id ) {
        return productRepository.findById(id);
    }

    public Iterable<Product> findAll() {
        return productRepository.findAll();
    }

    public void deleteById( String id ) {
        productRepository.deleteById(id);
    }

    public void delete( Product product ) {
        productRepository.delete(product);
    }

    public List<Product> findProductsByBoolQuery( String category, BigDecimal minPrice, Boolean inStock ) {
        //Bool Query
        QueryBuilder boolQuery = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("category", category))
                .should(QueryBuilders.rangeQuery("price").lt(minPrice))
                .should(QueryBuilders.matchQuery("inStock", inStock));

        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery).build();
        SearchHits<Product> searchHits = elasticsearchRestTemplate
                .search(nativeSearchQuery, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    public List<Product> findByInStock( boolean inStock ) {
        QueryBuilder termQueryBuilder = QueryBuilders.termQuery("inStock", inStock);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(termQueryBuilder).build();
        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());

    }


    public List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice) {
        QueryBuilder rangeQueryBuilder = QueryBuilders
                .rangeQuery("price").gte(minPrice).lte(maxPrice);
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(rangeQueryBuilder).build();
        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    public List<Product> findProductsByName( final String searchKeyword ) {

        //Search for an exact match of a term in a field:
        QueryBuilder termQueryBuilder = QueryBuilders.termQuery("category", searchKeyword);
        //Search for documents where a numeric field matches a specific value:
        QueryBuilder termQueryBuilder1 = QueryBuilders.termQuery("price", 42);
        //Search for documents where a boolean field matches a specific value:
        QueryBuilder termQueryBuilder2 = QueryBuilders.termQuery("inStock", true);

        // Perform a full-text search on a field with relevance scoring:
        //Match documents where a text field contains any of the specified terms:
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchKeyword);
        //Match documents where a text field contains any of the specified terms with a minimum should match requirement:
        QueryBuilder matchQueryBuilder1 = QueryBuilders.matchQuery("name", searchKeyword)
                .minimumShouldMatch("2");

        //NativeSearch Query
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQueryBuilder).build();

        //String Query
        Query stringSearchQuery = new StringQuery(
                "{\"match\":{\"name\":{\"query\":\"" + searchKeyword + "\"}}}\"");

        //criteria query
        Criteria criteria = new Criteria("price")
                .greaterThan(200.0)
                .lessThan(400.0);
        Query criteriaSearchQuery = new CriteriaQuery(criteria);

        SearchHits<Product> searchHits = elasticsearchRestTemplate
                .search(nativeSearchQuery, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }


    public List<String> fetchSuggestions( String nameKeyword ) {
        String lowercaseNameKeyword = nameKeyword.toLowerCase();

        QueryBuilder wildcardQueryBuilder = QueryBuilders
                .wildcardQuery("name", lowercaseNameKeyword + "*");

        Query wildcardSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(wildcardQueryBuilder)
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(
                wildcardSearchQuery, Product.class);

        return searchHits.getSearchHits().stream()
                .map(searchHit -> searchHit.getContent().getName())
                .collect(Collectors.toList());
    }

    public List<Product> fuzzySearch( String nameKeyword ) {
        //Create query on name field enabling fuzzy search
        QueryBuilder fuzzinessQueryBuilder = QueryBuilders
                .matchQuery("name", nameKeyword)
                .fuzziness(Fuzziness.AUTO);

        NativeSearchQuery fuzzinessQueryBuilder1 = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", nameKeyword)
                        .fuzziness(Fuzziness.ONE)
                        .prefixLength(3))
                .build();

       /* NativeSearchQuery multiMatchSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(fuzzinessQueryBuilder).build();*/

        SearchHits<Product> searchHits = elasticsearchRestTemplate
                .search(fuzzinessQueryBuilder1, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    public List<Product> multiMatchQuery( String nameKeyword ) {
        //Create query on multiple fields
        QueryBuilder multiMatchQueryBuilder = QueryBuilders
                .multiMatchQuery(nameKeyword, "category", "name");

        NativeSearchQuery multiMatchSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(multiMatchQueryBuilder).build();

        SearchHits<Product> searchHits = elasticsearchRestTemplate
                .search(multiMatchSearchQuery, Product.class);
        return searchHits.stream().map(SearchHit::getContent).collect(Collectors.toList());
    }

    //To calculate the average price for products in each category:
    public Map<String, Double> calculateAveragePricePerCategory() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(AggregationBuilders.terms("by_category").field("category")
                        .subAggregation(AggregationBuilders.avg("avg_price").field("price")))
                .build();

        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(searchQuery, Product.class);

        Terms byCategoryAggregation = searchHits.getAggregations().get("by_category");
        Map<String, Double> result = new HashMap<>();
        for (Terms.Bucket bucket : byCategoryAggregation.getBuckets()) {
            Avg avgPriceAggregation = bucket.getAggregations().get("avg_price");
            double avgPrice = avgPriceAggregation.getValue();
            result.put(bucket.getKeyAsString(), avgPrice);
        }
        return result;
    }

    //To count the number of products in each category:
    public Map<String, Long> countProductsPerCategory() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchAllQuery())
                .addAggregation(AggregationBuilders.terms("by_category").field("category"))
                .build();

        SearchHits<Product> searchHits = elasticsearchRestTemplate.search(searchQuery, Product.class);

        Terms byCategoryAggregation = searchHits.getAggregations().get("by_category");
        Map<String, Long> result = new HashMap<>();
        for (Terms.Bucket bucket : byCategoryAggregation.getBuckets()) {
            long docCount = bucket.getDocCount();
            result.put(bucket.getKeyAsString(), docCount);
        }
        return result;
    }

}

