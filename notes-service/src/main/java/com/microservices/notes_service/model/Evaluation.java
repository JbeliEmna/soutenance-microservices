package com.microservices.notes_service.model;

import com.microservices.notes_service.enums.RoleJury;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "evaluations")
@CompoundIndexes({
    @CompoundIndex(name = "uk_eval_soutenance_enseignant", def = "{'soutenanceId': 1, 'enseignantId': 1}", unique = true),
    @CompoundIndex(name = "uk_eval_soutenance_role", def = "{'soutenanceId': 1, 'roleJury': 1}", unique = true)
})
public class Evaluation {

    @Id
    private Long id;

    private Long soutenanceId;

    private Long enseignantId;

    private RoleJury roleJury;

    private Double note;

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

    public Long getEnseignantId() {
        return enseignantId;
    }

    public void setEnseignantId(Long enseignantId) {
        this.enseignantId = enseignantId;
    }

    public RoleJury getRoleJury() {
        return roleJury;
    }

    public void setRoleJury(RoleJury roleJury) {
        this.roleJury = roleJury;
    }

    public Double getNote() {
        return note;
    }

    public void setNote(Double note) {
        this.note = note;
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
