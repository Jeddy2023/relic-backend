package com.relic.relic.service.implementations;

import com.relic.relic.dto.*;
import com.relic.relic.exception.*;
import com.relic.relic.model.User;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.security.JwtTokenProvider;
import com.relic.relic.service.AuthService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class AuthServiceImple implements AuthService {
    private UserRepository userRepository;
    private JwtTokenProvider jwtTokenProvider;
    private PasswordEncoder passwordEncoder;
    private AuthenticationManager authenticationManager;
    private ModelMapper modelMapper;

    public AuthServiceImple(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    @Override
    public String register(RegisterCustomerDto registerCustomerDto) {
        if(userRepository.existsByEmail(registerCustomerDto.getEmail())){
            throw new AlreadyExistsException("Email already exists.");
        }
        if(registerCustomerDto.getEmail().isEmpty() || registerCustomerDto.getPassword().isEmpty()){
            throw new CustomException("Invalid email or password.");
        }

        User user = new User();
        user.setEmail(registerCustomerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerCustomerDto.getPassword()));
        user.setRole(registerCustomerDto.getRole());
        userRepository.save(user);
        return "You have been registered successfully, Verify your otp then login to continue";
    }

    @Override
    public String register(RegisterVendorDto registerVendorDto) {
        if(userRepository.existsByEmail(registerVendorDto.getEmail())){
            throw new AlreadyExistsException("Email already exists.");
        }
        if(registerVendorDto.getEmail().isEmpty() || registerVendorDto.getPassword().isEmpty()){
            throw new CustomException("Invalid email or password.");
        }
        if(registerVendorDto.getFirstname().isEmpty() || registerVendorDto.getLastname().isEmpty() || registerVendorDto.getUsername().isEmpty() || registerVendorDto.getShopname().isEmpty()){
            throw new CustomException("Invalid firstname, lastname, username or shopname.");
        }
        if(userRepository.existsByUsername(registerVendorDto.getUsername())){
            throw new AlreadyExistsException("Username already exists");
        }
        User user = new User();
        user.setEmail(registerVendorDto.getEmail());
        user.setRole(registerVendorDto.getRole());
        user.setPassword(passwordEncoder.encode(registerVendorDto.getPassword()));
        user.setLastname(registerVendorDto.getLastname());
        user.setFirstname(registerVendorDto.getFirstname());
        user.setShopname(registerVendorDto.getShopname());
        user.setUsername(registerVendorDto.getUsername());
        userRepository.save(user);
        return "You have been registered successfully, Verify your otp then login to continue";
    }

    @Override
    public LoginResponseDto login(LoginUserDto loginUserDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            User loggedUser = getLoggedInUser();
            UserDto userDto = modelMapper.map(loggedUser, UserDto.class);
            return new LoginResponseDto(userDto,accessToken,"Login Succesful");
        } catch (AuthenticationException e) {
            throw new BiddingUnauthorizedException("Invalid email or password");
        } catch (NoSuchElementException e) {
            throw new BiddingUnauthorizedException("User not found");
        }
    }

    @Override
    public UserDetailsDTO getUserDetails() {
        User loggedUser = getLoggedInUser();
        loggedUser.getEmail();
        loggedUser.getLastname();
        loggedUser.getPhonenumber();
        loggedUser.getFirstname();
        loggedUser.getUsername();
        return modelMapper.map(loggedUser, UserDetailsDTO.class);
    }

    @Override
    public String updateUserDetails(UpdateUserDetailsDto updateUserDetailsDto) {
        User loggedUser = getLoggedInUser();
        if (!updateUserDetailsDto.getPassword().isEmpty()) {
            if(updateUserDetailsDto.getNewpassword().isEmpty()){
                throw new CustomException("Please provide a new password");
            }
            String newPasswordEncoded = passwordEncoder.encode(updateUserDetailsDto.getNewpassword());

            if (!passwordEncoder.matches(updateUserDetailsDto.getPassword(), loggedUser.getPassword())) {
                throw new CustomException("Current Password is Incorrect");
            }

            loggedUser.setPassword(newPasswordEncoded);
        }
        loggedUser.setUsername(updateUserDetailsDto.getUsername());
        loggedUser.setLastname(updateUserDetailsDto.getLastname());
        loggedUser.setFirstname(updateUserDetailsDto.getFirstname());
        loggedUser.setPhonenumber(updateUserDetailsDto.getPhonenumber());
        loggedUser.setEmail(updateUserDetailsDto.getEmail());
        userRepository.save(loggedUser);
        return "User details updated successfully";
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());
    }

    private User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BiddingUnauthorizedException("User is not authenticated");
        }
        String name = authentication.getName();
        return userRepository.findByUsernameOrEmail(name, name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
