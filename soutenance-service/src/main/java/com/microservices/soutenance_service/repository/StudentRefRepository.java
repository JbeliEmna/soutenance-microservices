package com.microservices.soutenance_service.repository;

import com.microservices.soutenance_service.model.StudentRef;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface StudentRefRepository extends MongoRepository<StudentRef, Long> {
}
