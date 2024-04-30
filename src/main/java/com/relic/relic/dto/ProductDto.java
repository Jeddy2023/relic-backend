package com.relic.relic.dto;

import com.relic.relic.model.User;

import java.util.Date;

public class ProductDto {
    private Long id;
    private String name;
    private Long currentBid;
    private Date biddingTimeLimit;
    private User vendor;
}
