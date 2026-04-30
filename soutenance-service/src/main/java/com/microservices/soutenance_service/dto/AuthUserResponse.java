package com.microservices.soutenance_service.dto;

public record AuthUserResponse(
        String id,
        Long externalId,
        String email,
        String nom,
        String prenom,
        String role,
        boolean enabled
) {
}
