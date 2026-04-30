package com.microservices.soutenance_service.repository;

import com.microservices.soutenance_service.model.Jury;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface JuryRepository extends MongoRepository<Jury, Long> {

    Optional<Jury> findBySoutenanceId(Long soutenanceId);

    boolean existsBySoutenanceId(Long soutenanceId);

    boolean existsBySoutenanceIdAndIdNot(Long soutenanceId, Long id);
}
