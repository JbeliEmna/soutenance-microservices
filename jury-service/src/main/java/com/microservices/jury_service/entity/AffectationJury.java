package com.microservices.jury_service.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "affectations_jury")
public class AffectationJury {

    @Id
    private String id;

    @Field("idSoutenance")
    private Long idSoutenance;

    @Field("idEnseignant")
    private Long idEnseignant;

    @Field("roleJury")
    private RoleJury roleJury;

    @Field("dateAffectation")
    private Date dateAffectation;
}
