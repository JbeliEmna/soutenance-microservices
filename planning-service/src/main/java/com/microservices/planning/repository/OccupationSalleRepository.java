package com.soutenance.planning.repository;

import com.soutenance.planning.entity.OccupationSalle;
import com.soutenance.planning.entity.Creneau;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface OccupationSalleRepository extends MongoRepository<OccupationSalle, String> {

    boolean existsBySalleIdAndCreneauId(String salleId, String creneauId);

    Optional<OccupationSalle> findBySalleIdAndCreneauId(String salleId, String creneauId);

    List<OccupationSalle> findBySalleId(String salleId);

    List<OccupationSalle> findByCreneauId(String creneauId);

    List<OccupationSalle> findByStatut(String statut);

    @Query("{ 'salleId' : ?0, 'statut' : { $ne: 'ANNULEE' }, 'creneau.dateSoutenance' : { $gte: ?1, $lte: ?2 } }")
    List<OccupationSalle> findCreneauxOccupesPourSalle(String salleId, LocalDate debut, LocalDate fin);

    @Query("{ 'salleId' : ?0, 'creneau.dateSoutenance' : ?1 }")
    List<OccupationSalle> findReservationsBySalleAndDate(String salleId, LocalDate date);
}