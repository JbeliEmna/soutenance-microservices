package com.microservices.jury_service.dto;

import com.microservices.jury_service.entity.RoleJury;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffecterJuryRequest {

    @NotNull(message = "idSoutenance est requis")
    private Long idSoutenance;

    @NotNull(message = "idEnseignant est requis")
    private Long idEnseignant;

    @NotNull(message = "roleJury est requis")
    private RoleJury roleJury; // "président", "rapporteur", "examinateur"
}
