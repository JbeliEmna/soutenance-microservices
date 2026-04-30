package com.microservices.notes_service.repository;

import com.microservices.notes_service.enums.RoleJury;
import com.microservices.notes_service.model.Evaluation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface EvaluationRepository extends MongoRepository<Evaluation, Long> {

    List<Evaluation> findBySoutenanceId(Long soutenanceId);

    long countBySoutenanceId(Long soutenanceId);

    boolean existsBySoutenanceIdAndEnseignantId(Long soutenanceId, Long enseignantId);

    boolean existsBySoutenanceIdAndEnseignantIdAndIdNot(Long soutenanceId, Long enseignantId, Long id);

    boolean existsBySoutenanceIdAndRoleJury(Long soutenanceId, RoleJury roleJury);

    boolean existsBySoutenanceIdAndRoleJuryAndIdNot(Long soutenanceId, RoleJury roleJury, Long id);
}
