# Guide Architecture - Planning et Conflits

## 1. Vue d'ensemble de l'architecture

Le projet suit une architecture microservices Spring Cloud:

- config-service: serveur de configuration centralisee (Spring Cloud Config Server)
- discovery-service: annuaire de services (Eureka Server)
- gateway-service: point d'entree unique (API Gateway)
- soutenance-service: service metier Planning et Conflits (gestion des creneaux, salles, verifications de conflits)

Flux principal:

1) Le client appelle gateway-service.
2) gateway-service route vers soutenance-service via Eureka.
3) soutenance-service valide les references (etudiant, encadrant), controle les conflits, puis persiste dans MongoDB.
4) Les erreurs metier et de validation sont normalisees via GlobalExceptionHandler.

## 2. Role de chaque classe

### 2.1 Classes de demarrage (infrastructure)

- config-service/src/main/java/com/microservices/config_service/ConfigServiceApplication.java
  Role: demarrage du Config Server avec @EnableConfigServer.

- discovery-service/src/main/java/com/microservices/discovery_service/DiscoveryServiceApplication.java
  Role: demarrage du registre Eureka avec @EnableEurekaServer.

- gateway-service/src/main/java/com/microservices/gateway_service/GatewayServiceApplication.java
  Role: demarrage de la gateway reactive (WebFlux) qui route les routes /api/**.

- soutenance-service/src/main/java/com/microservices/soutenance_service/SoutenanceServiceApplication.java
  Role: point d'entree du service Planning et Conflits.

### 2.2 Couche Controller (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/controller/SoutenanceController.java
  Role: expose les endpoints CRUD planning:
  - POST /api/soutenances
  - PUT /api/soutenances/{id}
  - GET /api/soutenances/{id}
  - GET /api/soutenances
  - DELETE /api/soutenances/{id}

- soutenance-service/src/main/java/com/microservices/soutenance_service/controller/ReferenceController.java
  Role: expose les endpoints de references (etudiant/encadrant) utilises par les verifications:
  - POST /api/references/etudiants
  - POST /api/references/encadrants
  - GET /api/references/etudiants
  - GET /api/references/encadrants

### 2.3 Couche Service (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/service/SoutenanceService.java
  Role: coeur metier Planning et Conflits:
  - creation/mise a jour/suppression/lecture des creneaux
  - verification dateDebut < dateFin
  - verification existence etudiant/encadrant
  - detection conflits:
    - salle
    - encadrant
    - etudiant
  - gestion des timestamps createdAt/updatedAt

- soutenance-service/src/main/java/com/microservices/soutenance_service/service/ReferenceDataService.java
  Role: gestion des references externes locales:
  - create/list students
  - create/list encadrants
  - controles d'existence pour les validations metier

- soutenance-service/src/main/java/com/microservices/soutenance_service/service/SequenceGeneratorService.java
  Role: generation d'identifiants numeriques via la collection Mongo database_sequences.

### 2.4 Couche Repository (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/repository/SoutenanceRepository.java
  Role: acces Mongo pour Soutenance + methodes derivees de detection de conflits horaires.

- soutenance-service/src/main/java/com/microservices/soutenance_service/repository/StudentRefRepository.java
  Role: CRUD Mongo pour student_refs.

- soutenance-service/src/main/java/com/microservices/soutenance_service/repository/EncadrantRefRepository.java
  Role: CRUD Mongo pour encadrant_refs.

### 2.5 Couche Model (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/model/Soutenance.java
  Role: document metier principal (planning): id, etudiantId, encadrantId, salle, dateDebut, dateFin, createdAt, updatedAt.

- soutenance-service/src/main/java/com/microservices/soutenance_service/model/StudentRef.java
  Role: reference etudiant (id, nomComplet).

- soutenance-service/src/main/java/com/microservices/soutenance_service/model/EncadrantRef.java
  Role: reference encadrant (id, nomComplet).

- soutenance-service/src/main/java/com/microservices/soutenance_service/model/DatabaseSequence.java
  Role: stockage du compteur utilise par SequenceGeneratorService.

### 2.6 Couche DTO (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/dto/CreateSoutenanceRequest.java
  Role: payload de creation d'un creneau.

- soutenance-service/src/main/java/com/microservices/soutenance_service/dto/UpdateSoutenanceRequest.java
  Role: payload de mise a jour d'un creneau.

- soutenance-service/src/main/java/com/microservices/soutenance_service/dto/SoutenanceResponse.java
  Role: payload de sortie pour les lectures CRUD.

- soutenance-service/src/main/java/com/microservices/soutenance_service/dto/ReferencePersonRequest.java
  Role: payload de creation reference etudiant/encadrant.

- soutenance-service/src/main/java/com/microservices/soutenance_service/dto/ReferencePersonResponse.java
  Role: payload de sortie reference etudiant/encadrant.

### 2.7 Gestion des exceptions (soutenance-service)

- soutenance-service/src/main/java/com/microservices/soutenance_service/exception/BusinessException.java
  Role: exception metier avec HttpStatus explicite.

- soutenance-service/src/main/java/com/microservices/soutenance_service/exception/GlobalExceptionHandler.java
  Role: format de reponse d'erreur uniforme pour:
  - erreurs metier
  - erreurs de validation
  - erreurs inattendues

## 3. Requetes Postman de test

Variables d'environnement Postman conseillees:

- gateway_base_url = http://localhost:8089
- soutenance_direct_url = http://localhost:8084

Ordre de test recommande:

### 3.1 Creer references

POST {{soutenance_direct_url}}/api/references/etudiants
Body JSON:
{
  "id": 3001,
  "nomComplet": "Etudiant Planning"
}

POST {{soutenance_direct_url}}/api/references/encadrants
Body JSON:
{
  "id": 4001,
  "nomComplet": "Encadrant Planning"
}

### 3.2 Creer un creneau soutenance (via gateway)

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3001,
  "encadrantId": 4001,
  "salle": "B12",
  "dateDebut": "2026-04-30T09:00:00",
  "dateFin": "2026-04-30T10:00:00"
}

### 3.3 Lister

GET {{gateway_base_url}}/api/soutenances

### 3.4 Lire par id

GET {{gateway_base_url}}/api/soutenances/{id}

### 3.5 Modifier

PUT {{gateway_base_url}}/api/soutenances/{id}
Body JSON:
{
  "etudiantId": 3001,
  "encadrantId": 4001,
  "salle": "B15",
  "dateDebut": "2026-04-30T10:00:00",
  "dateFin": "2026-04-30T11:00:00"
}

### 3.6 Test conflit salle (attendu HTTP 409)

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3002,
  "encadrantId": 4002,
  "salle": "B15",
  "dateDebut": "2026-04-30T10:30:00",
  "dateFin": "2026-04-30T11:30:00"
}

### 3.7 Verification explicite: pas de conflit horaire (3 cas)

Objectif metier:
- Un encadrant ne peut pas avoir deux soutenances au meme horaire.
- Une salle ne peut pas etre occupee deux fois au meme horaire.
- Un etudiant ne peut pas avoir deux soutenances au meme horaire.

Preconditions:

POST {{soutenance_direct_url}}/api/references/etudiants
Body JSON:
{
  "id": 3002,
  "nomComplet": "Etudiant 2"
}

POST {{soutenance_direct_url}}/api/references/encadrants
Body JSON:
{
  "id": 4002,
  "nomComplet": "Encadrant 2"
}

POST {{soutenance_direct_url}}/api/references/etudiants
Body JSON:
{
  "id": 3003,
  "nomComplet": "Etudiant 3"
}

POST {{soutenance_direct_url}}/api/references/encadrants
Body JSON:
{
  "id": 4003,
  "nomComplet": "Encadrant 3"
}

Etape A - Creneau de base (doit passer):

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3001,
  "encadrantId": 4001,
  "salle": "C20",
  "dateDebut": "2026-05-02T09:00:00",
  "dateFin": "2026-05-02T10:00:00"
}

Etape B - Test conflit encadrant (attendu 409):

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3002,
  "encadrantId": 4001,
  "salle": "C21",
  "dateDebut": "2026-05-02T09:30:00",
  "dateFin": "2026-05-02T10:30:00"
}

Resultat attendu: message contient "Conflit horaire: encadrant deja affecte".

Etape C - Test conflit salle (attendu 409):

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3002,
  "encadrantId": 4002,
  "salle": "C20",
  "dateDebut": "2026-05-02T09:15:00",
  "dateFin": "2026-05-02T09:45:00"
}

Resultat attendu: message contient "Conflit horaire: salle deja occupee".

Etape D - Test conflit etudiant (attendu 409):

POST {{gateway_base_url}}/api/soutenances
Body JSON:
{
  "etudiantId": 3001,
  "encadrantId": 4003,
  "salle": "C22",
  "dateDebut": "2026-05-02T09:10:00",
  "dateFin": "2026-05-02T09:50:00"
}

Resultat attendu: message contient "Conflit horaire: etudiant deja planifie".

### 3.8 Supprimer

DELETE {{gateway_base_url}}/api/soutenances/{id}

## 4. Demarrage du projet (4 services)

Ordre de lancement:

1) config-service
2) discovery-service
3) gateway-service
4) soutenance-service

Commandes (PowerShell, depuis la racine du projet):

- Set-Location c:\Users\mouha\projet_microservice\config-service; .\mvnw.cmd spring-boot:run
- Set-Location c:\Users\mouha\projet_microservice\discovery-service; .\mvnw.cmd spring-boot:run
- Set-Location c:\Users\mouha\projet_microservice\gateway-service; .\mvnw.cmd spring-boot:run
- Set-Location c:\Users\mouha\projet_microservice\soutenance-service; .\mvnw.cmd spring-boot:run

## 5. Etat runtime observe (verifie)

- http://localhost:8888/actuator/health -> 200 UP
- http://localhost:8761/actuator/health -> 200 UP
- http://localhost:8089/actuator/health -> 200 UP
- http://localhost:8084/actuator/info -> 200
- http://localhost:8084/actuator/health -> timeout (cause probable: verification MongoDB distante)

Note pratique pour un health local plus rapide sur soutenance-service:

- .\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--management.health.mongo.enabled=false

