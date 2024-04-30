package com.relic.relic.repository;

import com.relic.relic.enums.Condition;
import com.relic.relic.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p " +
            "WHERE (:name IS NULL OR p.name LIKE %:name%) " +
            "AND (:condition IS NULL OR p.condition LIKE %:condition%) " +
            "AND (:currentBid IS NULL OR p.currentBid LIKE %:currentBid%) " +
            "AND (:category IS NULL OR p.category LIKE %:category%) " )
    List<Product> findProductsByFilters(
            @Param("name") String name,
            @Param("condition") String condition,
            @Param("currentBid") String currentBid,
            @Param("category") String category
    );
    List<Product> findProductsByCategoryId(@Param("categoryId") Long categoryId);
    List<Product> findProductsByCondition(@Param("condition") Condition condition);
    List<Product> findByNameIsContainingIgnoreCase(@Param("productName") String productName);
    @Query("SELECT p FROM Product p WHERE p.minimumBid >= :minimumBid")
    List<Product> findProductsByMinimumBid(Double minimumBid);
    @Query("SELECT DISTINCT p FROM Product p JOIN p.reviews r WHERE r.rating = :rating")
    List<Product> findProductsByReviewsRating(@Param("rating") int rating);
    @Query("SELECT p FROM Product p WHERE p.currentBid BETWEEN :minBid AND :maxBid")
    List<Product> findProductsByCurrentBidRange(@Param("minBid") Long minBid, @Param("maxBid") Long maxBid);
}
