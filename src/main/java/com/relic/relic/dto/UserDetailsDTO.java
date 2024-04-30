package com.relic.relic.dto;

import com.relic.relic.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsDTO {
    private String firstname;
    private String lastname;
    private String username;
    private String email;
    private String phonenumber;
}
