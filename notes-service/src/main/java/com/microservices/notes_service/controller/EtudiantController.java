package com.microservices.notes_service.controller;

import com.microservices.notes_service.dto.CreateEtudiantRequest;
import com.microservices.notes_service.dto.EtudiantResponse;
import com.microservices.notes_service.service.EtudiantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/etudiants")
public class EtudiantController {

    private final EtudiantService etudiantService;

    public EtudiantController(EtudiantService etudiantService) {
        this.etudiantService = etudiantService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EtudiantResponse create(@Valid @RequestBody CreateEtudiantRequest request) {
        return etudiantService.create(request);
    }

    @GetMapping("/{id}")
    public EtudiantResponse getById(@PathVariable Long id) {
        return etudiantService.getById(id);
    }

    @GetMapping
    public List<EtudiantResponse> listAll() {
        return etudiantService.listAll();
    }
}
