package com.soutenance.planning.entity;

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

    @Field("salle_id")
    private String salleId;      // Référence à l'ID MongoDB de la salle

    @Field("creneau_id")
    private String creneauId;    // Référence à l'ID MongoDB du créneau

    @Field("statut")
    private String statut;

    @Field("reference_soutenance_id")
    private Integer referenceSoutenanceId;

    @Field("reservee_par")
    private String reserveePar;

    @Field("reserved_at")
    private LocalDateTime reservedAt;

    @Field("cancelled_at")
    private LocalDateTime cancelledAt;
}