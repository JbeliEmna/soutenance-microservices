package com.microservices.planning.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationRequest {

    @NotNull(message = "L'ID de la salle est requis")
    private String salleId;  // Integer → String

    @NotNull(message = "L'ID du créneau est requis")
    private String creneauId;  // Integer → String

    private String statut = "RESERVEE";

    private Integer referenceSoutenanceId;

    private String reserveePar;
}