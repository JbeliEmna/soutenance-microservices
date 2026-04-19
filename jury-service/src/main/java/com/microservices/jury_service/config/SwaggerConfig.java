package com.microservices.jury_service.config;

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
                        .title("API Service Jury")
                        .version("1.0")
                        .description("""
                                ## Service de gestion des jurys pour les soutenances
                                
                                Ce microservice gère les membres du jury et leurs affectations aux soutenances.
                                
                                ### Fonctionnalités clés :
                                - **Membres Jury** : CRUD des enseignants pouvant faire partie d'un jury
                                - **Affectations** : Assignation des rôles (président, rapporteur, examinateur) aux soutenances
                                - **Vérifications** : Contrôle des doublons d'affectation
                                - **Consultation** : Liste des jurys par soutenance
                                
                                ### Rôles possibles :
                                - `président` : Préside la soutenance
                                - `rapporteur` : Évalue le travail
                                - `examinateur` : Participe à l'évaluation
                                
                                ### Stack Technique :
                                - **Backend** : Spring Boot 3.2.5
                                - **Base de données** : MongoDB Atlas
                                - **Documentation** : OpenAPI 3 / Swagger UI
                                """)
                        .contact(new Contact()
                                .name("Service Jury - Gestion des Soutenances")
                                .email("jury-service@microservices.com")
                                .url("https://github.com/microservices/jury-service"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Serveur Local (Développement)"),
                        new Server()
                                .url("https://api-jury.soutenance.fr")
                                .description("Serveur de Production")))
                .tags(Arrays.asList(
                        new Tag().name("Membre Jury").description("Gestion des membres du jury (CRUD)"),
                        new Tag().name("Affectation Jury").description("Gestion des affectations des jurys aux soutenances")
                ));
    }
}