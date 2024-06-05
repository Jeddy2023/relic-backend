package com.relic.relic.repository;

import com.relic.relic.enums.ReviewType;
import com.relic.relic.model.Product;
import com.relic.relic.model.Review;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByReviewType (ReviewType reviewType);
    List<Review> findAllByProduct(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
}
