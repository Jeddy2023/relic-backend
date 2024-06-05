package com.relic.relic.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TotalAdminCountsDto {
    private long totalUsers;
    private long totalProducts;
    private long totalBids;
}
