# Rapport Soutenance + Notes API (CORS + Swagger)

Date: 2026-04-26
Projet: `projet_microservice`

## 1) Changements appliqu�s

### Soutenance Service
- Ajout Swagger/OpenAPI dans `pom.xml`:
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9`
- Ajout CORS global:
  - `src/main/java/com/microservices/soutenance_service/config/WebConfig.java`
- Ajout configuration dans `application.yml`:
  - `app.cors.allowed-origin-patterns`
  - `springdoc.api-docs.path=/v3/api-docs`
  - `springdoc.swagger-ui.path=/swagger-ui.html`

### Notes Service
- Ajout Swagger/OpenAPI dans `pom.xml`:
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9`
- Ajout CORS global:
  - `src/main/java/com/microservices/notes_service/config/WebConfig.java`
- Ajout configuration dans `application.yml`:
  - `app.cors.allowed-origin-patterns`
  - `springdoc.api-docs.path=/v3/api-docs`
  - `springdoc.swagger-ui.path=/swagger-ui.html`

## 2) Rapport des contr�leurs

### Soutenance Service (`http://localhost:8084`)

#### `SoutenanceController` (`/api/soutenances`)
- `POST /api/soutenances`
- `PUT /api/soutenances/{id}`
- `PATCH /api/soutenances/{id}/etat`
- `GET /api/soutenances/{id}`
- `GET /api/soutenances`
- `DELETE /api/soutenances/{id}`

#### `JuryController` (`/api/juries`)
- `POST /api/juries`
- `PUT /api/juries/{id}`
- `GET /api/juries/{id}`
- `GET /api/juries/soutenance/{soutenanceId}`
- `GET /api/juries`
- `DELETE /api/juries/{id}`

#### `ReferenceController` (`/api/references`)
- `POST /api/references/etudiants`
- `POST /api/references/encadrants`
- `POST /api/references/enseignants`
- `GET /api/references/etudiants`
- `GET /api/references/encadrants`
- `GET /api/references/enseignants`

### Notes Service (`http://localhost:8088`)

#### `EtudiantController` (`/api/etudiants`)
- `POST /api/etudiants`
- `GET /api/etudiants/{id}`
- `GET /api/etudiants`

#### `EvaluationController` (`/api/evaluations`)
- `POST /api/evaluations`
- `PUT /api/evaluations/{id}`
- `GET /api/evaluations/soutenance/{soutenanceId}`
- `DELETE /api/evaluations/{id}`

#### `ResultatController` (`/api/resultats`)
- `GET /api/resultats/soutenances/{soutenanceId}`
- `GET /api/resultats/etudiants/{etudiantId}`

#### `SoutenanceEtudiantController` (`/api/soutenances`)
- `POST /api/soutenances/etudiants/assignations`
- `GET /api/soutenances/{soutenanceId}/etudiants`

## 3) D�marrage et tests effectu�s

### Commandes de d�marrage utilis�es pour les tests
- D�sactivation discovery/eureka pour test local:
  - `EUREKA_CLIENT_ENABLED=false`
  - `SPRING_CLOUD_DISCOVERY_ENABLED=false`
- Override runtime MongoDB URI (n�cessaire car l'URI en config n'a pas de nom de base):
  - `SPRING_DATA_MONGODB_URI=mongodb+srv://...@cluster0.s3inqkc.mongodb.net/microservices?appName=Cluster0`

### R�sultats de tests (smoke tests)

#### Soutenance Service
- `GET /actuator/health` -> `200`
- `GET /swagger-ui.html` -> `302` (redirection vers UI)
- `GET /v3/api-docs` -> `200`
- `GET /api/soutenances` -> `200`
- `GET /api/juries` -> `200`
- `GET /api/references/etudiants` -> `200`
- `OPTIONS /api/soutenances` (Origin `http://localhost:3000`) -> `200`, `Access-Control-Allow-Origin=http://localhost:3000`

#### Notes Service
- `GET /actuator/health` -> `200`
- `GET /swagger-ui.html` -> `302` (redirection vers UI)
- `GET /v3/api-docs` -> `200`
- `GET /api/etudiants` -> `200`
- `GET /api/evaluations/soutenance/1` -> `200`
- `GET /api/resultats/etudiants/1` -> `200`
- `GET /api/soutenances/1/etudiants` -> `200`
- `OPTIONS /api/etudiants` (Origin `http://localhost:3000`) -> `200`, `Access-Control-Allow-Origin=http://localhost:3000`

## 4) Point d'attention identifi�

- Les deux fichiers `application.yml` contiennent une URI MongoDB sans nom de base (`...mongodb.net/?appName=Cluster0`).
- Sans override runtime, les services �chouent au d�marrage avec:
  - `java.lang.IllegalArgumentException: Database name must not be empty`
