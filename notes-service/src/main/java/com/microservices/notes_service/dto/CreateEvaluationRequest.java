package com.microservices.notes_service.dto;

import com.microservices.notes_service.enums.RoleJury;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateEvaluationRequest(
        @NotNull Long soutenanceId,
        @NotNull Long enseignantId,
        @NotNull RoleJury roleJury,
        @NotNull @DecimalMin("0.0") @DecimalMax("20.0") Double note
) {
}
