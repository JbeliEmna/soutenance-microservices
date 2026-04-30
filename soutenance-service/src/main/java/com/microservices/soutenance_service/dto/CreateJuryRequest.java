package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotNull;

public record CreateJuryRequest(
        @NotNull Long soutenanceId,
        @NotNull Long presidentId,
        @NotNull Long rapporteurId,
        @NotNull Long examinateurId
) {
}
