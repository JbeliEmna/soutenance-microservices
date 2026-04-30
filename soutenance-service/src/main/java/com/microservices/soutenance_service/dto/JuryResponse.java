package com.microservices.soutenance_service.dto;

import com.microservices.soutenance_service.model.Jury;

import java.time.LocalDateTime;

public record JuryResponse(
        Long id,
        Long soutenanceId,
        Long presidentId,
        Long rapporteurId,
        Long examinateurId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JuryResponse fromEntity(Jury jury) {
        return new JuryResponse(
                jury.getId(),
                jury.getSoutenanceId(),
                jury.getPresidentId(),
                jury.getRapporteurId(),
                jury.getExaminateurId(),
                jury.getCreatedAt(),
                jury.getUpdatedAt()
        );
    }
}
