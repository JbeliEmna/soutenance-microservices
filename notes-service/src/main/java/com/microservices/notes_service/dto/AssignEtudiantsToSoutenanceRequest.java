package com.microservices.notes_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AssignEtudiantsToSoutenanceRequest(
        @NotNull Long soutenanceId,
        @NotEmpty @Size(min = 1, max = 2) List<Long> etudiantIds
) {
}
