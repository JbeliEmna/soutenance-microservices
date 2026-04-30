package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateSoutenanceRequest(
        @NotNull Long etudiantId,
        @NotNull Long encadrantId,
        @NotBlank String salle,
        @NotNull LocalDateTime dateDebut,
        @NotNull LocalDateTime dateFin
) {
}
