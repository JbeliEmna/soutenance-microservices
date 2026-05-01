package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateSoutenanceRequest(
        @NotEmpty @Size(min = 1, max = 2) List<Long> etudiantIds,
        @NotNull Long encadrantId,
        @NotBlank String salle,
        @NotNull LocalDateTime dateDebut,
        @NotNull LocalDateTime dateFin
) {
}
