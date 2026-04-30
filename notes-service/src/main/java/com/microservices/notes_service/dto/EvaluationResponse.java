package com.microservices.notes_service.dto;

import com.microservices.notes_service.enums.RoleJury;
import com.microservices.notes_service.model.Evaluation;

public record EvaluationResponse(
        Long id,
        Long soutenanceId,
        Long enseignantId,
        RoleJury roleJury,
        Double note
) {
    public static EvaluationResponse fromEntity(Evaluation evaluation) {
        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getSoutenanceId(),
                evaluation.getEnseignantId(),
                evaluation.getRoleJury(),
                evaluation.getNote()
        );
    }
}
