package com.microservices.soutenance_service.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateSalleRequest(
        @NotBlank String nom
) {
}
