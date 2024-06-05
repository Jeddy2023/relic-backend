package com.relic.relic.service;

import com.relic.relic.dto.MonthDataDto;
import com.relic.relic.dto.MonthDoubleDataDto;
import com.relic.relic.dto.ProductAnalyticsDto;
import com.relic.relic.dto.ProductCategoryRevenueDto;

import java.util.List;

public interface AnalyticsService{
    List<MonthDataDto> generateLast12MonthsUserData();
    List<MonthDataDto> generateLast12MonthsOrderData();
    List<MonthDataDto> generateLast12MonthsUserOrderData();
    List<MonthDoubleDataDto> generateLast12MonthsRevenueData();
    List<ProductAnalyticsDto> getTopSellingProducts();
    List<ProductAnalyticsDto> getUserTopSellingProducts();
    List<MonthDataDto> getBiddingActivityOverTime();
    List<MonthDoubleDataDto> getAverageTransactionAmountOverTime();
    List<ProductCategoryRevenueDto> getRevenueByProductCategory();
}
