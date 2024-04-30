package com.relic.relic.model;

import com.relic.relic.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstname;

    private String lastname;

    private String email;

    private String username;

    private String password;

    private String shopname;

    private String phonenumber;

    @Column(length = 1024)
    private String shopurl;

    @Enumerated(EnumType.STRING)
    private Role role;

    private Date createdAt = new Date();

    private Date lastUpdatedTimestamp = new Date();
}
