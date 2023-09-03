package com.example.demo.elasticcrudapi.controller;

import com.example.demo.elasticcrudapi.domain.Product;
import com.example.demo.elasticcrudapi.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable String id) {
        Optional<Product> optionalProduct = productService.findById(id);
        if (optionalProduct.isPresent()) {
            return new ResponseEntity<>(optionalProduct.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        Iterable<Product> products = productService.findAll();
        List<Product> productList = new ArrayList<>();
        products.forEach(productList::add);
        return new ResponseEntity<>(productList, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") String id) {
        Optional<Product> optionalProduct = productService.findById(id);

        if (optionalProduct.isPresent()) {
            productService.delete(optionalProduct.get());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/productByName/{name}")
    public List<Product> getProductsByName( @PathVariable(value = "name") String name){
       return productService.findProductsByName(name);
    }
    @GetMapping("/findByInStock/{inStock}")
    public List<Product> findByInStock(@PathVariable boolean inStock){
        return productService.findByInStock(inStock);
    }
    @GetMapping("/findPrice/findByPriceBetween/{minPrice}/{maxPrice}")
    List<Product> findByPriceBetween( @PathVariable BigDecimal minPrice, @PathVariable BigDecimal maxPrice) {
        return productService.findByPriceBetween(minPrice, maxPrice);
    }

    @GetMapping("/fetchSuggestionsByName/{name}")
    public List<String> fetchSuggestionsByName( @PathVariable(value = "name") String name){
        return productService.fetchSuggestions(name);
    }

    @GetMapping("/fuzzySearch/{name}")
    public List<Product> fuzzySearchByName( @PathVariable(value = "name") String name){
        return productService.fuzzySearch(name);
    }

    @GetMapping("/multiMatchQuery/{name}")
    public List<Product> multiMatchQuery( @PathVariable(value = "name") String name){
        return productService.multiMatchQuery(name);
    }

    @GetMapping("/productByBoolQuery")
    public List<Product> getProductsByBoolQuery( @RequestParam(name = "category") String category,
                                            @RequestParam(name = "price") BigDecimal price,
                                            @RequestParam(name = "inStock") Boolean inStock){
        return productService.findProductsByBoolQuery(category,price,inStock);
    }

    @GetMapping("/aggregations")
    public Map<String, Double> multiMatchQuery(){
        return productService.calculateAveragePricePerCategory();
    }
}

