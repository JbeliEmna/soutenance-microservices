package com.microservices.jury_service.repository;

import com.microservices.jury_service.entity.MembreJury;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MembreJuryRepository extends MongoRepository<MembreJury, String> {
    Optional<MembreJury> findByIdEnseignant(Long idEnseignant);
}
