package dev.estv.petcrud.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Dono dos dados de usuário. Diferente do monólito, NÃO existe mais a lista "pets"
 * (@OneToMany): pets vivem em outro serviço/banco e referenciam o usuário só pelo id.
 */
@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;
    private String number;

    @Column(unique = true) // antes dependia só do "exists" na aplicação; agora o banco garante
    private String email;

    private String password; // hash BCrypt, gerado pelo auth-service

    @Enumerated(EnumType.STRING)
    private Role role;

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN
    }
}
