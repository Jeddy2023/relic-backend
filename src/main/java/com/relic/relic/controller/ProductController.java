package com.relic.relic.controller;

import com.relic.relic.dto.CreateProductDto;
import com.relic.relic.dto.ProductDto;
import com.relic.relic.enums.Condition;
import com.relic.relic.model.Category;
import com.relic.relic.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auction")
@CrossOrigin(origins = "*")
public class ProductController {
    @Autowired
    ProductService productService;

    @PostMapping
    public ResponseEntity<String> createAuction(@RequestBody CreateProductDto createProductDto) {
        String createdProduct = productService.createProduct(createProductDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<ProductDto> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDto>> getProductByCategory(@PathVariable Long categoryId) {
        List<ProductDto> products = productService.getProductByCategory(categoryId);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/condition/{condition}")
    public ResponseEntity<List<ProductDto>> getProductByCondition(@PathVariable Condition condition) {
        List<ProductDto> products = productService.getProductByCondition(condition);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        ProductDto product = productService.getProductById(id);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        String response = productService.deleteProduct(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable Long id, @RequestBody CreateProductDto createProductDto) {
        String response = productService.updateProduct(id, createProductDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String keyword) {
        List<ProductDto> products = productService.searchProducts(keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/bid-range")
    public ResponseEntity<List<ProductDto>> getProductsByCurrentBidRange(@RequestParam Long minBid, @RequestParam Long maxBid) {
        List<ProductDto> products = productService.getProductsByCurrentBidRange(minBid, maxBid);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/minimum-bid/{minimumBid}")
    public ResponseEntity<List<ProductDto>> getProductsByMinimumBid(@PathVariable Double minimumBid) {
        List<ProductDto> products = productService.getProductsByMinimumBid(minimumBid);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ProductDto>> getProductsByRating(@PathVariable int rating) {
        List<ProductDto> products = productService.getProductsByRating(rating);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/average-rating")
    public ResponseEntity<List<ProductDto>> getProductsSortedByAverageRating() {
        List<ProductDto> products = productService.getProductsSortedByAverageRating();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/current-bid-low-to-high")
    public ResponseEntity<List<ProductDto>> getProductsSortedByCurrentBidLowToHigh() {
        List<ProductDto> products = productService.getProductsSortedByCurrentBidLowToHigh();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/current-bid-high-to-low")
    public ResponseEntity<List<ProductDto>> getProductsSortedByCurrentBidHighToLow() {
        List<ProductDto> products = productService.getProductsSortedByCurrentBidHighToLow();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/auction-ending-soonest")
    public ResponseEntity<List<ProductDto>> getProductsSortedByAuctionEndingSoonest() {
        List<ProductDto> products = productService.getProductsSortedByAuctionEndingSoonest();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/recently-started")
    public ResponseEntity<List<ProductDto>> getProductsSortedByRecentlyStarted() {
        List<ProductDto> products = productService.getProductsSortedByRecentlyStarted();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    @GetMapping("/sorted/most-active")
    public ResponseEntity<List<ProductDto>> getProductsSortedByMostActive() {
        List<ProductDto> products = productService.getProductsSortedByMostActive();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

}
