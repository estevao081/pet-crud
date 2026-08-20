package dev.estv.pet_crud_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String number;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<PetModel> pets;

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}
