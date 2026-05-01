package com.microservices.jury_service.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI juryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jury Service API")
                        .version("1.0.0")
                        .description("""
                                API du service jury pour la gestion des soutenances.

                                Responsabilites:
                                - Gestion des membres de jury.
                                - Affectation des roles president, rapporteur et examinateur.
                                - Verification de l'existence de la soutenance via soutenance-service.
                                - Exposition des affectations pour soutenance-service et notes-service.
                                """)
                        .contact(new Contact()
                                .name("Equipe Backend")
                                .email("jury-service@soutenance.local")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Jury service direct"),
                        new Server()
                                .url("http://localhost:8089")
                                .description("Gateway local")))
                .tags(List.of(
                        new Tag()
                                .name("Membre Jury")
                                .description("CRUD des enseignants membres du jury"),
                        new Tag()
                                .name("Affectation Jury")
                                .description("Affectation des membres aux soutenances")
                ));
    }
}
