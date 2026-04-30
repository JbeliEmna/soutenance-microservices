# Rapport execution backend et utilisation OpenFeign

## 1. Demarrage du projet

Le backend microservices a ete lance depuis:

```text
C:\Users\mouha\projet_microservice
```

Commande utilisee:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-backend-and-test.ps1 -KeepRunning
```

Le script compile le backend, demarre les services Spring Boot, verifie les endpoints health, attend l'inscription Eureka, puis execute un scenario API via la Gateway.

## 2. Services demarres

| Service | Port | URL health | Etat |
| --- | ---: | --- | --- |
| discovery-service | 8761 | http://localhost:8761/actuator/health | UP |
| config-service | 8888 | http://localhost:8888/actuator/health | UP |
| auth-service | 8085 | http://localhost:8085/actuator/health | UP |
| soutenance-service | 8084 | http://localhost:8084/actuator/health | UP |
| jury-service | 8082 | http://localhost:8082/actuator/health | UP |
| notes-service | 8088 | http://localhost:8088/actuator/health | UP |
| gateway-service | 8089 | http://localhost:8089/actuator/health | UP |

URL principale a utiliser pour tester:

```text
http://localhost:8089
```

## 3. Tests fonctionnels executes

| Besoin metier | Test execute | Resultat |
| --- | --- | --- |
| Creation d'une salle | POST /api/salles | 201 Created |
| Creation d'une soutenance | POST /api/soutenances | 201 Created, etat PLANIFIEE |
| Consultation d'une soutenance | GET /api/soutenances/{id} | 200 OK |
| Modification d'une soutenance | PUT /api/soutenances/{id} | 200 OK |
| Etudiant inexistant | POST /api/soutenances avec etudiant inconnu | 400 Bad Request |
| Encadrant inexistant | POST /api/soutenances avec encadrant inconnu | 400 Bad Request |
| Conflit salle | meme horaire + meme salle | 409 Conflict |
| Conflit encadrant | meme horaire + meme encadrant | 409 Conflict |
| Conflit etudiant | meme horaire + meme etudiant | 409 Conflict |
| Affectation jury | president, rapporteur, examinateur | 201 Created |
| Verification jury complet | GET /jury-complet | 200 OK |
| Saisie des notes | president 16, rapporteur 14, examinateur 15 | 201 Created |
| Calcul de moyenne | GET /api/resultats/soutenances/{id} | moyenne 15.0 |
| Attribution mention | moyenne 15.0 | BIEN |
| Consultation resultat etudiant | GET /api/resultats/etudiants/{id} | 200 OK |
| Details complets soutenance | GET /api/soutenances/{id}/details | jury + evaluations + resultat |

Resultat final du scenario:

```text
Soutenance id = 10
Etat initial = PLANIFIEE
Notes = 16, 14, 15
Moyenne = 15.0
Mention = BIEN
Jury = 3 membres
Evaluations = 3
```

## 4. Role d'OpenFeign dans le projet

OpenFeign permet a un microservice d'appeler un autre microservice avec une interface Java, sans ecrire manuellement du code HTTP avec RestTemplate ou WebClient.

Dans ce projet, chaque client Feign est declare avec:

```java
@FeignClient(name = "nom-du-service")
```

Le nom correspond au nom enregistre dans Eureka. Donc le service appelant ne connait pas l'adresse exacte ni le port de l'autre service: Spring Cloud + Eureka trouvent automatiquement l'instance disponible.

## 5. Utilisation concrete dans l'application

### 5.1 soutenance-service vers auth-service

Fichier:

```text
soutenance-service/src/main/java/com/microservices/soutenance_service/client/AuthServiceClient.java
```

But:

- verifier que l'etudiant existe avant de planifier une soutenance;
- verifier que l'encadrant existe et possede le role ROLE_ENSEIGNANT.

Endpoints appeles par Feign:

```text
GET /api/users/internal/{externalId}
GET /api/users/internal/{externalId}/exists?role=...
```

Exemple metier:

Avant `POST /api/soutenances`, `soutenance-service` demande a `auth-service`:

```text
Est-ce que externalId=... existe avec ROLE_ETUDIANT ?
Est-ce que externalId=... existe avec ROLE_ENSEIGNANT ?
```

Si la reponse est non, la planification est refusee avec:

```text
400 Bad Request
L'etudiant n'existe pas
```

ou:

```text
400 Bad Request
L'encadrant n'existe pas
```

### 5.2 jury-service vers soutenance-service

Fichier:

```text
jury-service/src/main/java/com/microservices/jury_service/client/SoutenanceServiceClient.java
```

But:

- verifier qu'une soutenance existe avant d'affecter un membre du jury.

Endpoint appele:

```text
GET /api/soutenances/{id}
```

Cela evite d'affecter un president, rapporteur ou examinateur a une soutenance inexistante.

### 5.3 notes-service vers jury-service

Fichier:

```text
notes-service/src/main/java/com/microservices/notes_service/client/JuryServiceClient.java
```

But:

- verifier qu'un enseignant est bien affecte au jury avant d'accepter sa note;
- verifier que le role envoye correspond au role reel: PRESIDENT, RAPPORTEUR ou EXAMINATEUR.

Endpoint appele:

```text
GET /api/affectations-jury/soutenance/{soutenanceId}
```

Exemple:

Si un enseignant non affecte tente de saisir une note, `notes-service` refuse avec un conflit.

### 5.4 notes-service vers soutenance-service

Fichier:

```text
notes-service/src/main/java/com/microservices/notes_service/client/SoutenanceServiceClient.java
```

But:

- verifier que la soutenance existe;
- changer automatiquement l'etat de la soutenance.

Endpoints appeles:

```text
GET /api/soutenances/{id}
PUT /api/soutenances/{id}/etat
```

Comportement observe:

- apres la premiere note, la soutenance peut passer a EN_COURS;
- apres les trois notes, la soutenance peut passer a TERMINEE.

### 5.5 soutenance-service vers jury-service et notes-service

Fichiers:

```text
soutenance-service/src/main/java/com/microservices/soutenance_service/client/JuryServiceClient.java
soutenance-service/src/main/java/com/microservices/soutenance_service/client/NotesServiceClient.java
```

But:

- construire les details complets d'une soutenance;
- retourner dans une seule reponse la soutenance, le jury, les evaluations et le resultat.

Endpoint teste:

```text
GET /api/soutenances/{id}/details
```

Ce endpoint montre bien l'orchestration entre plusieurs microservices.

## 6. Conclusion

Les exigences principales sont validees:

- planification des soutenances;
- verification de l'existence etudiant/encadrant par OpenFeign;
- gestion des conflits de salle, encadrant et etudiant;
- affectation d'un jury complet;
- saisie des notes par les membres autorises;
- calcul automatique de la moyenne;
- attribution automatique de la mention;
- consultation du resultat par etudiant;
- orchestration inter-services avec OpenFeign.

OpenFeign est donc utilise comme mecanisme de communication interne entre microservices. Il rend les appels plus simples, plus lisibles, et compatibles avec la decouverte de services Eureka.
