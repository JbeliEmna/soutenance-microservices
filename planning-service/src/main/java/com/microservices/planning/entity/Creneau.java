package com.soutenance.planning.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Document(collection = "creneaux")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@CompoundIndex(name = "idx_unique_creneau", def = "{'dateSoutenance': 1, 'heureDebut': 1}", unique = true)
public class Creneau {

    @Id
    private String id;

    @Field("date_soutenance")
    private LocalDate dateSoutenance;

    @Field("heure_debut")
    private LocalTime heureDebut;

    @Field("heure_fin")
    private LocalTime heureFin;

    @Field("duree_minutes")
    private Integer dureeMinutes;

    @Field("created_at")
    private LocalDateTime createdAt;
}