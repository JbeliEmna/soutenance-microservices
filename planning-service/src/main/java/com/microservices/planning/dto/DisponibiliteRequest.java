package com.soutenance.planning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DisponibiliteRequest {

    @NotNull(message = "L'ID de la salle est requis")
    private String salleId;  // Integer → String

    @NotNull(message = "La date de début est requise")
    private LocalDate dateDebut;

    @NotNull(message = "La date de fin est requise")
    private LocalDate dateFin;
}