package com.relic.relic.service;

import com.relic.relic.dto.*;

import java.util.List;

public interface AuthService {
    String register(RegisterCustomerDto registerCustomerDto);
    String register(RegisterVendorDto registerVendorDto);
    LoginResponseDto login(LoginUserDto loginDto);
    UserDetailsDTO getUserDetails();
    String updateUserDetails(UpdateUserDetailsDto updateUserDetailsDto);
    List<UserDto> getAllUsers();
}
