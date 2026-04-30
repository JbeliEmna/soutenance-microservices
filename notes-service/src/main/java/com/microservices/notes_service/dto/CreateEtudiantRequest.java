package com.microservices.notes_service.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEtudiantRequest(
        @NotBlank String matricule,
        @NotBlank String nom,
        @NotBlank String prenom
) {
}
