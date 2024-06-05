package com.relic.relic.service;

import com.relic.relic.dto.*;
import com.relic.relic.enums.Condition;

import java.util.List;

public interface ProductService {
    String createProduct(CreateProductDto createProductDto);
    List<ProductDto> getAllProducts(boolean totalWonProducts,
                                    boolean totalActiveAuctions,
                                    Long categoryId,
                                    Condition condition,
                                    String keyword,
                                    Long minBid,
                                    Long maxBid,
                                    Double minimumBid,
                                    Integer rating,
                                    String sortBy);
    ProductDto getProductById(Long id);
    String deleteProduct(Long id);
    String updateProduct(Long id, CreateProductDto createProductDto);

    List<UserProductDto> getUserProducts(
            boolean pendingReviews,
            boolean activeProducts,
            boolean offlineBid,
            String keyword,
            Long userId
    );
    TotalProductsCountDto getTotalProductCounts();
    List<AdminProductDto> getAllProductsForAdmin(
            Long categoryId,
            Condition condition,
            String keyword,
            Boolean isBiddingClosed,
            Boolean isBlocked,
            String biddingTimeLimitStart,
            String biddingTimeLimitEnd,
            Long minBid,
            Long maxBid,
            String sortBy
    );
    String createProductWithUsername(CreateProductDto createProductDto);
    String blockProduct(Long productId);
    String unblockProduct(Long productId);
    String endProduct(Long productId);
    String resetWinningBidder(Long productId);
}
