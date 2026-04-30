package com.microservices.soutenance_service.dto;

public record JuryAffectationResponse(
        String id,
        Long idSoutenance,
        Long idEnseignant,
        String roleJury,
        String dateAffectation,
        String nomEnseignant,
        String prenomEnseignant
) {
}
