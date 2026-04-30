package com.microservices.auth_service.dto;

import com.microservices.auth_service.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    private Long externalId;

    @NotBlank(message = "Nom requis")
    private String nom;

    @NotBlank(message = "Prénom requis")
    private String prenom;

    @Email(message = "Email invalide")
    @NotBlank(message = "Email requis")
    private String email;

    @Size(min = 8, message = "Minimum 8 caractères")
    @NotBlank(message = "Mot de passe requis")
    private String password;

    private Role role; // si null → ROLE_ETUDIANT par défaut
}
