package com.relic.relic.repository;

import com.relic.relic.model.ShippingAddress;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    Optional<ShippingAddress> findByUser(User user);
}
