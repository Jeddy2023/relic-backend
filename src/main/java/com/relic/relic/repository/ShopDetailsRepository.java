package com.relic.relic.repository;

import com.relic.relic.model.ShopDetails;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShopDetailsRepository extends JpaRepository<ShopDetails, Long> {
    Optional<ShopDetails> findByUser(User user);
    Boolean existsByEmail(String email);
    Boolean existsByShopname(String shopname);
}
