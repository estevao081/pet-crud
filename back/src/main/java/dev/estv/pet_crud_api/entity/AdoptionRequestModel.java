package dev.estv.pet_crud_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "adoption_requests")
@Getter
@Setter
public class AdoptionRequestModel {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private PetModel pet;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private UserModel owner;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private UserModel requester;

    // Snapshot dos dados de contato de quem solicitou a adoção,
    // para que a notificação recebida pelo dono do pet preserve essas
    // informações mesmo que o solicitante altere seu perfil depois.
    @Column(nullable = false)
    private String requesterName;

    @Column(nullable = false)
    private String requesterPhone;

    @Column(nullable = false)
    private String requesterAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    public enum Status {
        PENDING, ACCEPTED, REJECTED
    }
}
