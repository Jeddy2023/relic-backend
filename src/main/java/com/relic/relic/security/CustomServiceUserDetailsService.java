package com.relic.relic.security;

import com.relic.relic.enums.Role;
import com.relic.relic.model.User;
import com.relic.relic.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomServiceUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    public CustomServiceUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No User found with: " + usernameOrEmail));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );


//        Role userRole = user.getRole();
//        SimpleGrantedAuthority authorities = new SimpleGrantedAuthority(userRole.toString());
//        System.out.println(userRole);
//
//
//
//        return new org.springframework.security.core.userdetails.User(user.getEmail()
//                ,user.getPassword(), Collections.singleton(authorities));
    }


}
