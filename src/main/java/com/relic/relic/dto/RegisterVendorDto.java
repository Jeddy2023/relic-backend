package com.relic.relic.dto;

import com.relic.relic.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RegisterVendorDto {
    @NotNull(message = "firstname must be provided")
    @NotBlank(message = "firstname cannot be empty")
    private String firstname;

    @NotNull(message = "lastname must be provided")
    @NotBlank(message = "lastname cannot be empty")
    private String lastname;

    @NotNull(message = "username must be provided")
    @NotBlank(message = "username cannot be empty")
    private String username;

    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "password must be provided")
    @NotBlank(message = "password cannot be empty")
    @Min(value = 6, message = "password must be at least 6 characters")
    private String password;

    @NotNull(message = "shopname must be provided")
    @NotBlank(message = "shopname cannot be empty")
    private String shopname;

    @NotNull(message = "role must be provided")
    private String role;

    private String phonenumber;
}
