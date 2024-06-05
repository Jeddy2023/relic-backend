package com.relic.relic.dto;

import lombok.Data;

@Data
public class VerifyEmailDto {
    private String otp;
    private String verificationToken;
}
