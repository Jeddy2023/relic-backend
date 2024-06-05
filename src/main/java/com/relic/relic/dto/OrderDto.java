package com.relic.relic.dto;

import com.relic.relic.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private String productName;
    private String vendorShopName;
    private String customerUsername;
    private Date createdAt;
    private Long totalPrice;
    private Double companyPercentage;
    private Double companyCommission;
    private Boolean paymentReceived;
    private Boolean vendorPaymentSent;
    private OrderStatus status;
    private String cancelReason;
}
