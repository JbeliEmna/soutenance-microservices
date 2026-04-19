package com.microservices.jury_service.dto;

import com.microservices.jury_service.entity.RoleJury;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffectationJuryDTO {
    private String id;
    private Integer idSoutenance;
    private Integer idEnseignant;
    private RoleJury roleJury;
    private Date dateAffectation;
    private String nomEnseignant;
    private String prenomEnseignant;
}