package com.relic.relic.service;

import com.relic.relic.dto.AdminUserDetailsDto;
import com.relic.relic.dto.SuspensionDto;
import com.relic.relic.dto.TotalAdminCountsDto;
import com.relic.relic.enums.Role;

import java.util.List;

public interface AdminService {
    TotalAdminCountsDto getTotalCounts();
    List<AdminUserDetailsDto> getAllUsers(
            Boolean isVerified,
            Boolean isBlocked,
            Role role,
            String keyword,
            String startDateStr,
            String endDateStr,
            int page,
            int size
    );
    void blockUser(Long userId);
    void unBlockUser(Long userId);
    void makeUserAdmin(Long userId);
    void suspendUser(SuspensionDto suspensionDto);
    void deleteUser(Long userId);
}
