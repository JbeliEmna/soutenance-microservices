package com.microservices.auth_service.config;

import com.microservices.auth_service.entity.Role;
import com.microservices.auth_service.entity.User;
import com.microservices.auth_service.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    private static final String DEFAULT_PASSWORD = "password123";

    @Bean
    CommandLineRunner seedAuthData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            seedUser(userRepository, passwordEncoder, 9001L, "Admin", "Seed", "admin.postman@test.tn", Role.ROLE_ADMIN);
            seedUser(userRepository, passwordEncoder, 1001L, "Ben Ali", "Ahmed", "ahmed.seed@test.tn", Role.ROLE_ETUDIANT);
            seedUser(userRepository, passwordEncoder, 1002L, "Trabelsi", "Mouna", "mouna.seed@test.tn", Role.ROLE_ETUDIANT);
            seedUser(userRepository, passwordEncoder, 2001L, "Encadrant", "Sami", "encadrant.seed@test.tn", Role.ROLE_ENSEIGNANT);
            seedUser(userRepository, passwordEncoder, 2002L, "Encadrant", "Second", "encadrant2.seed@test.tn", Role.ROLE_ENSEIGNANT);
            seedUser(userRepository, passwordEncoder, 3001L, "President", "Prof", "president.seed@test.tn", Role.ROLE_ENSEIGNANT);
            seedUser(userRepository, passwordEncoder, 3002L, "Rapporteur", "Prof", "rapporteur.seed@test.tn", Role.ROLE_ENSEIGNANT);
            seedUser(userRepository, passwordEncoder, 3003L, "Examinateur", "Prof", "examinateur.seed@test.tn", Role.ROLE_ENSEIGNANT);
            seedUser(userRepository, passwordEncoder, 3999L, "Intrus", "Prof", "intrus.seed@test.tn", Role.ROLE_ENSEIGNANT);
        };
    }

    private void seedUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            Long externalId,
            String nom,
            String prenom,
            String email,
            Role role
    ) {
        if (userRepository.existsByExternalId(externalId) || userRepository.existsByEmail(email)) {
            return;
        }

        userRepository.save(User.builder()
                .externalId(externalId)
                .nom(nom)
                .prenom(prenom)
                .email(email)
                .password(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(role)
                .enabled(true)
                .build());
    }
}
