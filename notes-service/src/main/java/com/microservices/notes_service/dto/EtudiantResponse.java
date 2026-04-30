package com.microservices.notes_service.dto;

import com.microservices.notes_service.model.Etudiant;

public record EtudiantResponse(
        Long id,
        String matricule,
        String nom,
        String prenom
) {
    public static EtudiantResponse fromEntity(Etudiant etudiant) {
        return new EtudiantResponse(
                etudiant.getId(),
                etudiant.getMatricule(),
                etudiant.getNom(),
                etudiant.getPrenom()
        );
    }
}
