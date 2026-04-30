package com.microservices.soutenance_service.service;

import com.microservices.soutenance_service.client.AuthServiceClient;
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
     * User existence checks are intentionally delegated to auth-service.
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
        return authUserExistsWithRole(id, "ROLE_ETUDIANT");
    }

    public boolean encadrantExists(Long id) {
        return authUserExistsWithRole(id, "ROLE_ENSEIGNANT");
    }

    public boolean enseignantExists(Long id) {
        return authUserExistsWithRole(id, "ROLE_ENSEIGNANT");
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

    private boolean authUserExistsWithRole(Long externalId, String role) {
        try {
            Boolean exists = authServiceClient.existsByExternalIdAndRole(externalId, role);
            return Boolean.TRUE.equals(exists);
        } catch (FeignException.NotFound ex) {
            return false;
        }
    }
}
