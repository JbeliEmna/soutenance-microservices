package com.microservices.jury_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembreJuryDTO {
    private String id;
    private Integer idEnseignant;
    private String nom;
    private String prenom;
    private String grade;
    private String email;
}