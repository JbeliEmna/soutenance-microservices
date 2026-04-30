package com.microservices.jury_service.dto;

public record SoutenanceSummaryDTO(
        Long id,
        Long etudiantId,
        Long encadrantId,
        String salle,
        String dateDebut,
        String dateFin,
        String etat
) {
}
