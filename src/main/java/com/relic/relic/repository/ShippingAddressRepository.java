package com.relic.relic.repository;

import com.relic.relic.model.ShippingAddress;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
    Optional<ShippingAddress> findByUser(User user);
}
