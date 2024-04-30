package com.relic.relic.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoginUserDto {
    @NotNull(message = "Username or Email must be provided")
    @NotBlank(message = "Username and Email cannot be empty")
    private String email;

    @NotNull(message = "password must be provided")
    @NotBlank(message = "password cannot be empty")
    @Size(min = 6, max = 40, message = "password must be up to 6 characters")
    private String password;
}
