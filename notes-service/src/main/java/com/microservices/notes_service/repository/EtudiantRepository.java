package com.microservices.notes_service.repository;

import com.microservices.notes_service.model.Etudiant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EtudiantRepository extends MongoRepository<Etudiant, Long> {

    boolean existsByMatricule(String matricule);
}
