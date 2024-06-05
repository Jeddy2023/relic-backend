package com.relic.relic.controller;

import com.relic.relic.dto.*;
import com.relic.relic.service.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@CrossOrigin(value = "http://localhost:5173")
public class AuthController {
    private AuthService authService;

    @PostMapping("/register/customer")
    public ResponseEntity<RegisterResponseDto> registerCustomer(@RequestBody RegisterCustomerDto registerCustomerDto) {
        RegisterResponseDto message = authService.register(registerCustomerDto);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/register/vendor")
    public ResponseEntity<RegisterResponseDto> registerVendor(@RequestBody RegisterVendorDto registerVendorDto) {
        RegisterResponseDto message = authService.register(registerVendorDto);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> userLogin(@Valid @RequestBody LoginUserDto loginUserDto){
        return new ResponseEntity<>(authService.login(loginUserDto), HttpStatus.OK);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestBody VerifyEmailDto verifyEmailDto) {
        String response = authService.verifyEmail(verifyEmailDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@RequestBody String verificationToken) {
        String response = authService.resendOtp(verificationToken);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<UserDetailsDTO> getUserDetails(){
        UserDetailsDTO user = authService.getUserDetails();
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        List<UserDto> users = authService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PatchMapping("/update")
    public ResponseEntity<String> updateUserDetails(@Valid @RequestBody UpdateUserDetailsDto updateUserDetailsDto){
        String updatedUser = authService.updateUserDetails(updateUserDetailsDto);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}
