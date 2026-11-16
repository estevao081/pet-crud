package com.estv.pet_crud_auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class UserCredential {

    @Id
    @GeneratedValue
    private UUID id;
    private UUID userId;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}
