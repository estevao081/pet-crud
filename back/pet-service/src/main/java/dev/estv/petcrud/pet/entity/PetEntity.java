package dev.estv.petcrud.pet.entity;

import dev.estv.petcrud.pet.exception.InvalidGenderException;
import dev.estv.petcrud.pet.exception.InvalidTypeException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Principal mudança em relação ao monólito: o dono deixou de ser um @ManyToOne para UserModel
 * e virou só o UUID do usuário. Não há foreign key entre bancos de serviços diferentes.
 * A coluna continua se chamando "user_id", então dados antigos migram sem renomear nada.
 */
@Entity
@Table(name = "pets", indexes = @Index(name = "idx_pets_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PetEntity {

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

    @Column(name = "user_id")
    private UUID ownerId;

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
