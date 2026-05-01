package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.client.AuthServiceClient;
import com.microservices.soutenance_service.dto.AuthUserResponse;
import com.microservices.soutenance_service.dto.ReferencePersonRequest;
import com.microservices.soutenance_service.dto.ReferencePersonResponse;
import com.microservices.soutenance_service.exception.BusinessException;
import com.microservices.soutenance_service.model.EncadrantRef;
import com.microservices.soutenance_service.model.StudentRef;
import com.microservices.soutenance_service.repository.EncadrantRefRepository;
import com.microservices.soutenance_service.repository.StudentRefRepository;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReferenceDataService {

    /*
     * Student and teacher data is resolved from auth-service through OpenFeign.
     * Local StudentRef/EncadrantRef collections are legacy compatibility data only.
     */
    private final StudentRefRepository studentRefRepository;
    private final EncadrantRefRepository encadrantRefRepository;
    private final AuthServiceClient authServiceClient;

    public ReferenceDataService(
            StudentRefRepository studentRefRepository,
            EncadrantRefRepository encadrantRefRepository,
            AuthServiceClient authServiceClient
    ) {
        this.studentRefRepository = studentRefRepository;
        this.encadrantRefRepository = encadrantRefRepository;
        this.authServiceClient = authServiceClient;
    }

    public ReferencePersonResponse createStudent(ReferencePersonRequest request) {
        if (studentRefRepository.existsById(request.id())) {
            throw new BusinessException(HttpStatus.CONFLICT, "Cet etudiant existe deja");
        }

        StudentRef studentRef = new StudentRef();
        studentRef.setId(request.id());
        studentRef.setNomComplet(request.nomComplet());
        StudentRef saved = studentRefRepository.save(studentRef);
        return new ReferencePersonResponse(saved.getId(), saved.getNomComplet());
    }

    public ReferencePersonResponse createEncadrant(ReferencePersonRequest request) {
        return createTeacher(request, "Cet encadrant existe deja");
    }

    public ReferencePersonResponse createEnseignant(ReferencePersonRequest request) {
        return createTeacher(request, "Cet enseignant existe deja");
    }

    public boolean studentExists(Long id) {
        return authUserWithRoleExists(id, "ROLE_ETUDIANT");
    }

    public boolean encadrantExists(Long id) {
        return authUserWithRoleExists(id, "ROLE_ENSEIGNANT");
    }

    public boolean enseignantExists(Long id) {
        return authUserWithRoleExists(id, "ROLE_ENSEIGNANT");
    }

    public Long resolveStudentIdFromAuth(Long requestedExternalId) {
        return getAuthUserWithRole(requestedExternalId, "ROLE_ETUDIANT", "L'etudiant n'existe pas").externalId();
    }

    public Long resolveEncadrantIdFromAuth(Long requestedExternalId) {
        return getAuthUserWithRole(requestedExternalId, "ROLE_ENSEIGNANT", "L'encadrant n'existe pas").externalId();
    }

    public Long resolveEnseignantIdFromAuth(Long requestedExternalId) {
        return getAuthUserWithRole(requestedExternalId, "ROLE_ENSEIGNANT", "L'enseignant n'existe pas").externalId();
    }

    public List<ReferencePersonResponse> listStudents() {
        return studentRefRepository.findAll()
                .stream()
                .map(student -> new ReferencePersonResponse(student.getId(), student.getNomComplet()))
                .toList();
    }

    public List<ReferencePersonResponse> listEncadrants() {
        return encadrantRefRepository.findAll()
                .stream()
                .map(encadrant -> new ReferencePersonResponse(encadrant.getId(), encadrant.getNomComplet()))
                .toList();
    }

    public List<ReferencePersonResponse> listEnseignants() {
        return listEncadrants();
    }

    private ReferencePersonResponse createTeacher(ReferencePersonRequest request, String conflictMessage) {
        if (encadrantRefRepository.existsById(request.id())) {
            throw new BusinessException(HttpStatus.CONFLICT, conflictMessage);
        }

        EncadrantRef encadrantRef = new EncadrantRef();
        encadrantRef.setId(request.id());
        encadrantRef.setNomComplet(request.nomComplet());
        EncadrantRef saved = encadrantRefRepository.save(encadrantRef);
        return new ReferencePersonResponse(saved.getId(), saved.getNomComplet());
    }

    private boolean authUserWithRoleExists(Long externalId, String role) {
        try {
            AuthUserResponse user = getAuthUserWithRole(externalId, role, "Utilisateur introuvable");
            return user != null;
        } catch (FeignException.NotFound ex) {
            return false;
        } catch (BusinessException ex) {
            return false;
        }
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
