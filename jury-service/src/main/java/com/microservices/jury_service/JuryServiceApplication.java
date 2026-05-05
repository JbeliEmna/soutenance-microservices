package com.microservices.jury_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
@SpringBootApplication
@EnableFeignClients
public class JuryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(JuryServiceApplication.class, args);
	}

}
