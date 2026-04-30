package com.microservices.jury_service.entity;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor

@AllArgsConstructor
@Document(collection = "membres_jury")
public class MembreJury {

    @Id
    private String id;

    @Field("idEnseignant")
    private Long idEnseignant;

    @Field("nom")
    private String nom;

    @Field("prenom")
    private String prenom;

    @Field("grade")
    private String grade;

    @Field("email")
    private String email;
}
