package com.microservices.soutenance_service.dto;

import com.microservices.soutenance_service.model.Salle;

import java.time.LocalDateTime;

public record SalleResponse(
        Long id,
        String nom,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static SalleResponse fromEntity(Salle salle) {
        return new SalleResponse(
                salle.getId(),
                salle.getNom(),
                salle.getCreatedAt(),
                salle.getUpdatedAt()
        );
    }
}
