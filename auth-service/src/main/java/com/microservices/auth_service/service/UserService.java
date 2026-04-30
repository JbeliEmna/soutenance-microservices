package com.microservices.auth_service.service;

import com.microservices.auth_service.dto.AuthResponse;
import com.microservices.auth_service.dto.LoginRequest;
import com.microservices.auth_service.dto.RegisterRequest;
import com.microservices.auth_service.entity.Role;
import com.microservices.auth_service.entity.User;
import com.microservices.auth_service.repository.UserRepository;
import com.microservices.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request, boolean isAdminCreating) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email deja utilise");
        }
        if (request.getExternalId() != null && userRepository.existsByExternalId(request.getExternalId())) {
            throw new RuntimeException("Identifiant metier deja utilise");
        }

        Role role = request.getRole() == null ? Role.ROLE_ETUDIANT : request.getRole();

        boolean firstAdminBootstrap = role == Role.ROLE_ADMIN && userRepository.count() == 0;
        if ((role == Role.ROLE_ENSEIGNANT || role == Role.ROLE_ADMIN) && !isAdminCreating && !firstAdminBootstrap) {
            throw new RuntimeException("Seul un admin peut creer ce type de compte");
        }

        User user = User.builder()
                .externalId(request.getExternalId())
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .enabled(true)
                .build();

        userRepository.save(user);
        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .externalId(user.getExternalId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .message("Compte cree avec succes")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouve"));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .externalId(user.getExternalId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .message("Connexion reussie")
                .build();
    }
}
