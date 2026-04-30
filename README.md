# Soutenance Microservices

## Technologies
- Java 17+
- Spring Boot 3.5.x
- Spring Cloud 2025.0.0
- MongoDB
- Angular/front-end via API Gateway

## Prerequis
- Java installe
- Maven installe
- MongoDB Atlas accessible ou MongoDB local configure
- Git installe

## Structure du projet
| Service | Port | Description |
|---------|------|-------------|
| discovery-service | 8761 | Eureka - Service Discovery |
| config-service | 8888 | Configuration centralisee |
| auth-service | 8085 | Authentification et utilisateurs |
| soutenance-service | 8084 | Gestion des soutenances, salles et planning |
| jury-service | 8082 | Gestion des jurys |
| planning-service | 8083 | Planning et conflits, module reserve |
| notes-service | 8088 | Notes et resultats |
| gateway-service | 8089 | API Gateway |

## Ordre de demarrage
1. discovery-service
2. config-service
3. auth-service
4. soutenance-service, jury-service, notes-service
5. gateway-service

## Lancer un service
```bash
cd nom-du-service
mvn spring-boot:run
```

## Verifications apres demarrage
- Eureka Dashboard: http://localhost:8761
- Config Server: http://localhost:8888/actuator/health
- Auth Service: http://localhost:8085/actuator/health
- Soutenance Service: http://localhost:8084/actuator/health
- Jury Service: http://localhost:8082/actuator/health
- Notes Service: http://localhost:8088/actuator/health
- Gateway: http://localhost:8089/actuator/health

## Notes importantes
- Les utilisateurs viennent de `auth-service` avec `externalId`.
- Les endpoints `/api/references/**` de `soutenance-service` sont legacy et ne sont plus la source de verite.
- Utiliser le Gateway en priorite: http://localhost:8089
