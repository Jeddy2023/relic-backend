package com.relic.relic.service;

import com.relic.relic.dto.CreateProductDto;
import com.relic.relic.dto.ProductDto;
import com.relic.relic.enums.Condition;

import java.util.List;

public interface ProductService {
    String createProduct(CreateProductDto createProductDto);
    List<ProductDto> getAllProducts();
    List<ProductDto> getProductByCategory(Long categoryId);
    List<ProductDto> getProductByCondition(Condition condition);
    ProductDto getProductById(Long id);
    String deleteProduct(Long id);
    String updateProduct(Long id, CreateProductDto createProductDto);
    List<ProductDto> searchProducts(String productName);
    List<ProductDto> getProductsByCurrentBidRange(Long minBid, Long maxBid);
    List<ProductDto> getProductsByMinimumBid(Double minimumBid);
    List<ProductDto> getProductsByRating(int rating);
    List<ProductDto> getProductsSortedByAverageRating();
    List<ProductDto> getProductsSortedByCurrentBidLowToHigh();
    List<ProductDto> getProductsSortedByCurrentBidHighToLow();
    List<ProductDto> getProductsSortedByAuctionEndingSoonest();
    List<ProductDto> getProductsSortedByRecentlyStarted();
    List<ProductDto> getProductsSortedByMostActive();

}
