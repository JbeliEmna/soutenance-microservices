package com.microservices.soutenance_service.dto;

import java.util.List;

public record SoutenanceDetailsResponse(
        SoutenanceResponse soutenance,
        List<JuryAffectationResponse> jury,
        List<EvaluationFeignResponse> evaluations,
        ResultatFeignResponse resultat
) {
}
