package com.relic.relic.repository;

import com.relic.relic.enums.ReviewType;
import com.relic.relic.model.Product;
import com.relic.relic.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByReviewType (ReviewType reviewType);
    List<Review> findAllByProduct(Product product);
}
