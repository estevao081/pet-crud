package dev.estv.pet_crud_api.specification;

import dev.estv.pet_crud_api.dto.PetDTOs;
import dev.estv.pet_crud_api.entity.PetModel;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;

public class PetSpecification {

    public static Specification<PetModel> filter(PetDTOs.PetResponse dto) {

        return (root, query, cb) -> {

            var predicates = new ArrayList<Predicate>();

            if (hasValue(dto.getName())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + dto.getName().toLowerCase() + "%"
                        )
                );
            }

            if (hasValue(dto.getType())) {
                predicates.add(
                        cb.equal(
                                root.get("type"),
                                PetModel.Type.fromString(dto.getType())
                        )
                );
            }

            if (hasValue(dto.getGender())) {
                predicates.add(
                        cb.equal(
                                root.get("gender"),
                                PetModel.Gender.fromString(dto.getGender())
                        )
                );
            }

            if (hasValue(dto.getAge())) {
                predicates.add(
                        cb.equal(root.get("age"),
                                Integer.parseInt(dto.getAge()))
                );
            }

            if (hasValue(dto.getWeight())) {
                predicates.add(
                        cb.equal(root.get("weight"),
                                Integer.parseInt(dto.getWeight()))
                );
            }

            if (hasValue(dto.getCity())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("city")),
                                "%" + dto.getCity().toLowerCase() + "%"
                        )
                );
            }

            if (hasValue(dto.getState())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("state")),
                                "%" + dto.getState().toLowerCase() + "%"
                        )
                );
            }

            if (hasValue(dto.getRace())) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("race")),
                                "%" + dto.getRace().toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean hasValue(String value) {
        return value != null && !value.isBlank();
    }
}
