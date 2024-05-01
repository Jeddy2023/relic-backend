package com.relic.relic.dto;
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
    private Long currentBid;
    private Date biddingTimeLimit;
    private String vendorShopName;
}
