package com.microservices.notes_service.dto;

public record JuryAffectationFeignResponse(
        String id,
        Long idSoutenance,
        Long idEnseignant,
        String roleJury,
        String nomEnseignant,
        String prenomEnseignant
) {
}
