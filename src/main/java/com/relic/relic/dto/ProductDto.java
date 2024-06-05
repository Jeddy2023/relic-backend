package com.relic.relic.dto;
import com.relic.relic.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String condition;
    private String shortDescription;
    private String description;
    private Long currentBid;
    private String biddingTimeLimit;
    private String vendorShopName;
    private String productImageUrl;
    private Long minimumBid;
    private String createdAt;
    private int numberOfBids;
    private Double averageRating;
    private Long categoryId;
    private Long winningBidderId;
}
