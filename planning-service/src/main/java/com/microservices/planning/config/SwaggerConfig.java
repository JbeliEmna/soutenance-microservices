package com.microservices.planning.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Planning & Gestion des Conflits")
                        .version("1.0")
                        .description("""
                                ## Service de gestion des salles, créneaux et réservations
                                
                                Ce microservice centralise la planification des soutenances et la détection automatique des conflits d'occupation.
                                
                                ### Fonctionnalités clés :
                                - **Salles** : Gestion du parc des salles disponibles.
                                - **Créneaux** : Définition des fenêtres temporelles de soutenance.
                                - **Réservations** : Affectation des soutenances aux salles (Occupation).
                                - **Vérifications** : Algorithmes de détection de double réservation.
                                - **Recherche** : Filtrage intelligent des créneaux libres.
                                
                                ### Stack Technique :
                                - **Backend** : Spring Boot 3.2.5
                                - **Base de données** : MongoDB Atlas (Cloud)
                                - **Documentation** : OpenAPI 3 / Swagger UI
                                """)
                        .contact(new Contact()
                                .name("Ahlem Horchani - VR/AR Developer")
                                .email("ahlemhorchani701@gmail.com")
                                .url("https://github.com/ahlemhorchani"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8083")
                                .description("Serveur Local (Développement)"),
                        new Server()
                                .url("https://api-planning.soutenance.fr")
                                .description("Serveur de Production (Staging)")))
                .tags(Arrays.asList(
                        new Tag().name("Salles").description("Gestion du référentiel des salles de soutenance"),
                        new Tag().name("Créneaux").description("Définition et gestion des horaires (Slots)"),
                        new Tag().name("Réservations").description("Gestion des occupations et annulations"),
                        new Tag().name("Vérifications").description("Outils de diagnostic et détection de conflits"),
                        new Tag().name("Recherche").description("Moteur de recherche de disponibilités")
                ));
    }
}