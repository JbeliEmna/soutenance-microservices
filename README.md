# Soutenance Microservices

## Technologies
- Java 17
- Spring Boot 3.5.13
- Spring Cloud 2025.0.0
- MySQL 8
- Angular (frontend)

## Prérequis
- Java 17 installé
- Maven installé
- MySQL 8 installé (username: root, password: root)
- Git installé

## Structure du projet
| Service | Port | Description | Package |
|---------|------|-------------|---------|
| config-service | 8888 | Configuration centralisée | com.microservices.config |
| discovery-service | 8761 | Eureka - Service Discovery | com.microservices.discovery |
| gateway-service | 8080 | API Gateway | com.microservices.gateway |
| soutenance-service | 8081 | Gestion des soutenances | com.microservices.soutenance |
| jury-service | 8082 | Gestion des jurys | com.microservices.jury |
| planning-service | 8083 | Planning et conflits | com.microservices.planning |
| notes-service | 8084 | Notes et résultats | com.microservices.notes |
| auth-service | 8085 | Authentification | com.microservices.auth |

## Ordre de démarrage (IMPORTANT)
1. config-service
2. discovery-service
3. gateway-service
4. Les services métier (dans n'importe quel ordre)

## Lancer un service
```bash
cd nom-du-service
mvn spring-boot:run
```

## Vérifications après démarrage
- Eureka Dashboard : http://localhost:8761
- Config Server : http://localhost:8888/soutenance-service/default
- Gateway : http://localhost:8080/actuator/health

## Si ton mot de passe MySQL est différent de "root"
Crée le fichier `src/main/resources/application-local.yml` :
```yaml
spring:
  datasource:
    username: root
    password: TON_MOT_DE_PASSE
```
Puis lance avec :
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## La base de données est créée automatiquement au premier démarrage.

## Instructions pour les collègues
1. Cloner le projet :
```bash
git clone https://github.com/JbeliEmna/soutenance-microservices.git
```
2. Aller sur sa branche :
```bash
git checkout feature/nom-du-service
```
3. Ouvrir son dossier de service dans IntelliJ :
    - File → Open → choisir le dossier du service (ex: jury-service)
4. Lancer les 3 services infrastructure d'abord
5. Lancer son service et commencer à coder
