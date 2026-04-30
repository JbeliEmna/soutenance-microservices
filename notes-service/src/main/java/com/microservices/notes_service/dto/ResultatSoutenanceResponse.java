package com.microservices.notes_service.dto;

import com.microservices.notes_service.enums.MentionFinale;
import com.microservices.notes_service.model.ResultatSoutenance;

import java.util.List;

public record ResultatSoutenanceResponse(
        Long soutenanceId,
        Double noteFinale,
        MentionFinale mention,
        List<Long> etudiantIds
) {
    public static ResultatSoutenanceResponse fromEntity(ResultatSoutenance resultat, List<Long> etudiantIds) {
        return new ResultatSoutenanceResponse(
                resultat.getSoutenanceId(),
                resultat.getNoteFinale(),
                resultat.getMention(),
                etudiantIds
        );
    }
}
