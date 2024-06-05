package com.relic.relic.service;

import com.relic.relic.dto.CreateShopDetailsDto;

public interface ShopDetailsService {
    String saveDetails(CreateShopDetailsDto createShopDetailsDto);
    CreateShopDetailsDto getUserShopDetails();
}
