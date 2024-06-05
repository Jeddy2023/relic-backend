package com.relic.relic.dto;

import com.relic.relic.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderUserDto {
    private Long id;
    private String productName;
    private Date createdAt;
    private Long totalPrice;
    private Double companyPercentage;
    private Double companyCommission;
    private String status;
    private String vendorShopName;
    private String customerUsername;
}
