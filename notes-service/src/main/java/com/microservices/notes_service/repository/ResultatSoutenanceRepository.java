package com.microservices.notes_service.repository;

import com.microservices.notes_service.model.ResultatSoutenance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ResultatSoutenanceRepository extends MongoRepository<ResultatSoutenance, Long> {

    Optional<ResultatSoutenance> findBySoutenanceId(Long soutenanceId);
}
