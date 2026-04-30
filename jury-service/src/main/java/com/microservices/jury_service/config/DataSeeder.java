package com.microservices.jury_service.config;

import com.microservices.jury_service.entity.AffectationJury;
import com.microservices.jury_service.entity.MembreJury;
import com.microservices.jury_service.entity.RoleJury;
import com.microservices.jury_service.repository.AffectationJuryRepository;
import com.microservices.jury_service.repository.MembreJuryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Date;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedJuryData(
            MembreJuryRepository membreJuryRepository,
            AffectationJuryRepository affectationJuryRepository
    ) {
        return args -> {
            seedMember(membreJuryRepository, "seed-president", 3001L, "President", "Prof", "Professeur", "president.seed@test.tn");
            seedMember(membreJuryRepository, "seed-rapporteur", 3002L, "Rapporteur", "Prof", "Maitre assistant", "rapporteur.seed@test.tn");
            seedMember(membreJuryRepository, "seed-examinateur", 3003L, "Examinateur", "Prof", "Assistant", "examinateur.seed@test.tn");
            seedMember(membreJuryRepository, "seed-intrus", 3999L, "Intrus", "Prof", "Assistant", "intrus.seed@test.tn");

            seedAffectation(affectationJuryRepository, "seed-aff-president", 1L, 3001L, RoleJury.PRESIDENT);
            seedAffectation(affectationJuryRepository, "seed-aff-rapporteur", 1L, 3002L, RoleJury.RAPPORTEUR);
            seedAffectation(affectationJuryRepository, "seed-aff-examinateur", 1L, 3003L, RoleJury.EXAMINATEUR);
        };
    }

    private void seedMember(
            MembreJuryRepository repository,
            String id,
            Long idEnseignant,
            String nom,
            String prenom,
            String grade,
            String email
    ) {
        if (repository.existsById(id) || repository.findByIdEnseignant(idEnseignant).isPresent()) {
            return;
        }
        repository.save(MembreJury.builder()
                .id(id)
                .idEnseignant(idEnseignant)
                .nom(nom)
                .prenom(prenom)
                .grade(grade)
                .email(email)
                .build());
    }

    private void seedAffectation(
            AffectationJuryRepository repository,
            String id,
            Long idSoutenance,
            Long idEnseignant,
            RoleJury roleJury
    ) {
        if (repository.existsById(id) || repository.existsByIdSoutenanceAndIdEnseignant(idSoutenance, idEnseignant)) {
            return;
        }
        repository.save(AffectationJury.builder()
                .id(id)
                .idSoutenance(idSoutenance)
                .idEnseignant(idEnseignant)
                .roleJury(roleJury)
                .dateAffectation(new Date())
                .build());
    }
}
