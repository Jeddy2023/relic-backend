package com.relic.relic.repository;

import com.relic.relic.model.ShopDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopAddressRepository extends JpaRepository<ShopDetails, Long> {
}
