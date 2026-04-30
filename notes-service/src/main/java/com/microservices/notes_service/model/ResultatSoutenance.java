package com.microservices.notes_service.model;

import com.microservices.notes_service.enums.MentionFinale;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "resultats_soutenance")
public class ResultatSoutenance {

    @Id
    private Long id;

    @Indexed(unique = true)
    private Long soutenanceId;

    private Double noteFinale;

    private MentionFinale mention;

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

    public Double getNoteFinale() {
        return noteFinale;
    }

    public void setNoteFinale(Double noteFinale) {
        this.noteFinale = noteFinale;
    }

    public MentionFinale getMention() {
        return mention;
    }

    public void setMention(MentionFinale mention) {
        this.mention = mention;
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
