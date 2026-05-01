package com.microservices.soutenance_service.config;

import com.microservices.soutenance_service.enums.EtatSoutenance;
import com.microservices.soutenance_service.model.EncadrantRef;
import com.microservices.soutenance_service.model.Jury;
import com.microservices.soutenance_service.model.Salle;
import com.microservices.soutenance_service.model.Soutenance;
import com.microservices.soutenance_service.model.StudentRef;
import com.microservices.soutenance_service.repository.EncadrantRefRepository;
import com.microservices.soutenance_service.repository.JuryRepository;
import com.microservices.soutenance_service.repository.SalleRepository;
import com.microservices.soutenance_service.repository.SoutenanceRepository;
import com.microservices.soutenance_service.repository.StudentRefRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedSoutenanceData(
            StudentRefRepository studentRefRepository,
            EncadrantRefRepository encadrantRefRepository,
            SalleRepository salleRepository,
            SoutenanceRepository soutenanceRepository,
            JuryRepository juryRepository,
            MongoTemplate mongoTemplate
    ) {
        return args -> {
            LocalDateTime now = LocalDateTime.now();

            seedStudentRef(studentRefRepository, 1001L, "Ahmed Ben Ali");
            seedStudentRef(studentRefRepository, 1002L, "Mouna Trabelsi");
            seedEncadrantRef(encadrantRefRepository, 2001L, "Dr Sami Encadrant");
            seedEncadrantRef(encadrantRefRepository, 2002L, "Dr Second Encadrant");

            seedSalle(salleRepository, 1L, "Salle Seed A1", now);
            seedSalle(salleRepository, 2L, "Salle Seed B1", now);

            if (!soutenanceRepository.existsById(1L)) {
                Soutenance soutenance = new Soutenance();
                soutenance.setId(1L);
                soutenance.setEtudiantIds(List.of(1001L, 1002L));
                soutenance.setEncadrantId(2001L);
                soutenance.setSalle("Salle Seed A1");
                soutenance.setDateDebut(LocalDateTime.of(2026, 5, 10, 9, 0));
                soutenance.setDateFin(LocalDateTime.of(2026, 5, 10, 10, 0));
                soutenance.setEtat(EtatSoutenance.TERMINEE);
                soutenance.setCreatedAt(now);
                soutenance.setUpdatedAt(now);
                soutenanceRepository.save(soutenance);
            }

            if (!juryRepository.existsById(1L)) {
                Jury jury = new Jury();
                jury.setId(1L);
                jury.setSoutenanceId(1L);
                jury.setPresidentId(3001L);
                jury.setRapporteurId(3002L);
                jury.setExaminateurId(3003L);
                jury.setCreatedAt(now);
                jury.setUpdatedAt(now);
                juryRepository.save(jury);
            }

            seedSequence(mongoTemplate, "salle_sequence", 1000L);
            seedSequence(mongoTemplate, "soutenance_sequence", 1000L);
            seedSequence(mongoTemplate, "jury_sequence", 1000L);
        };
    }

    private void seedStudentRef(StudentRefRepository repository, Long id, String nomComplet) {
        if (repository.existsById(id)) {
            return;
        }
        StudentRef studentRef = new StudentRef();
        studentRef.setId(id);
        studentRef.setNomComplet(nomComplet);
        repository.save(studentRef);
    }

    private void seedEncadrantRef(EncadrantRefRepository repository, Long id, String nomComplet) {
        if (repository.existsById(id)) {
            return;
        }
        EncadrantRef encadrantRef = new EncadrantRef();
        encadrantRef.setId(id);
        encadrantRef.setNomComplet(nomComplet);
        repository.save(encadrantRef);
    }

    private void seedSalle(SalleRepository repository, Long id, String nom, LocalDateTime now) {
        if (repository.existsById(id) || repository.existsByNomIgnoreCase(nom)) {
            return;
        }
        Salle salle = new Salle();
        salle.setId(id);
        salle.setNom(nom);
        salle.setCreatedAt(now);
        salle.setUpdatedAt(now);
        repository.save(salle);
    }

    private void seedSequence(MongoTemplate mongoTemplate, String sequenceName, long value) {
        mongoTemplate.upsert(
                Query.query(Criteria.where("_id").is(sequenceName)),
                new Update().max("seq", value),
                "database_sequences"
        );
    }
}
