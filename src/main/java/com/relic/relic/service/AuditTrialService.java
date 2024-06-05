package com.relic.relic.service;

import com.relic.relic.model.AuditTrial;
import com.relic.relic.model.User;

import java.util.List;

public interface AuditTrialService {
    List<AuditTrial> getAllAuditTrials(
            Long userId,
            String action,
            String audit,
            String startDate,
            String endDate,
            String sortBy
    );
    void addToAuditTrial(User user, String action, String audit);
}
