package com.relic.relic.service.implementations;

import com.relic.relic.dto.AdminUserDetailsDto;
import com.relic.relic.dto.SuspensionDto;
import com.relic.relic.dto.TotalAdminCountsDto;
import com.relic.relic.enums.Role;
import com.relic.relic.model.Suspension;
import com.relic.relic.model.User;
import com.relic.relic.repository.BiddingRepository;
import com.relic.relic.repository.ProductRepository;
import com.relic.relic.repository.SuspensionRepository;
import com.relic.relic.repository.UserRepository;
import com.relic.relic.service.AdminService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminServiceImple implements AdminService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final SuspensionRepository suspensionRepository;
    @Autowired
    private final BiddingRepository biddingRepository;
    private ModelMapper modelMapper;

    @Override
    public TotalAdminCountsDto getTotalCounts() {
        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalBids = biddingRepository.count();
        return new TotalAdminCountsDto(totalUsers, totalProducts, totalBids);
    }

    @Override
    public List<AdminUserDetailsDto> getAllUsers(
            Boolean isVerified,
            Boolean isBlocked,
            Role role,
            String keyword,
            String startDateStr,
            String endDateStr,
            int page,
            int size
    ) {
        List<User> users = userRepository.findAll();

        if (isVerified != null) {
            users = users.stream()
                    .filter(user -> user.isVerified() == isVerified)
                    .collect(Collectors.toList());
        }

        if (isBlocked != null) {
            users = users.stream()
                    .filter(user -> user.isBlocked() == isBlocked)
                    .collect(Collectors.toList());
        }

        if (role != null) {
            users = users.stream()
                    .filter(user -> user.getRole() == role)
                    .collect(Collectors.toList());
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerCaseKeyword = keyword.toLowerCase();
            users = users.stream()
                    .filter(user -> user.getFirstname().toLowerCase().contains(lowerCaseKeyword) ||
                            user.getLastname().toLowerCase().contains(lowerCaseKeyword) ||
                            user.getUsername().toLowerCase().contains(lowerCaseKeyword) ||
                            user.getEmail().toLowerCase().contains(lowerCaseKeyword) ||
                            (user.getShopname() != null && user.getShopname().toLowerCase().contains(lowerCaseKeyword)))
                    .collect(Collectors.toList());
        }

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS", Locale.ENGLISH);
            if (startDateStr != null && endDateStr != null) {
                Date startDate = dateFormat.parse(startDateStr);
                Date endDate = dateFormat.parse(endDateStr);
                users = users.stream()
                        .filter(user -> user.getCreatedAt().after(startDate) && user.getCreatedAt().before(endDate))
                        .collect(Collectors.toList());
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        users.sort(Comparator.comparing(User::getCreatedAt).reversed());

        return users.stream()
                .map(user -> modelMapper.map(user, AdminUserDetailsDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void blockUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(user -> {
            user.setBlocked(true);
            userRepository.save(user);
        });
    }

    @Override
    public void unBlockUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(user -> {
            user.setBlocked(false);
            userRepository.save(user);
        });
    }

    @Override
    public void makeUserAdmin(Long userId) {
        System.out.println("Yes");
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(user -> {
            user.setRole(Role.ADMIN);
            userRepository.save(user);
        });
    }

    @Override
    public void suspendUser(SuspensionDto suspensionDto) {
        Optional<User> userOptional = userRepository.findById(suspensionDto.getUserId());
        userOptional.ifPresent(user -> {
            Suspension suspension = new Suspension();
            suspension.setUser(user);
            suspension.setEndDate(suspensionDto.getEndDate());
            suspensionRepository.save(suspension);
            user.setBlocked(true);
            userRepository.save(user);
        });
    }

    @Override
    public void deleteUser(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(userRepository::delete);
    }

    @Transactional
    @Scheduled(fixedRate = 6000)
    public void checkSuspensionEndDates() {
        List<Suspension> suspensions = suspensionRepository.findAll();
        Date now = new Date();

        for (Suspension suspension : suspensions) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date endDate = dateFormat.parse(suspension.getEndDate());

                if (suspension.isSuspended() && now.after(endDate)) {
                    suspension.setSuspended(false);
                    suspension.getUser().setBlocked(false);
                    userRepository.save(suspension.getUser());
                    suspensionRepository.save(suspension);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
