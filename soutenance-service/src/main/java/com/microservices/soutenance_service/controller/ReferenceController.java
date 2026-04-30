package com.microservices.soutenance_service.controller;

import com.microservices.soutenance_service.dto.ReferencePersonRequest;
import com.microservices.soutenance_service.dto.ReferencePersonResponse;
import com.microservices.soutenance_service.service.ReferenceDataService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/references")
@Deprecated
public class ReferenceController {

    /*
     * Legacy endpoints kept only for backward compatibility with old data.
     * The real source of truth for students, supervisors and teachers is now auth-service,
     * queried from ReferenceDataService through OpenFeign by externalId and role.
     */
    private final ReferenceDataService referenceDataService;

    public ReferenceController(ReferenceDataService referenceDataService) {
        this.referenceDataService = referenceDataService;
    }

    @PostMapping("/etudiants")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferencePersonResponse createStudent(@Valid @RequestBody ReferencePersonRequest request) {
        return referenceDataService.createStudent(request);
    }

    @PostMapping("/encadrants")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferencePersonResponse createEncadrant(@Valid @RequestBody ReferencePersonRequest request) {
        return referenceDataService.createEncadrant(request);
    }

    @PostMapping("/enseignants")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferencePersonResponse createEnseignant(@Valid @RequestBody ReferencePersonRequest request) {
        return referenceDataService.createEnseignant(request);
    }

    @GetMapping("/etudiants")
    public List<ReferencePersonResponse> listStudents() {
        return referenceDataService.listStudents();
    }

    @GetMapping("/encadrants")
    public List<ReferencePersonResponse> listEncadrants() {
        return referenceDataService.listEncadrants();
    }

    @GetMapping("/enseignants")
    public List<ReferencePersonResponse> listEnseignants() {
        return referenceDataService.listEnseignants();
    }
}
