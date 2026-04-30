package com.microservices.notes_service.repository;

import com.microservices.notes_service.model.SoutenanceEtudiant;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SoutenanceEtudiantRepository extends MongoRepository<SoutenanceEtudiant, Long> {

    List<SoutenanceEtudiant> findBySoutenanceId(Long soutenanceId);

    List<SoutenanceEtudiant> findByEtudiantId(Long etudiantId);

    boolean existsBySoutenanceIdAndEtudiantId(Long soutenanceId, Long etudiantId);

    long countBySoutenanceId(Long soutenanceId);

    void deleteBySoutenanceId(Long soutenanceId);
}
