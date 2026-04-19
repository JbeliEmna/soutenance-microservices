package com.microservices.planning.repository;

import com.microservices.planning.entity.Salle;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SalleRepository extends MongoRepository<Salle, String> {

    Optional<Salle> findByNumero(String numero);

    boolean existsByNumero(String numero);
}