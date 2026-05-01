package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.client.AuthServiceClient;
import com.microservices.soutenance_service.dto.AuthUserResponse;
import com.microservices.soutenance_service.exception.BusinessException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ReferenceDataService {

    private final AuthServiceClient authServiceClient;

    public ReferenceDataService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public Long resolveStudentIdFromAuth(Long requestedExternalId) {
        return getAuthUserWithRole(requestedExternalId, "ROLE_ETUDIANT", "L'etudiant n'existe pas").externalId();
    }

    public Long resolveEncadrantIdFromAuth(Long requestedExternalId) {
        return getAuthUserWithRole(requestedExternalId, "ROLE_ENSEIGNANT", "L'encadrant n'existe pas").externalId();
    }

    private AuthUserResponse getAuthUserWithRole(Long externalId, String role, String notFoundMessage) {
        AuthUserResponse user;
        try {
            user = authServiceClient.getByExternalId(externalId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, notFoundMessage);
        }

        if (user == null || user.externalId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, notFoundMessage);
        }
        if (!role.equals(user.role())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, notFoundMessage);
        }
        if (!user.enabled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Utilisateur desactive dans auth-service");
        }
        return user;
    }
}
