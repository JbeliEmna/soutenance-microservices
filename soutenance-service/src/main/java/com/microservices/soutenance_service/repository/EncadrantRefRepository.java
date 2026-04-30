package com.microservices.soutenance_service.repository;

import com.microservices.soutenance_service.model.EncadrantRef;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EncadrantRefRepository extends MongoRepository<EncadrantRef, Long> {
}
