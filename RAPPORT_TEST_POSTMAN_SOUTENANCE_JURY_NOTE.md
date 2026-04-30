# Rapport de test Postman - modules Soutenance, Jury, Notes

## 1) Objectif
Ce document definit les cas de test API a executer avec Postman pour les 3 modules:
- soutenance-service
- jury-service
- notes-service

Le but est de verifier:
- les parcours nominaux (creation, lecture, mise a jour, suppression)
- les regles metier
- la gestion des erreurs (400, 404, 409)

## 2) Environnement de test
- Date du rapport: 2026-04-24
- Service de configuration: http://localhost:8888
- Discovery (Eureka): http://localhost:8761
- Gateway: http://localhost:8089
- Soutenance direct: http://localhost:8084
- Jury direct: http://localhost:8082
- Notes direct: http://localhost:8088

Important:
- Les cas de test ci-dessous utilisent les URLs directes des services metier.
- Raison: le routage gateway actuel ne couvre pas exactement tous les paths exposes par jury-service et notes-service.

## 3) Variables Postman recommandees
Creer un environnement Postman avec:

```text
soutenance_base_url = http://localhost:8084
jury_base_url       = http://localhost:8082
notes_base_url      = http://localhost:8088

studentRefId1       = 3001
studentRefId2       = 3002
encadrantRefId1     = 4001
encadrantRefId2     = 4002

soutenanceId        =
soutenanceId2       =
juryId              =

enseignantId1       =
enseignantId2       =
enseignantId3       =
enseignantId4       =

etudiantId1         =
etudiantId2         =
evaluationId1       =
evaluationId2       =
evaluationId3       =
```

## 4) Scripts Postman utiles

### 4.1 Script de validation standard (onglet Tests)
```javascript
pm.test("Status code attendu", function () {
  pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);
});
```

### 4.2 Stockage d'un id de reponse dans une variable
```javascript
const body = pm.response.json();
if (body.id) {
  pm.environment.set("resourceId", body.id);
}
```

### 4.3 Verification format d'erreur standard
```javascript
const body = pm.response.json();
pm.test("Erreur standard", function () {
  pm.expect(body).to.have.property("timestamp");
  pm.expect(body).to.have.property("status");
  pm.expect(body).to.have.property("error");
  pm.expect(body).to.have.property("message");
  pm.expect(body).to.have.property("path");
});
```

## 5) Cas de test - module Soutenance

### SOU-01 - Creer reference etudiant (OK)
- Methode/URL: `POST {{soutenance_base_url}}/api/references/etudiants`
- Body:
```json
{
  "id": {{studentRefId1}},
  "nomComplet": "Etudiant Test 1"
}
```
- Attendu: `201`, payload avec `id` et `nomComplet`.
- Resultat obtenu: ..........

### SOU-02 - Creer reference etudiant deja existante (KO)
- Methode/URL: `POST {{soutenance_base_url}}/api/references/etudiants`
- Body: meme body que SOU-01
- Attendu: `409`, message contient `Cet etudiant existe deja`.
- Resultat obtenu: ..........

### SOU-03 - Creer references encadrants/enseignants (OK)
- Requetes:
  - `POST {{soutenance_base_url}}/api/references/encadrants`
  - `POST {{soutenance_base_url}}/api/references/enseignants`
- Body type:
```json
{
  "id": {{encadrantRefId1}},
  "nomComplet": "Enseignant Test 1"
}
```
- Attendu: `201`.
- Resultat obtenu: ..........

### SOU-04 - Creer une soutenance valide (OK)
- Methode/URL: `POST {{soutenance_base_url}}/api/soutenances`
- Body:
```json
{
  "etudiantId": {{studentRefId1}},
  "encadrantId": {{encadrantRefId1}},
  "salle": "B12",
  "dateDebut": "2026-05-02T09:00:00",
  "dateFin": "2026-05-02T10:00:00"
}
```
- Attendu: `201`, `etat = PLANIFIEE`, `id` present.
- Postman Test suggere:
```javascript
const body = pm.response.json();
pm.environment.set("soutenanceId", body.id);
pm.test("Etat initial", () => pm.expect(body.etat).to.eql("PLANIFIEE"));
```
- Resultat obtenu: ..........

### SOU-05 - Date invalide (KO)
- Methode/URL: `POST {{soutenance_base_url}}/api/soutenances`
- Body: `dateDebut` >= `dateFin`
- Attendu: `400`, message contient `La date de debut doit etre avant la date de fin`.
- Resultat obtenu: ..........

### SOU-06 - Conflit de salle (KO)
- Methode/URL: `POST {{soutenance_base_url}}/api/soutenances`
- Preconditions: SOU-04 existe
- Body (meme salle, chevauchement horaire):
```json
{
  "etudiantId": {{studentRefId2}},
  "encadrantId": {{encadrantRefId2}},
  "salle": "B12",
  "dateDebut": "2026-05-02T09:15:00",
  "dateFin": "2026-05-02T09:45:00"
}
```
- Attendu: `409`, message contient `salle deja occupee`.
- Resultat obtenu: ..........

### SOU-07 - Conflit encadrant (KO)
- Methode/URL: `POST {{soutenance_base_url}}/api/soutenances`
- Body: encadrant identique, salle differente, horaire chevauche
- Attendu: `409`, message contient `encadrant deja affecte`.
- Resultat obtenu: ..........

### SOU-08 - Conflit etudiant (KO)
- Methode/URL: `POST {{soutenance_base_url}}/api/soutenances`
- Body: etudiant identique, salle differente, horaire chevauche
- Attendu: `409`, message contient `etudiant deja planifie`.
- Resultat obtenu: ..........

### SOU-09 - Transition d'etat valide (OK)
- Methode/URL: `PATCH {{soutenance_base_url}}/api/soutenances/{{soutenanceId}}/etat`
- Body:
```json
{ "etat": "EN_COURS" }
```
- Attendu: `200`, `etat = EN_COURS`.
- Resultat obtenu: ..........

### SOU-10 - Transition d'etat invalide (KO)
- Methode/URL: `PATCH {{soutenance_base_url}}/api/soutenances/{{soutenanceId2}}/etat`
- Precondition: `soutenanceId2` doit etre en `PLANIFIEE`
- Body:
```json
{ "etat": "TERMINEE" }
```
- Attendu: `400`, message contient `Transition d'etat invalide`.
- Resultat obtenu: ..........

### SOU-11 - Lecture par id
- Methode/URL: `GET {{soutenance_base_url}}/api/soutenances/{{soutenanceId}}`
- Attendu: `200`.
- Resultat obtenu: ..........

### SOU-12 - Suppression puis lecture KO
- Etape 1: `DELETE {{soutenance_base_url}}/api/soutenances/{{soutenanceId}}` -> Attendu `204`
- Etape 2: `GET {{soutenance_base_url}}/api/soutenances/{{soutenanceId}}` -> Attendu `404`
- Resultat obtenu: ..........

## 6) Cas de test - module Jury

### JUR-01 - Creer des enseignants (OK)
- Methode/URL: `POST {{jury_base_url}}/api/enseignants`
- Body type:
```json
{
  "nom": "Dupont",
  "prenom": "Ali",
  "grade": "Maitre de conferences"
}
```
- Attendu: `201`, recuperer 4 ids (`enseignantId1..4`).
- Resultat obtenu: ..........

### JUR-02 - Creer un jury valide (OK)
- Methode/URL: `POST {{jury_base_url}}/api/juries`
- Body:
```json
{
  "soutenanceId": 9001,
  "encadrantId": {{enseignantId1}},
  "presidentId": {{enseignantId2}},
  "rapporteurId": {{enseignantId3}},
  "examinateurId": {{enseignantId4}}
}
```
- Attendu: `201`, id jury present.
- Resultat obtenu: ..........

### JUR-03 - Jury deja affecte a la meme soutenance (KO)
- Methode/URL: `POST {{jury_base_url}}/api/juries`
- Body: meme `soutenanceId` que JUR-02
- Attendu: `409`, message contient `Un jury est deja affecte`.
- Resultat obtenu: ..........

### JUR-04 - Membres non distincts (KO)
- Methode/URL: `POST {{jury_base_url}}/api/juries`
- Body: `presidentId == rapporteurId`
- Attendu: `400`, message contient `membres du jury doivent etre distincts`.
- Resultat obtenu: ..........

### JUR-05 - Encadrant aussi membre du jury (KO)
- Methode/URL: `POST {{jury_base_url}}/api/juries`
- Body: `encadrantId == presidentId`
- Attendu: `409`, message contient `a la fois encadrant et membre du jury`.
- Resultat obtenu: ..........

### JUR-06 - Enseignant inexistant (KO)
- Methode/URL: `POST {{jury_base_url}}/api/juries`
- Body: un id enseignant non existant (ex: 999999)
- Attendu: `400`, message contient `n'existe pas`.
- Resultat obtenu: ..........

### JUR-07 - Lire jury par soutenance (OK)
- Methode/URL: `GET {{jury_base_url}}/api/juries/soutenance/9001`
- Attendu: `200`.
- Resultat obtenu: ..........

### JUR-08 - Mettre a jour jury (OK)
- Methode/URL: `PUT {{jury_base_url}}/api/juries/{{juryId}}`
- Attendu: `200`.
- Resultat obtenu: ..........

### JUR-09 - Lire un jury inexistant (KO)
- Methode/URL: `GET {{jury_base_url}}/api/juries/999999`
- Attendu: `404`, message contient `Jury introuvable`.
- Resultat obtenu: ..........

### JUR-10 - Supprimer un jury (OK)
- Methode/URL: `DELETE {{jury_base_url}}/api/juries/{{juryId}}`
- Attendu: `204`.
- Resultat obtenu: ..........

## 7) Cas de test - module Notes

### NOT-01 - Creer etudiants (OK)
- Methode/URL: `POST {{notes_base_url}}/api/etudiants`
- Body type:
```json
{
  "matricule": "MAT-2026-001",
  "nom": "Ben",
  "prenom": "Amira"
}
```
- Attendu: `201`, recuperer `etudiantId1` puis `etudiantId2`.
- Resultat obtenu: ..........

### NOT-02 - Matricule deja existant (KO)
- Methode/URL: `POST {{notes_base_url}}/api/etudiants`
- Body: meme matricule
- Attendu: `409`, message contient `matricule existe deja`.
- Resultat obtenu: ..........

### NOT-03 - Assigner 1 ou 2 etudiants a une soutenance (OK)
- Methode/URL: `POST {{notes_base_url}}/api/soutenances/etudiants/assignations`
- Body:
```json
{
  "soutenanceId": 9001,
  "etudiantIds": [{{etudiantId1}}, {{etudiantId2}}]
}
```
- Attendu: `200`, reponse liste de 1 ou 2 ids.
- Resultat obtenu: ..........

### NOT-04 - Assignation avec plus de 2 etudiants (KO)
- Methode/URL: `POST {{notes_base_url}}/api/soutenances/etudiants/assignations`
- Body: 3 ids etudiants
- Attendu: `400` (validation `@Size(max=2)`).
- Resultat obtenu: ..........

### NOT-05 - Assignation avec doublons (KO)
- Methode/URL: `POST {{notes_base_url}}/api/soutenances/etudiants/assignations`
- Body: `[{{etudiantId1}}, {{etudiantId1}}]`
- Attendu: `400`, message contient `contient des doublons`.
- Resultat obtenu: ..........

### NOT-06 - Creer evaluation sans binome assigne (KO)
- Methode/URL: `POST {{notes_base_url}}/api/evaluations`
- Body:
```json
{
  "soutenanceId": 9991,
  "enseignantId": 501,
  "roleJury": "PRESIDENT",
  "note": 12.5
}
```
- Attendu: `400`, message contient `une soutenance doit concerner 1 ou 2 etudiants`.
- Resultat obtenu: ..........

### NOT-07 - Creer une evaluation valide (OK)
- Methode/URL: `POST {{notes_base_url}}/api/evaluations`
- Body:
```json
{
  "soutenanceId": 9001,
  "enseignantId": 501,
  "roleJury": "PRESIDENT",
  "note": 14.0
}
```
- Attendu: `201`, id evaluation present.
- Resultat obtenu: ..........

### NOT-08 - Role jury duplique pour la meme soutenance (KO)
- Methode/URL: `POST {{notes_base_url}}/api/evaluations`
- Body: meme `soutenanceId`, meme `roleJury`, autre enseignant
- Attendu: `409`, message contient `role du jury est deja utilise`.
- Resultat obtenu: ..........

### NOT-09 - Enseignant duplique pour la meme soutenance (KO)
- Methode/URL: `POST {{notes_base_url}}/api/evaluations`
- Body: meme `soutenanceId`, meme `enseignantId`, autre role
- Attendu: `409`, message contient `enseignant a deja saisi une evaluation`.
- Resultat obtenu: ..........

### NOT-10 - Limite de 3 evaluations (KO sur la 4eme)
- Etape 1: creer 3 evaluations distinctes (roles PRESIDENT/RAPPORTEUR/EXAMINATEUR) -> `201`
- Etape 2: tentative 4eme evaluation -> `409`
- Attendu: message contient `ne peut contenir que 3 evaluations`.
- Resultat obtenu: ..........

### NOT-11 - Note hors intervalle [0, 20] (KO)
- Methode/URL: `PUT {{notes_base_url}}/api/evaluations/{{evaluationId1}}`
- Body: `note = 21`
- Attendu: `400` (validation `@DecimalMax(20.0)`).
- Resultat obtenu: ..........

### NOT-12 - Lire evaluations par soutenance (OK)
- Methode/URL: `GET {{notes_base_url}}/api/evaluations/soutenance/9001`
- Attendu: `200`, liste (max 3 elements).
- Resultat obtenu: ..........

### NOT-13 - Lire resultat par soutenance (OK)
- Methode/URL: `GET {{notes_base_url}}/api/resultats/soutenances/9001`
- Attendu: `200`, payload avec:
  - `soutenanceId`
  - `noteFinale`
  - `mention` (`AJOURNE`, `PASSABLE`, `ASSEZ_BIEN`, `BIEN`, `TRES_BIEN`)
  - `etudiantIds`
- Resultat obtenu: ..........

### NOT-14 - Lire resultats par etudiant (OK)
- Methode/URL: `GET {{notes_base_url}}/api/resultats/etudiants/{{etudiantId1}}`
- Attendu: `200`, liste de resultats.
- Resultat obtenu: ..........

### NOT-15 - Supprimer toutes les evaluations puis verifier resultat KO
- Etape 1: supprimer les evaluations de la soutenance (`DELETE /api/evaluations/{id}`)
- Etape 2: `GET /api/resultats/soutenances/9001`
- Attendu: `404`, message contient `Resultat introuvable pour cette soutenance`.
- Resultat obtenu: ..........

## 8) Criteres d'acceptation globaux
Le lot est valide si:
- tous les cas nominaux passent avec les bons codes (`200/201/204`)
- les cas d'erreur retournent les bons codes (`400/404/409`)
- les messages metier correspondent aux regles implementees
- le format d'erreur est homogene (`timestamp`, `status`, `error`, `message`, `path`)

## 9) Resume execution (a remplir)
- Nombre de cas executes: ......
- Nombre de cas OK: ......
- Nombre de cas KO: ......
- Defauts identifies: ......
- Actions correctives proposees: ......
