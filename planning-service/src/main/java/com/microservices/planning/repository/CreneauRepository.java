package com.microservices.planning.repository;

import com.microservices.planning.entity.Creneau;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CreneauRepository extends MongoRepository<Creneau, String> {

    @Query("{ 'dateSoutenance' : { $gte: ?0, $lte: ?1 } }")
    List<Creneau> findCreneauxEntreDates(LocalDate debut, LocalDate fin);

    boolean existsByDateSoutenanceAndHeureDebut(LocalDate date, LocalTime heure);
}