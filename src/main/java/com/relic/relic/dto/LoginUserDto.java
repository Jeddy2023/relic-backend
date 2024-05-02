package com.relic.relic.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginUserDto {
    private String email;

    private String password;
}
