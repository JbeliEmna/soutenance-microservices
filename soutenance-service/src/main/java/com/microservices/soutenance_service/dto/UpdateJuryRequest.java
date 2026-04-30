package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateJuryRequest(
        @NotNull Long soutenanceId,
        @NotNull Long presidentId,
        @NotNull Long rapporteurId,
        @NotNull Long examinateurId
) {
}
