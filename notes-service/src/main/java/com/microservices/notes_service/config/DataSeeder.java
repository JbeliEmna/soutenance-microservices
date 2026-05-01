package com.microservices.notes_service.config;

import com.microservices.notes_service.enums.MentionFinale;
import com.microservices.notes_service.enums.RoleJury;
import com.microservices.notes_service.model.Etudiant;
import com.microservices.notes_service.model.Evaluation;
import com.microservices.notes_service.model.ResultatSoutenance;
import com.microservices.notes_service.repository.EtudiantRepository;
import com.microservices.notes_service.repository.EvaluationRepository;
import com.microservices.notes_service.repository.ResultatSoutenanceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedNotesData(
            EtudiantRepository etudiantRepository,
            EvaluationRepository evaluationRepository,
            ResultatSoutenanceRepository resultatSoutenanceRepository,
            MongoTemplate mongoTemplate
    ) {
        return args -> {
            LocalDateTime now = LocalDateTime.now();

            seedEtudiant(etudiantRepository, 1L, "ETU-SEED-1001", "Ben Ali", "Ahmed", now);
            seedEtudiant(etudiantRepository, 2L, "ETU-SEED-1002", "Trabelsi", "Mouna", now);

            seedEvaluation(evaluationRepository, 1L, 1L, 3001L, RoleJury.PRESIDENT, 16.0, now);
            seedEvaluation(evaluationRepository, 2L, 1L, 3002L, RoleJury.RAPPORTEUR, 14.0, now);
            seedEvaluation(evaluationRepository, 3L, 1L, 3003L, RoleJury.EXAMINATEUR, 15.0, now);

            if (resultatSoutenanceRepository.findBySoutenanceId(1L).isEmpty()) {
                ResultatSoutenance resultat = new ResultatSoutenance();
                resultat.setId(1L);
                resultat.setSoutenanceId(1L);
                resultat.setNoteFinale(15.0);
                resultat.setMention(MentionFinale.BIEN);
                resultat.setCreatedAt(now);
                resultat.setUpdatedAt(now);
                resultatSoutenanceRepository.save(resultat);
            }

            seedSequence(mongoTemplate, "etudiant_sequence", 1000L);
            seedSequence(mongoTemplate, "evaluation_sequence", 1000L);
            seedSequence(mongoTemplate, "resultat_sequence", 1000L);
        };
    }

    private void seedEtudiant(
            EtudiantRepository repository,
            Long id,
            String matricule,
            String nom,
            String prenom,
            LocalDateTime now
    ) {
        if (repository.existsById(id) || repository.existsByMatricule(matricule)) {
            return;
        }
        Etudiant etudiant = new Etudiant();
        etudiant.setId(id);
        etudiant.setMatricule(matricule);
        etudiant.setNom(nom);
        etudiant.setPrenom(prenom);
        etudiant.setCreatedAt(now);
        etudiant.setUpdatedAt(now);
        repository.save(etudiant);
    }

    private void seedEvaluation(
            EvaluationRepository repository,
            Long id,
            Long soutenanceId,
            Long enseignantId,
            RoleJury roleJury,
            Double note,
            LocalDateTime now
    ) {
        if (repository.existsById(id)
                || repository.existsBySoutenanceIdAndEnseignantId(soutenanceId, enseignantId)
                || repository.existsBySoutenanceIdAndRoleJury(soutenanceId, roleJury)) {
            return;
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setId(id);
        evaluation.setSoutenanceId(soutenanceId);
        evaluation.setEnseignantId(enseignantId);
        evaluation.setRoleJury(roleJury);
        evaluation.setNote(note);
        evaluation.setCreatedAt(now);
        evaluation.setUpdatedAt(now);
        repository.save(evaluation);
    }

    private void seedSequence(MongoTemplate mongoTemplate, String sequenceName, long value) {
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(sequenceName)),
                new Update().max("seq", value),
                "database_sequences"
        );
    }
}
