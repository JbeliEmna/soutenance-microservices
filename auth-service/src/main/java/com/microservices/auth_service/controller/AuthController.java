package com.microservices.auth_service.controller;

import com.microservices.auth_service.dto.*;
import com.microservices.auth_service.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(
        name = "Authentification",
        description = "Endpoints pour l'inscription, la connexion et la gestion des utilisateurs"
)
public class AuthController {

    private final UserService userService;

    // ─────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(
            summary = "Inscription d'un nouvel utilisateur",
            description = "Permet a un etudiant ou enseignant de s'inscrire. " +
                    "Le role est defini dans le body de la requete."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Utilisateur cree avec succes",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                        "email": "ahmed@etudiant.com",
                        "nom": "Ben Ali",
                        "prenom": "Ahmed",
                        "role": "ROLE_ETUDIANT",
                        "message": "Inscription reussie"
                    }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Donnees invalides ou email deja utilise",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> register(
            @Parameter(
                    description = "Informations d'inscription de l'utilisateur",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                        {
                            "nom": "Ben Ali",
                            "prenom": "Ahmed",
                            "email": "ahmed@etudiant.com",
                            "password": "password123",
                            "role": "ROLE_ETUDIANT",
                            "matricule": "ETU2024001"
                        }
                    """)
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request, false));
    }

    // ─────────────────────────────────────────────
    // CREATE USER (ADMIN)
    // ─────────────────────────────────────────────
    @PostMapping("/admin/create-user")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Creer un utilisateur (Admin uniquement)",
            description = "Permet a un administrateur de creer un compte enseignant ou admin. " +
                    "Necessite un token JWT avec le role ROLE_ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Utilisateur cree avec succes par l'admin",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                        "email": "mohamed@enseignant.com",
                        "nom": "Ben Salem",
                        "prenom": "Mohamed",
                        "role": "ROLE_ENSEIGNANT",
                        "message": "Utilisateur cree avec succes"
                    }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT manquant ou invalide",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acces refuse - role ADMIN requis",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Donnees invalides ou email deja utilise",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> createUser(
            @Parameter(
                    description = "Informations du nouvel utilisateur a creer",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                        {
                            "nom": "Ben Salem",
                            "prenom": "Mohamed",
                            "email": "mohamed@enseignant.com",
                            "password": "password123",
                            "role": "ROLE_ENSEIGNANT",
                            "grade": "Maitre de Conferences"
                        }
                    """)
                    )
            )
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request, true));
    }

    // ─────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────
    @PostMapping("/login")
    @Operation(
            summary = "Connexion utilisateur",
            description = "Authentifie un utilisateur avec son email et mot de passe. " +
                    "Retourne un token JWT a utiliser dans les prochaines requetes."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Connexion reussie - token JWT retourne",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(value = """
                    {
                        "token": "eyJhbGciOiJIUzI1NiJ9...",
                        "email": "ahmed@etudiant.com",
                        "nom": "Ben Ali",
                        "prenom": "Ahmed",
                        "role": "ROLE_ETUDIANT",
                        "message": "Connexion reussie"
                    }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Email ou mot de passe incorrect",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                    {
                        "status": 401,
                        "error": "Non authentifie",
                        "message": "Token manquant ou invalide"
                    }
                """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Donnees invalides - champs manquants",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<AuthResponse> login(
            @Parameter(
                    description = "Credentials de connexion",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(value = """
                        {
                            "email": "ahmed@etudiant.com",
                            "password": "password123"
                        }
                    """)
                    )
            )
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    // ─────────────────────────────────────────────
    // ME (PROFIL)
    // ─────────────────────────────────────────────
    @GetMapping("/me")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
            summary = "Profil de l'utilisateur connecte",
            description = "Retourne le nom et le role de l'utilisateur actuellement connecte. " +
                    "Necessite un token JWT valide dans le header Authorization."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Informations de l'utilisateur connecte",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "Connecte : ahmed@etudiant.com | Role : [ROLE_ETUDIANT]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Token JWT manquant ou expire",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<String> me(
            @Parameter(hidden = true) Authentication auth) {
        return ResponseEntity.ok("Connecte : " + auth.getName()
                + " | Role : " + auth.getAuthorities());
    }
}