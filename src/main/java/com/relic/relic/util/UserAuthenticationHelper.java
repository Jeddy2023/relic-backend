package com.relic.relic.util;

import com.relic.relic.exception.BiddingUnauthorizedException;
import com.relic.relic.exception.ResourceNotFoundException;
import com.relic.relic.model.User;
import com.relic.relic.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserAuthenticationHelper {

    @Autowired
    private UserRepository userRepository;

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BiddingUnauthorizedException("Login to access this resource");
        }
        String name = authentication.getName();
        return userRepository.findByUsernameOrEmail(name, name)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
