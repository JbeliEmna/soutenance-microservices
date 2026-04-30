package com.microservices.auth_service.repository;

import com.microservices.auth_service.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(Long externalId);
    boolean existsByEmail(String email);
    boolean existsByExternalId(Long externalId);
}
