package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReferencePersonRequest(
        @NotNull Long id,
        @NotBlank String nomComplet
) {
}
