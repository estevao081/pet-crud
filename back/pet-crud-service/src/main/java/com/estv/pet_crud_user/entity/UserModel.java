package com.estv.pet_crud_user.entity;

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
    private UUID id;
    private String name;
    private String number;
    private String address;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<PetModel> pets;
}
