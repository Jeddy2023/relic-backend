package com.relic.relic.service;

import com.relic.relic.dto.ApplicationReviewDto;
import com.relic.relic.dto.ProductReveiwDto;
import com.relic.relic.dto.ReviewDto;
import com.relic.relic.model.Review;

import java.util.List;

public interface ReviewService {
    String giveproductReview(ProductReveiwDto productReviewDTO, Long productId);
    String giveApplicationReview(ApplicationReviewDto applicationReviewDTO);
    List<ReviewDto> getAllReviews();
    List<ReviewDto> getAllProductReviews();
    List<ReviewDto> getAllApplicationReviews();
    List<ReviewDto> getAllReviewForAParticularProduct(Long productId);
}
