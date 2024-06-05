package com.relic.relic.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TotalProductsCountDto {
    private long totalPendingReviews;
    private long totalActiveProducts;
    private long totalOfflineProducts;
    private long totalUserProducts;
}
