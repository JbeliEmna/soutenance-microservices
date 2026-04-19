package com.microservices.jury_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class JuryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JuryServiceApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")               // Toutes les routes
						.allowedOrigins(
								"http://localhost:3000",  // React
								"http://localhost:3001",  // Autre frontend
								"http://127.0.0.1:3000"
						)
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
						.allowedHeaders("*")
						.allowCredentials(true)
						.exposedHeaders("Authorization")  // Exposer le header Authorization
						.maxAge(3600);
			}
		};
	}
}