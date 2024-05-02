package com.relic.relic.dto;

import com.relic.relic.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterCustomerDto {
    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "password must be provided")
    @NotBlank(message = "password cannot be empty")
    @Min(value = 6, message = "password must be at least 6 characters")
    private String password;

    private String role;
}
