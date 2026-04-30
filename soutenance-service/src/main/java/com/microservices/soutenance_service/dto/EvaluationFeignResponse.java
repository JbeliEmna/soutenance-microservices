package com.microservices.soutenance_service.dto;

public record EvaluationFeignResponse(
        Long id,
        Long soutenanceId,
        Long enseignantId,
        String roleJury,
        Double note
) {
}
