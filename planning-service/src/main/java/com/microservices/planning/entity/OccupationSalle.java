package com.microservices.planning.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

@Document(collection = "occupations_salle")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "idx_unique_reservation", def = "{'salleId': 1, 'creneauId': 1}", unique = true)

public class OccupationSalle {

    @Id
    private String id;

    @Field("salleId")      // Changé de "salle_id" à "salleId"
    private String salleId;

    @Field("creneauId")    // Changé de "creneau_id" à "creneauId"
    private String creneauId;

    @Field("statut")
    private String statut;

    @Field("referenceSoutenanceId") // Aligné aussi pour la cohérence
    private Integer referenceSoutenanceId;

    @Field("reserveePar")
    private String reserveePar;

    @Field("reservedAt")
    private LocalDateTime reservedAt;

    @Field("cancelledAt")
    private LocalDateTime cancelledAt;
}