package com.microservices.soutenance_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "juries")
public class Jury {

    @Id
    private Long id;

    private Long soutenanceId;

    private Long presidentId;

    private Long rapporteurId;

    private Long examinateurId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSoutenanceId() {
        return soutenanceId;
    }

    public void setSoutenanceId(Long soutenanceId) {
        this.soutenanceId = soutenanceId;
    }

    public Long getPresidentId() {
        return presidentId;
    }

    public void setPresidentId(Long presidentId) {
        this.presidentId = presidentId;
    }

    public Long getRapporteurId() {
        return rapporteurId;
    }

    public void setRapporteurId(Long rapporteurId) {
        this.rapporteurId = rapporteurId;
    }

    public Long getExaminateurId() {
        return examinateurId;
    }

    public void setExaminateurId(Long examinateurId) {
        this.examinateurId = examinateurId;
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
