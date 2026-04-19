package com.microservices.jury_service.repository;

import com.microservices.jury_service.entity.AffectationJury;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AffectationJuryRepository extends MongoRepository<AffectationJury, String> {
    List<AffectationJury> findByIdSoutenance(Integer idSoutenance);
    boolean existsByIdSoutenanceAndIdEnseignant(Integer idSoutenance, Integer idEnseignant);
}