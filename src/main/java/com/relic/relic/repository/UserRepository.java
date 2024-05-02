package com.relic.relic.repository;

import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);
    Boolean existsByShopname(String shopname);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
