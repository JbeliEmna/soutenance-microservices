package com.microservices.auth_service.dto;

import com.microservices.auth_service.entity.Role;
import com.microservices.auth_service.entity.User;

public record UserInternalResponse(
        String id,
        Long externalId,
        String email,
        String nom,
        String prenom,
        Role role,
        boolean enabled
) {
    public static UserInternalResponse fromEntity(User user) {
        return new UserInternalResponse(
                user.getId(),
                user.getExternalId(),
                user.getEmail(),
                user.getNom(),
                user.getPrenom(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
