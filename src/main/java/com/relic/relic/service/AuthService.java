package com.relic.relic.service;

import com.relic.relic.dto.*;

import java.util.List;

public interface AuthService {
    RegisterResponseDto register(RegisterCustomerDto registerCustomerDto);
    RegisterResponseDto register(RegisterVendorDto registerVendorDto);
    LoginResponseDto login(LoginUserDto loginDto);
    UserDetailsDTO getUserDetails();
    String updateUserDetails(UpdateUserDetailsDto updateUserDetailsDto);
    List<UserDto> getAllUsers();
    String verifyEmail(VerifyEmailDto verifyEmailDto);
    String resendOtp(String verificationToken);
}
