package com.microservices.notes_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "soutenance_etudiants")
@CompoundIndexes({
    @CompoundIndex(name = "uk_soutenance_etudiant", def = "{'soutenanceId': 1, 'etudiantId': 1}", unique = true)
})
public class SoutenanceEtudiant {

    @Id
    private Long id;

    private Long soutenanceId;

    private Long etudiantId;

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

    public Long getEtudiantId() {
        return etudiantId;
    }

    public void setEtudiantId(Long etudiantId) {
        this.etudiantId = etudiantId;
    }
}
