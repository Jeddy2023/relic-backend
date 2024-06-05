package com.relic.relic.controller;

import com.relic.relic.dto.CreateShopDetailsDto;
import com.relic.relic.service.ShopDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shop_details")
@CrossOrigin(origins = "*")
public class ShopDetailsController {
    @Autowired
    private ShopDetailsService shopDetailsService;

    @PostMapping
    public ResponseEntity<String> saveShopDetails(@RequestBody CreateShopDetailsDto createShopDetailsDto) {
        String saveShopDetails = shopDetailsService.saveDetails(createShopDetailsDto);
        return new ResponseEntity<>(saveShopDetails, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<CreateShopDetailsDto> getUserShopDetails() {
        CreateShopDetailsDto shopDetails = shopDetailsService.getUserShopDetails();
        return new ResponseEntity<>(shopDetails, HttpStatus.OK);
    }
}
