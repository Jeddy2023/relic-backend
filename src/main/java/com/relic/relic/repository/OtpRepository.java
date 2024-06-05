package com.relic.relic.repository;

import com.relic.relic.enums.OtpType;
import com.relic.relic.model.Otp;
import com.relic.relic.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {
    void deleteAllByUser(User user);

    void deleteAllByUserAndOtpType(User user, OtpType otpType);
    Optional<Otp> findByUserAndTokenAndOtpType(User user, String token, OtpType otpType);
}
