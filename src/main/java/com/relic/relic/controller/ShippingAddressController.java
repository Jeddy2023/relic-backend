package com.relic.relic.controller;

import com.relic.relic.dto.CreateShippingAddressDTO;
import com.relic.relic.service.ShippingAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shippingaddress")
@CrossOrigin(origins = "*")
public class ShippingAddressController {
    @Autowired
    ShippingAddressService shippingAddressService;

    @PostMapping
    public ResponseEntity<String> saveShippingAddress(@RequestBody CreateShippingAddressDTO createShippingAddressDTO) {
        String createdShippingAddress = shippingAddressService.saveAddress(createShippingAddressDTO);
        return new ResponseEntity<>(createdShippingAddress, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CreateShippingAddressDTO> getUserShippingAddress() {
        CreateShippingAddressDTO shippingAddress = shippingAddressService.getUserShippingAddress();
        return new ResponseEntity<>(shippingAddress, HttpStatus.CREATED);
    }
}
