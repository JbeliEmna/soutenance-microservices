package com.microservices.notes_service.dto;

public record SoutenanceFeignResponse(
        Long id,
        Long etudiantId,
        Long encadrantId,
        String salle,
        String dateDebut,
        String dateFin,
        String etat
) {
}
