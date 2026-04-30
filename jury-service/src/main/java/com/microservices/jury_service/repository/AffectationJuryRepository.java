package com.microservices.jury_service.repository;

import com.microservices.jury_service.entity.AffectationJury;
import com.microservices.jury_service.entity.RoleJury;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AffectationJuryRepository extends MongoRepository<AffectationJury, String> {
    List<AffectationJury> findByIdSoutenance(Long idSoutenance);
    boolean existsByIdSoutenanceAndIdEnseignant(Long idSoutenance, Long idEnseignant);
    boolean existsByIdSoutenanceAndRoleJury(Long idSoutenance, RoleJury roleJury);
    boolean existsByIdSoutenanceAndRoleJuryAndIdNot(Long idSoutenance, RoleJury roleJury, String id);
    boolean existsByIdSoutenanceAndIdEnseignantAndIdNot(Long idSoutenance, Long idEnseignant, String id);
    long countByIdSoutenance(Long idSoutenance);
}
