package com.microservices.soutenance_service.dto;

import com.microservices.soutenance_service.enums.EtatSoutenance;
import com.microservices.soutenance_service.model.Soutenance;

import java.time.LocalDateTime;
import java.util.List;

public record SoutenanceResponse(
        Long id,
        List<Long> etudiantIds,
        Long encadrantId,
        String salle,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        EtatSoutenance etat,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SoutenanceResponse fromEntity(Soutenance soutenance) {
        return new SoutenanceResponse(
                soutenance.getId(),
                soutenance.getEtudiantIds(),
                soutenance.getEncadrantId(),
                soutenance.getSalle(),
                soutenance.getDateDebut(),
                soutenance.getDateFin(),
                soutenance.getEtat(),
                soutenance.getCreatedAt(),
                soutenance.getUpdatedAt()
        );
    }
}
