package com.relic.relic.repository;

import com.relic.relic.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiddingRepository extends JpaRepository<Bid, Long> {

}
