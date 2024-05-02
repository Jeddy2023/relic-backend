package com.relic.relic.service;

import com.relic.relic.dto.CreateShippingAddressDTO;

public interface ShippingAddressService {
    String saveAddress(CreateShippingAddressDTO createShippingAddressDTO);
    CreateShippingAddressDTO getUserShippingAddress();
}
