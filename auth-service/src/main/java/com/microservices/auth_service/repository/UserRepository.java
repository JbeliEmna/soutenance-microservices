package com.microservices.auth_service.repository;

import com.microservices.auth_service.entity.User;
import com.microservices.auth_service.entity.Role;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByExternalId(Long externalId);
    List<User> findByRoleAndEnabledTrue(Role role);
    boolean existsByEmail(String email);
    boolean existsByExternalId(Long externalId);
}
