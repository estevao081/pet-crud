package dev.estv.pet_crud_api.entity;

import dev.estv.pet_crud_api.exception.exceptions.InvalidGenderException;
import dev.estv.pet_crud_api.exception.exceptions.InvalidTypeException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetModel {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String age;

    @Column(nullable = false)
    private String weight;

    @Column(nullable = false)
    private String race;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserModel owner;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String imageUrl;

    public enum Type {
        CAO, GATO;

        public static Type fromString(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }

            return switch (input.trim().toUpperCase()) {
                case "CÃO" -> CAO;
                case "GATO" -> GATO;
                default -> throw new InvalidTypeException();
            };
        }
    }

    public enum Gender {
        F, M;

        public static Gender fromString(String input) {
            if (input == null || input.isBlank()) {
                return null;
            }

            return switch (input.trim().toUpperCase()) {
                case "M" -> M;
                case "F" -> F;
                default -> throw new InvalidGenderException();
            };
        }
    }
}
