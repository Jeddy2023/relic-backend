package com.relic.relic.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
public class UpdateUserDetailsDto {
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String password;
    private String newpassword;
    private String phonenumber;
}
