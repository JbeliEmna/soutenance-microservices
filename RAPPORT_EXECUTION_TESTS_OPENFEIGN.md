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
| planning-service | 8083 | http://localhost:8083/actuator/health | UP |
| notes-service | 8088 | http://localhost:8088/actuator/health | UP |
| gateway-service | 8089 | http://localhost:8089/actuator/health | UP |

URL principale a utiliser pour tester:

```text
http://localhost:8089
```

## 2.1 Bases de donnees separees

Tous les services utilisent le meme serveur MongoDB Atlas de Mouhanned, mais chaque service pointe maintenant vers une base differente.

| Service | Base MongoDB |
| --- | --- |
| auth-service | `gestion_soutenances_auth` |
| soutenance-service | `gestion_soutenances_soutenance` |
| jury-service | `gestion_soutenances_jury` |
| planning-service | `gestion_soutenances_planning` |
| notes-service | `gestion_soutenances_notes` |

La configuration est presente dans les `application.yml` locaux et dans le depot de configuration:

```text
C:\Users\mouha\soutenance-microservices\config-repo
```

Cela permet de garder une separation claire:

- `auth-service` garde les utilisateurs et roles;
- `soutenance-service` garde salles, soutenances, references legacy et jurys legacy;
- `jury-service` garde les membres et affectations jury;
- `notes-service` garde etudiants, assignations, evaluations et resultats;
- `planning-service` a sa propre base reservee pour les futures entites de planning.

## 2.2 Seed des donnees

Des seeders idempotents ont ete ajoutes. Ils s'executent au demarrage et n'inserent pas de doublons.

Fichiers ajoutes:

```text
auth-service/src/main/java/com/microservices/auth_service/config/DataSeeder.java
soutenance-service/src/main/java/com/microservices/soutenance_service/config/DataSeeder.java
jury-service/src/main/java/com/microservices/jury_service/config/DataSeeder.java
notes-service/src/main/java/com/microservices/notes_service/config/DataSeeder.java
```

Donnees principales seed:

| Domaine | Donnees |
| --- | --- |
| Auth | admin `9001`, etudiants `1001`, `1002`, enseignants `2001`, `2002`, `3001`, `3002`, `3003`, `3999` |
| Soutenance | salles `Salle Seed A1`, `Salle Seed B1`, soutenance `id=1` |
| Jury | president `3001`, rapporteur `3002`, examinateur `3003` affectes a la soutenance `1` |
| Notes | etudiants notes `1`, `2`, evaluations `16`, `14`, `15`, resultat final `BIEN` |

Compte admin seed:

```text
email: admin.postman@test.tn
password: password123
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

## 3.1 Tests seed verifies apres separation des bases

| Test | Resultat |
| --- | --- |
| Login admin seed | 200 OK |
| GET `/api/soutenances/1` | 200 OK, etat `TERMINEE`, salle `Salle Seed A1` |
| GET `/api/salles` | 200 OK |
| GET `/api/affectations-jury/soutenance/1` | 200 OK, 3 affectations |
| GET `/api/affectations-jury/soutenance/1/jury-complet` | 200 OK, `Jury complet: 3 membres` |
| GET `/api/evaluations/soutenance/1` | 200 OK, 3 evaluations |
| GET `/api/resultats/soutenances/1` | 200 OK, moyenne `15.0`, mention `BIEN` |
| GET `/api/resultats/etudiants/1` | 200 OK |
| GET `/api/soutenances/1/details` | 200 OK, aggregation OpenFeign complete |
| POST conflit salle sur `Salle Seed A1` | 409 Conflict |

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
