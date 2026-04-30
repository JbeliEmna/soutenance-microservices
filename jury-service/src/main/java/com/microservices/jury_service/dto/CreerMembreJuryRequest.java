package com.microservices.jury_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreerMembreJuryRequest {

    @NotNull(message = "idEnseignant est requis")
    private Long idEnseignant;

    @NotBlank(message = "Le nom est requis")
    private String nom;

    @NotBlank(message = "Le prénom est requis")
    private String prenom;

    @NotBlank(message = "Le grade est requis")
    private String grade;

    @Email(message = "Email doit être valide")
    @NotBlank(message = "L'email est requis")
    private String email;
}
