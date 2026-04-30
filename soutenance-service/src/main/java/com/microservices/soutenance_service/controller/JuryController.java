package com.microservices.soutenance_service.controller;

import com.microservices.soutenance_service.dto.CreateJuryRequest;
import com.microservices.soutenance_service.dto.JuryResponse;
import com.microservices.soutenance_service.dto.UpdateJuryRequest;
import com.microservices.soutenance_service.service.JuryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/juries")
public class JuryController {

    private final JuryService juryService;

    public JuryController(JuryService juryService) {
        this.juryService = juryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JuryResponse create(@Valid @RequestBody CreateJuryRequest request) {
        return juryService.create(request);
    }

    @PutMapping("/{id}")
    public JuryResponse update(@PathVariable Long id, @Valid @RequestBody UpdateJuryRequest request) {
        return juryService.update(id, request);
    }

    @GetMapping("/{id}")
    public JuryResponse getById(@PathVariable Long id) {
        return juryService.getById(id);
    }

    @GetMapping("/soutenance/{soutenanceId}")
    public JuryResponse getBySoutenanceId(@PathVariable Long soutenanceId) {
        return juryService.getBySoutenanceId(soutenanceId);
    }

    @GetMapping
    public List<JuryResponse> listAll() {
        return juryService.listAll();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        juryService.delete(id);
    }
}
