package com.relic.relic.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateShippingAddressDTO {
    private String streetAddress;
    private String city;
    private String state;
    private String country;
    private String firstname;
    private String lastname;
}
