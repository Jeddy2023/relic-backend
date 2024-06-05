package com.relic.relic.repository;

import com.relic.relic.model.AuditTrial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditTrialRepository extends JpaRepository<AuditTrial, Long> {
}
