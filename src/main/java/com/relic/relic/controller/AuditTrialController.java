package com.relic.relic.controller;

import com.relic.relic.model.AuditTrial;
import com.relic.relic.service.AuditTrialService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping("/audit")
public class AuditTrialController {
    private final AuditTrialService auditTrialService;

    public AuditTrialController(AuditTrialService auditTrialService) {
        this.auditTrialService = auditTrialService;
    }

    @GetMapping("/all")
    public ResponseEntity<Page<AuditTrial>> getAllAuditTrials(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String audit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "Newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<AuditTrial> auditTrials = auditTrialService.getAllAuditTrials(userId, action, audit, startDate, endDate, sortBy);

        int start = Math.min(page * size, auditTrials.size());
        int end = Math.min((page + 1) * size, auditTrials.size());
        Pageable pageable = PageRequest.of(page, size);
        Page<AuditTrial> auditTrialPage = new PageImpl<>(auditTrials.subList(start, end), pageable, auditTrials.size());

        return new ResponseEntity<>(auditTrialPage, HttpStatus.OK);
    }
}
