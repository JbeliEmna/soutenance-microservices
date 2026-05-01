package com.microservices.jury_service.dto;

import java.util.List;

public record SoutenanceSummaryDTO(
        Long id,
        List<Long> etudiantIds,
        Long encadrantId,
        String salle,
        String dateDebut,
        String dateFin,
        String etat
) {
}
