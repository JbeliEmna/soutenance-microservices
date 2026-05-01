package com.microservices.notes_service.service;

import com.microservices.notes_service.client.AuthServiceClient;
import com.microservices.notes_service.dto.AuthUserResponse;
import com.microservices.notes_service.exception.BusinessException;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AuthStudentService {

    private final AuthServiceClient authServiceClient;

    public AuthStudentService(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    public Long resolveStudentIdFromAuth(Long externalId) {
        AuthUserResponse user;
        try {
            user = authServiceClient.getByExternalId(externalId);
        } catch (FeignException.NotFound ex) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Etudiant introuvable dans auth-service");
        }

        if (user == null || user.externalId() == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Etudiant introuvable dans auth-service");
        }
        if (!"ROLE_ETUDIANT".equals(user.role())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "L'utilisateur auth n'est pas un etudiant");
        }
        if (!user.enabled()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Etudiant desactive dans auth-service");
        }
        return user.externalId();
    }
}
