package dev.estv.pet_crud_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public class PetDTOs {

    public record PetRecord(
            @NotBlank(message = "Name is required")
            String name,

            @NotNull(message = "Type is required")
            String type,

            @NotNull(message = "Gender is required")
            String gender,

            @NotNull(message = "City is required")
            String city,

            @NotNull(message = "City is required")
            String state,

            String age,
            String weight,

            @Pattern(
                    regexp = "^[A-Za-zÀ-ÿ\\s]*$",
                    message = "Race must have letters only"
            )
            String race
    ) {}

    @Setter
    @Getter
    public static class PetResponse {

        private String id;
        private String name;
        private String type;
        private String gender;
        private String city;
        private String state;
        private String age;
        private String weight;
        private String race;
        private dev.estv.pet_crud_api.dto.PetDTOs.PetResponse.OwnerDTO owner;
        private String imageUrl;

        @Setter
        @Getter
        public static class OwnerDTO {
            private String name;
            private long number;
        }
    }
}
