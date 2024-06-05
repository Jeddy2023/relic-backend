package com.relic.relic.service;

import com.relic.relic.enums.OtpType;
import com.relic.relic.model.User;

public interface OtpService {

    String create(User user);
    String create(User user, OtpType otpType);
    Boolean verify(User user, String token, OtpType otpType);
}
