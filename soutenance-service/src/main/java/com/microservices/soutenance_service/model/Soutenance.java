package com.microservices.soutenance_service.model;

import com.microservices.soutenance_service.enums.EtatSoutenance;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "soutenances")
public class Soutenance {

    @Id
    private Long id;

    private Long etudiantId;

    private Long encadrantId;

    private String salle;

    private LocalDateTime dateDebut;

    private LocalDateTime dateFin;

    private EtatSoutenance etat;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }

    public Long getEncadrantId() {
        return encadrantId;
    }

    public void setEncadrantId(Long encadrantId) {
        this.encadrantId = encadrantId;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDateTime dateFin) {
        this.dateFin = dateFin;
    }

    public EtatSoutenance getEtat() {
        return etat;
    }

    public void setEtat(EtatSoutenance etat) {
        this.etat = etat;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
