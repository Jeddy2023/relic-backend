package com.relic.relic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProductDto {
    private Long id;
    private String productImageUrl;
    private String name;
    private int totalReviews;
    private int totalBids;
    private Long currentBid;
    private String updatedAt;
}
