package com.microservices.soutenance_service.dto;

import com.microservices.soutenance_service.enums.EtatSoutenance;
import jakarta.validation.constraints.NotNull;

public record UpdateEtatSoutenanceRequest(
        @NotNull EtatSoutenance etat
) {
}
