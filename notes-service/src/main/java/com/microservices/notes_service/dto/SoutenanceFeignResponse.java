package com.microservices.notes_service.dto;

import java.util.List;

public record SoutenanceFeignResponse(
        Long id,
        List<Long> etudiantIds,
        Long encadrantId,
        String salle,
        String dateDebut,
        String dateFin,
        String etat
) {
}
