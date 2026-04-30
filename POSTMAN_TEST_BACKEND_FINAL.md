# Postman Test Plan - Backend Gestion des Soutenances

Ce fichier est un plan de test Postman complet pour verifier toutes les entites, tous les endpoints, les conflits et les scenarios metier du backend.

Objectif couvert:

- planification des dates et salles;
- affectation jury: president, rapporteur, examinateur;
- saisie des notes;
- calcul automatique de la moyenne;
- attribution de la mention finale;
- consultation des resultats par les etudiants;
- verification avant planification: etudiant, encadrant, conflits salle, encadrant, etudiant.

## 0. Pre-requis

Demarrer les services dans cet ordre:

```text
discovery-service  : http://localhost:8761
config-service     : http://localhost:8888
gateway-service    : http://localhost:8089
soutenance-service : http://localhost:8084
jury-service       : http://localhost:8082
notes-service      : http://localhost:8088
auth-service       : selon application.yml
```

Base URL recommandee dans Postman:

```text
{{gatewayUrl}} = http://localhost:8089
```

Variables Postman a creer:

| Variable | Valeur initiale |
| --- | --- |
| `gatewayUrl` | `http://localhost:8089` |
| `token` | vide |
| `adminToken` | vide |
| `adminExternalId` | `9001` |
| `studentRefId` | `1001` |
| `studentRefId2` | `1002` |
| `encadrantId` | `2001` |
| `teacherPresidentId` | `3001` |
| `teacherRapporteurId` | `3002` |
| `teacherExaminateurId` | `3003` |
| `teacherIntrusId` | `3999` |
| `salleId` | vide |
| `soutenanceId` | vide |
| `membrePresidentMongoId` | vide |
| `membreRapporteurMongoId` | vide |
| `membreExaminateurMongoId` | vide |
| `affPresidentId` | vide |
| `affRapporteurId` | vide |
| `affExaminateurId` | vide |
| `notesEtudiantId` | vide |
| `notesEtudiantId2` | vide |
| `evalPresidentId` | vide |
| `evalRapporteurId` | vide |
| `evalExaminateurId` | vide |

Headers pour toutes les requetes JSON:

```text
Content-Type: application/json
Accept: application/json
```

Pour les endpoints securises:

```text
Authorization: Bearer {{token}}
```

## 1. Auth Service

Important: les IDs utilises par `soutenance-service`, `jury-service` et `notes-service` sont maintenant des IDs metier venant de `auth-service`.

Le champ important est:

```json
"externalId": 1001
```

`soutenance-service` verifie par OpenFeign:

- `ROLE_ETUDIANT` pour l'etudiant;
- `ROLE_ENSEIGNANT` pour l'encadrant;
- `ROLE_ENSEIGNANT` pour les membres jury.

### 1.1 Bootstrap Admin

Si la base `auth-service` est vide, cette requete cree le premier admin. Si un admin existe deja, connecte-toi avec cet admin et mets son token dans `adminToken`.

```http
POST {{gatewayUrl}}/api/auth/register
```

Body:

```json
{
  "externalId": 9001,
  "nom": "Admin",
  "prenom": "Postman",
  "email": "admin.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ADMIN"
}
```

Expected on empty DB:

```text
Status: 201 Created
role = ROLE_ADMIN
```

Postman Tests:

```javascript
pm.test("Admin bootstrap returns 201", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("adminToken", json.token);
pm.expect(json.role).to.eql("ROLE_ADMIN");
```

### 1.2 Login Admin

Use this if admin already exists.

```http
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "email": "admin.postman@test.tn",
  "password": "password123"
}
```

Expected:

```text
Status: 200 OK
```

Postman Tests:

```javascript
pm.test("Admin login returns 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.environment.set("adminToken", json.token);
```

### 1.3 Register Etudiant Auth 1001

```http
POST {{gatewayUrl}}/api/auth/register
```

Body:

```json
{
  "externalId": 1001,
  "nom": "Ben Ali",
  "prenom": "Ahmed",
  "email": "ahmed.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ETUDIANT"
}
```

Expected:

```text
Status: 201 Created
Body contains: token, email, role, nom, prenom, message
role = ROLE_ETUDIANT
```

Postman Tests:

```javascript
pm.test("Register returns 201", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.expect(json.email).to.eql("ahmed.postman@test.tn");
pm.expect(json.role).to.eql("ROLE_ETUDIANT");
pm.expect(json.externalId).to.eql(1001);
pm.environment.set("token", json.token);
```

### 1.4 Register Etudiant Auth 1002

```http
POST {{gatewayUrl}}/api/auth/register
```

Body:

```json
{
  "externalId": 1002,
  "nom": "Trabelsi",
  "prenom": "Mouna",
  "email": "mouna.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ETUDIANT"
}
```

Expected:

```text
Status: 201 Created
role = ROLE_ETUDIANT
externalId = 1002
```

### 1.5 Create Encadrant Auth 2001

```http
POST {{gatewayUrl}}/api/auth/admin/create-user
Authorization: Bearer {{adminToken}}
```

Body:

```json
{
  "externalId": 2001,
  "nom": "Encadrant",
  "prenom": "Sami",
  "email": "encadrant.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ENSEIGNANT"
}
```

Expected:

```text
Status: 201 Created
role = ROLE_ENSEIGNANT
externalId = 2001
```

### 1.6 Create Jury Teachers In Auth

President:

```http
POST {{gatewayUrl}}/api/auth/admin/create-user
Authorization: Bearer {{adminToken}}
```

Body:

```json
{
  "externalId": 3001,
  "nom": "President",
  "prenom": "Prof",
  "email": "president.auth.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ENSEIGNANT"
}
```

Rapporteur:

```http
POST {{gatewayUrl}}/api/auth/admin/create-user
Authorization: Bearer {{adminToken}}
```

Body:

```json
{
  "externalId": 3002,
  "nom": "Rapporteur",
  "prenom": "Prof",
  "email": "rapporteur.auth.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ENSEIGNANT"
}
```

Examinateur:

```http
POST {{gatewayUrl}}/api/auth/admin/create-user
Authorization: Bearer {{adminToken}}
```

Body:

```json
{
  "externalId": 3003,
  "nom": "Examinateur",
  "prenom": "Prof",
  "email": "examinateur.auth.postman@test.tn",
  "password": "password123",
  "role": "ROLE_ENSEIGNANT"
}
```

Expected for each:

```text
Status: 201 Created
role = ROLE_ENSEIGNANT
```

### 1.7 Login Etudiant

```http
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "email": "ahmed.postman@test.tn",
  "password": "password123"
}
```

Expected:

```text
Status: 200 OK
Body contains JWT token
```

Postman Tests:

```javascript
pm.test("Login returns 200", () => pm.response.to.have.status(200));
const json = pm.response.json();
pm.expect(json.token).to.be.a("string").and.not.empty;
pm.environment.set("token", json.token);
```

### 1.8 Me

```http
GET {{gatewayUrl}}/api/auth/me
Authorization: Bearer {{token}}
```

Expected:

```text
Status: 200 OK
Body contains connected user email and role
```

### 1.9 Login Wrong Password

```http
POST {{gatewayUrl}}/api/auth/login
```

Body:

```json
{
  "email": "ahmed.postman@test.tn",
  "password": "wrong-password"
}
```

Expected:

```text
Status: 401 Unauthorized or 500 depending current auth exception handler
Meaning: login refused
```

## 2. Soutenance Service - References Optional/Legacy

These endpoints still exist, but they are no longer the source of truth for planning validation.

The real validation now uses:

```text
auth-service /api/users/internal/{externalId}/exists?role=...
```

You can skip this whole section for the final architecture. Keep it only if you want to test the legacy local reference CRUD.

### 2.1 Create Student Reference

```http
POST {{gatewayUrl}}/api/references/etudiants
```

Body:

```json
{
  "id": 1001,
  "nomComplet": "Ahmed Ben Ali"
}
```

Expected:

```text
Status: 201 Created
id = 1001
```

### 2.2 Create Second Student Reference

```http
POST {{gatewayUrl}}/api/references/etudiants
```

Body:

```json
{
  "id": 1002,
  "nomComplet": "Mouna Trabelsi"
}
```

Expected:

```text
Status: 201 Created
id = 1002
```

### 2.3 Create Encadrant Reference

```http
POST {{gatewayUrl}}/api/references/encadrants
```

Body:

```json
{
  "id": 2001,
  "nomComplet": "Dr Sami Encadrant"
}
```

Expected:

```text
Status: 201 Created
id = 2001
```

### 2.4 Create Jury Teacher References

Create president teacher:

```http
POST {{gatewayUrl}}/api/references/enseignants
```

Body:

```json
{
  "id": 3001,
  "nomComplet": "Prof President"
}
```

Expected:

```text
Status: 201 Created
```

Create rapporteur teacher:

```http
POST {{gatewayUrl}}/api/references/enseignants
```

Body:

```json
{
  "id": 3002,
  "nomComplet": "Prof Rapporteur"
}
```

Expected:

```text
Status: 201 Created
```

Create examinateur teacher:

```http
POST {{gatewayUrl}}/api/references/enseignants
```

Body:

```json
{
  "id": 3003,
  "nomComplet": "Prof Examinateur"
}
```

Expected:

```text
Status: 201 Created
```

### 2.5 List References

```http
GET {{gatewayUrl}}/api/references/etudiants
GET {{gatewayUrl}}/api/references/encadrants
GET {{gatewayUrl}}/api/references/enseignants
```

Expected:

```text
Status: 200 OK
Body: array
```

### 2.6 Conflict - Duplicate Student Reference

```http
POST {{gatewayUrl}}/api/references/etudiants
```

Body:

```json
{
  "id": 1001,
  "nomComplet": "Ahmed Duplicate"
}
```

Expected:

```text
Status: 409 Conflict
message contains: Cet etudiant existe deja
```

## 3. Soutenance Service - Salles

### 3.1 Create Salle

```http
POST {{gatewayUrl}}/api/salles
```

Body:

```json
{
  "nom": "Salle Postman A1"
}
```

Expected:

```text
Status: 201 Created
Body contains: id, nom, createdAt, updatedAt
```

Postman Tests:

```javascript
pm.test("Salle created", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("salleId", json.id);
pm.expect(json.nom).to.eql("Salle Postman A1");
```

### 3.2 Get Salle By Id

```http
GET {{gatewayUrl}}/api/salles/{{salleId}}
```

Expected:

```text
Status: 200 OK
nom = Salle Postman A1
```

### 3.3 List Salles

```http
GET {{gatewayUrl}}/api/salles
```

Expected:

```text
Status: 200 OK
Body: array
```

### 3.4 Update Salle

```http
PUT {{gatewayUrl}}/api/salles/{{salleId}}
```

Body:

```json
{
  "nom": "Salle Postman A1"
}
```

Expected:

```text
Status: 200 OK
```

### 3.5 Conflict - Duplicate Salle Name

```http
POST {{gatewayUrl}}/api/salles
```

Body:

```json
{
  "nom": "Salle Postman A1"
}
```

Expected:

```text
Status: 409 Conflict
message contains: Une salle avec ce nom existe deja
```

## 4. Soutenance Service - Soutenances and Planning Conflicts

### 4.1 Create Soutenance - Nominal

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1001,
  "encadrantId": 2001,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-10T09:00:00",
  "dateFin": "2026-05-10T10:00:00"
}
```

Expected:

```text
Status: 201 Created
etat = PLANIFIEE
```

Postman Tests:

```javascript
pm.test("Soutenance created", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("soutenanceId", json.id);
pm.expect(json.etat).to.eql("PLANIFIEE");
pm.expect(json.etudiantId).to.eql(1001);
pm.expect(json.encadrantId).to.eql(2001);
```

### 4.2 Get Soutenance By Id

```http
GET {{gatewayUrl}}/api/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
id = {{soutenanceId}}
etat = PLANIFIEE
```

### 4.3 List Soutenances

```http
GET {{gatewayUrl}}/api/soutenances
```

Expected:

```text
Status: 200 OK
Body: array
```

### 4.4 Conflict - Student Does Not Exist

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 999999,
  "encadrantId": 2001,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-11T09:00:00",
  "dateFin": "2026-05-11T10:00:00"
}
```

Expected:

```text
Status: 400 Bad Request
message contains: L'etudiant n'existe pas
```

### 4.5 Conflict - Encadrant Does Not Exist

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1002,
  "encadrantId": 999999,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-11T09:00:00",
  "dateFin": "2026-05-11T10:00:00"
}
```

Expected:

```text
Status: 400 Bad Request
message contains: L'encadrant n'existe pas
```

### 4.6 Conflict - Salle Does Not Exist

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1002,
  "encadrantId": 2001,
  "salle": "Salle Unknown",
  "dateDebut": "2026-05-11T09:00:00",
  "dateFin": "2026-05-11T10:00:00"
}
```

Expected:

```text
Status: 400 Bad Request
message contains: La salle n'existe pas
```

### 4.7 Conflict - Invalid Date Range

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1002,
  "encadrantId": 2001,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-11T10:00:00",
  "dateFin": "2026-05-11T09:00:00"
}
```

Expected:

```text
Status: 400 Bad Request
message contains: La date de debut doit etre avant la date de fin
```

### 4.8 Conflict - Salle Already Occupied

Same time and same room as soutenance `{{soutenanceId}}`.

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1002,
  "encadrantId": 3001,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-10T09:30:00",
  "dateFin": "2026-05-10T10:30:00"
}
```

Expected:

```text
Status: 409 Conflict
message contains: salle deja occupee
```

### 4.9 Conflict - Encadrant Already Assigned

For this test, create another room first.

```http
POST {{gatewayUrl}}/api/salles
```

Body:

```json
{
  "nom": "Salle Postman B1"
}
```

Then:

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1002,
  "encadrantId": 2001,
  "salle": "Salle Postman B1",
  "dateDebut": "2026-05-10T09:30:00",
  "dateFin": "2026-05-10T10:30:00"
}
```

Expected:

```text
Status: 409 Conflict
message contains: encadrant deja affecte
```

### 4.10 Conflict - Student Already Planned

Create another encadrant first:

```http
POST {{gatewayUrl}}/api/references/encadrants
```

Body:

```json
{
  "id": 2002,
  "nomComplet": "Dr Second Encadrant"
}
```

Then:

```http
POST {{gatewayUrl}}/api/soutenances
```

Body:

```json
{
  "etudiantId": 1001,
  "encadrantId": 2002,
  "salle": "Salle Postman B1",
  "dateDebut": "2026-05-10T09:30:00",
  "dateFin": "2026-05-10T10:30:00"
}
```

Expected:

```text
Status: 409 Conflict
message contains: etudiant deja planifie
```

### 4.11 Update Soutenance

```http
PUT {{gatewayUrl}}/api/soutenances/{{soutenanceId}}
```

Body:

```json
{
  "etudiantId": 1001,
  "encadrantId": 2001,
  "salle": "Salle Postman A1",
  "dateDebut": "2026-05-10T09:00:00",
  "dateFin": "2026-05-10T10:00:00"
}
```

Expected:

```text
Status: 200 OK
```

## 5. Jury Service - Membres Jury

### 5.1 Create President Member

```http
POST {{gatewayUrl}}/api/membres-jury
```

Body:

```json
{
  "idEnseignant": 3001,
  "nom": "President",
  "prenom": "Prof",
  "grade": "Professeur",
  "email": "president.postman@test.tn"
}
```

Expected:

```text
Status: 201 Created
Body contains: id, idEnseignant
```

Postman Tests:

```javascript
pm.test("President member created", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("membrePresidentMongoId", json.id);
pm.expect(json.idEnseignant).to.eql(3001);
```

### 5.2 Create Rapporteur Member

```http
POST {{gatewayUrl}}/api/membres-jury
```

Body:

```json
{
  "idEnseignant": 3002,
  "nom": "Rapporteur",
  "prenom": "Prof",
  "grade": "Maitre assistant",
  "email": "rapporteur.postman@test.tn"
}
```

Expected:

```text
Status: 201 Created
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("membreRapporteurMongoId", json.id);
```

### 5.3 Create Examinateur Member

```http
POST {{gatewayUrl}}/api/membres-jury
```

Body:

```json
{
  "idEnseignant": 3003,
  "nom": "Examinateur",
  "prenom": "Prof",
  "grade": "Assistant",
  "email": "examinateur.postman@test.tn"
}
```

Expected:

```text
Status: 201 Created
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("membreExaminateurMongoId", json.id);
```

### 5.4 Get/List/Update/Delete Member Endpoints

Get by Mongo id:

```http
GET {{gatewayUrl}}/api/membres-jury/{{membrePresidentMongoId}}
```

Expected:

```text
Status: 200 OK
idEnseignant = 3001
```

Get by enseignant id:

```http
GET {{gatewayUrl}}/api/membres-jury/enseignant/3001
```

Expected:

```text
Status: 200 OK
```

List:

```http
GET {{gatewayUrl}}/api/membres-jury
```

Expected:

```text
Status: 200 OK
Body: array
```

Update:

```http
PUT {{gatewayUrl}}/api/membres-jury/{{membrePresidentMongoId}}
```

Body:

```json
{
  "idEnseignant": 3001,
  "nom": "President",
  "prenom": "Updated",
  "grade": "Professeur",
  "email": "president.updated.postman@test.tn"
}
```

Expected:

```text
Status: 200 OK
prenom = Updated
```

## 6. Jury Service - Affectations

### 6.1 Affect President

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3001,
  "roleJury": "president"
}
```

Expected:

```text
Status: 201 Created
idSoutenance = {{soutenanceId}}
idEnseignant = 3001
roleJury = president
```

Postman Tests:

```javascript
pm.test("President affected", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("affPresidentId", json.id);
pm.expect(json.idEnseignant).to.eql(3001);
```

### 6.2 Affect Rapporteur

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3002,
  "roleJury": "rapporteur"
}
```

Expected:

```text
Status: 201 Created
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("affRapporteurId", json.id);
```

### 6.3 Affect Examinateur

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3003,
  "roleJury": "examinateur"
}
```

Expected:

```text
Status: 201 Created
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("affExaminateurId", json.id);
```

### 6.4 Get Affectations By Soutenance

```http
GET {{gatewayUrl}}/api/affectations-jury/soutenance/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
Body: array with 3 items
Roles: president, rapporteur, examinateur
```

Postman Tests:

```javascript
pm.test("Jury has 3 members", () => {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.have.length(3);
});
```

### 6.5 Jury Complete Check

```http
GET {{gatewayUrl}}/api/affectations-jury/soutenance/{{soutenanceId}}/jury-complet
```

Expected:

```text
Status: 200 OK
success = true
message contains: Jury complet: 3 membres
```

### 6.6 Conflict - Soutenance Does Not Exist

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": 999999,
  "idEnseignant": 3001,
  "roleJury": "president"
}
```

Expected:

```text
Status: 404 Not Found
message contains: Soutenance non trouvee
```

### 6.7 Conflict - Same Teacher Twice

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3001,
  "roleJury": "rapporteur"
}
```

Expected:

```text
Status: 409 Conflict
message contains: deja affecte
```

### 6.8 Conflict - Same Role Twice

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3002,
  "roleJury": "president"
}
```

Expected:

```text
Status: 409 Conflict
message contains: role
```

### 6.9 Conflict - More Than 3 Jury Members

First create a fourth member:

```http
POST {{gatewayUrl}}/api/membres-jury
```

Body:

```json
{
  "idEnseignant": 3999,
  "nom": "Intrus",
  "prenom": "Prof",
  "grade": "Assistant",
  "email": "intrus.postman@test.tn"
}
```

Then:

```http
POST {{gatewayUrl}}/api/affectations-jury
```

Body:

```json
{
  "idSoutenance": {{soutenanceId}},
  "idEnseignant": 3999,
  "roleJury": "examinateur"
}
```

Expected:

```text
Status: 409 Conflict
message contains either:
- role examinateur deja affecte
- ne peut avoir que 3 membres
```

## 7. Notes Service - Etudiants and Assignation

### 7.1 Create Notes Etudiant 1

```http
POST {{gatewayUrl}}/api/etudiants
```

Body:

```json
{
  "matricule": "ETU-POSTMAN-1001",
  "nom": "Ben Ali",
  "prenom": "Ahmed"
}
```

Expected:

```text
Status: 201 Created
Body contains: id, matricule, nom, prenom
```

Postman Tests:

```javascript
pm.test("Notes etudiant 1 created", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("notesEtudiantId", json.id);
```

### 7.2 Create Notes Etudiant 2

```http
POST {{gatewayUrl}}/api/etudiants
```

Body:

```json
{
  "matricule": "ETU-POSTMAN-1002",
  "nom": "Trabelsi",
  "prenom": "Mouna"
}
```

Expected:

```text
Status: 201 Created
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("notesEtudiantId2", json.id);
```

### 7.3 Get/List Etudiants

```http
GET {{gatewayUrl}}/api/etudiants/{{notesEtudiantId}}
GET {{gatewayUrl}}/api/etudiants
```

Expected:

```text
Status: 200 OK
```

### 7.4 Assign 2 Etudiants To Soutenance

```http
POST {{gatewayUrl}}/api/soutenances/etudiants/assignations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "etudiantIds": [{{notesEtudiantId}}, {{notesEtudiantId2}}]
}
```

Expected:

```text
Status: 200 OK
Body: array with 2 ids
```

Postman Tests:

```javascript
pm.test("Two students assigned", () => {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.have.length(2);
});
```

### 7.5 Get Etudiants By Soutenance

```http
GET {{gatewayUrl}}/api/soutenances/{{soutenanceId}}/etudiants
```

Expected:

```text
Status: 200 OK
Body: array with 2 ids
```

### 7.6 Conflict - Assign Unknown Soutenance

```http
POST {{gatewayUrl}}/api/soutenances/etudiants/assignations
```

Body:

```json
{
  "soutenanceId": 999999,
  "etudiantIds": [{{notesEtudiantId}}]
}
```

Expected:

```text
Status: 400 Bad Request
message contains: La soutenance n'existe pas
```

### 7.7 Conflict - Duplicate Student In Assignation

```http
POST {{gatewayUrl}}/api/soutenances/etudiants/assignations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "etudiantIds": [{{notesEtudiantId}}, {{notesEtudiantId}}]
}
```

Expected:

```text
Status: 400 Bad Request
message contains: doublons
```

### 7.8 Conflict - More Than 2 Students

```http
POST {{gatewayUrl}}/api/soutenances/etudiants/assignations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "etudiantIds": [{{notesEtudiantId}}, {{notesEtudiantId2}}, 999999]
}
```

Expected:

```text
Status: 400 Bad Request
Validation error because list size max = 2
```

## 8. Notes Service - Evaluations and Resultats

### 8.1 Conflict - Teacher Not Assigned To Jury

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3999,
  "roleJury": "PRESIDENT",
  "note": 15.0
}
```

Expected:

```text
Status: 409 Conflict
message contains: n'est pas affecte
```

### 8.2 Conflict - Wrong Role For Assigned Teacher

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3001,
  "roleJury": "RAPPORTEUR",
  "note": 15.0
}
```

Expected:

```text
Status: 409 Conflict
message contains: n'est pas affecte a cette soutenance avec ce role
```

### 8.3 Conflict - Note Out Of Range

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3001,
  "roleJury": "PRESIDENT",
  "note": 25.0
}
```

Expected:

```text
Status: 400 Bad Request
Validation error for note max 20
```

### 8.4 Create President Evaluation

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3001,
  "roleJury": "PRESIDENT",
  "note": 16.0
}
```

Expected:

```text
Status: 201 Created
Body contains evaluation id
Soutenance state becomes EN_COURS
Resultat exists with noteFinale = 16.0 and mention = TRES_BIEN
```

Postman Tests:

```javascript
pm.test("President evaluation created", () => pm.response.to.have.status(201));
const json = pm.response.json();
pm.environment.set("evalPresidentId", json.id);
pm.expect(json.note).to.eql(16.0);
```

### 8.5 Verify State EN_COURS

```http
GET {{gatewayUrl}}/api/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
etat = EN_COURS
```

### 8.6 Create Rapporteur Evaluation

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3002,
  "roleJury": "RAPPORTEUR",
  "note": 14.0
}
```

Expected:

```text
Status: 201 Created
Current average = 15.0
Current mention = BIEN
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("evalRapporteurId", json.id);
```

### 8.7 Create Examinateur Evaluation

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3003,
  "roleJury": "EXAMINATEUR",
  "note": 15.0
}
```

Expected:

```text
Status: 201 Created
Final average = 15.0
Final mention = BIEN
Soutenance state becomes TERMINEE
```

Postman Tests:

```javascript
const json = pm.response.json();
pm.environment.set("evalExaminateurId", json.id);
```

### 8.8 Get Evaluations By Soutenance

```http
GET {{gatewayUrl}}/api/evaluations/soutenance/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
Body: array with 3 evaluations
```

Postman Tests:

```javascript
pm.test("Three evaluations exist", () => {
  pm.response.to.have.status(200);
  pm.expect(pm.response.json()).to.have.length(3);
});
```

### 8.9 Get Resultat By Soutenance

```http
GET {{gatewayUrl}}/api/resultats/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
noteFinale = 15.0
mention = BIEN
etudiantIds contains assigned students
```

Postman Tests:

```javascript
pm.test("Final result is BIEN with average 15", () => {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.noteFinale).to.eql(15.0);
  pm.expect(json.mention).to.eql("BIEN");
  pm.expect(json.etudiantIds).to.have.length(2);
});
```

### 8.10 Get Resultats By Etudiant

```http
GET {{gatewayUrl}}/api/resultats/etudiants/{{notesEtudiantId}}
```

Expected:

```text
Status: 200 OK
Body: array containing result for soutenance {{soutenanceId}}
```

### 8.11 Verify State TERMINEE

```http
GET {{gatewayUrl}}/api/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
etat = TERMINEE
```

### 8.12 Conflict - Duplicate Teacher Evaluation

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3001,
  "roleJury": "PRESIDENT",
  "note": 17.0
}
```

Expected:

```text
Status: 409 Conflict
message contains: deja saisi une evaluation
or max 3 evaluations, depending validation order
```

### 8.13 Conflict - Duplicate Role Evaluation

If duplicate teacher check happens first, use another assigned teacher with already-used role.

```http
POST {{gatewayUrl}}/api/evaluations
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3002,
  "roleJury": "PRESIDENT",
  "note": 13.0
}
```

Expected:

```text
Status: 409 Conflict
message contains:
- n'est pas affecte avec ce role
or
- role du jury deja utilise
```

### 8.14 Update Evaluation

```http
PUT {{gatewayUrl}}/api/evaluations/{{evalExaminateurId}}
```

Body:

```json
{
  "soutenanceId": {{soutenanceId}},
  "enseignantId": 3003,
  "roleJury": "EXAMINATEUR",
  "note": 18.0
}
```

Expected:

```text
Status: 200 OK
Result recalculated: average = 16.0, mention = TRES_BIEN
```

Verify:

```http
GET {{gatewayUrl}}/api/resultats/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 200 OK
noteFinale = 16.0
mention = TRES_BIEN
```

### 8.15 Delete Evaluation

```http
DELETE {{gatewayUrl}}/api/evaluations/{{evalExaminateurId}}
```

Expected:

```text
Status: 204 No Content
Result recalculated with remaining two notes: average = 15.0, mention = BIEN
```

Optional: recreate it after delete if you need final complete state.

## 9. Soutenance Details Orchestration

### 9.1 Get Complete Details

```http
GET {{gatewayUrl}}/api/soutenances/{{soutenanceId}}/details
```

Expected:

```text
Status: 200 OK
Body contains:
- soutenance
- jury
- evaluations
- resultat
```

Expected body shape:

```json
{
  "soutenance": {
    "id": 1,
    "etudiantId": 1001,
    "encadrantId": 2001,
    "salle": "Salle Postman A1",
    "etat": "TERMINEE"
  },
  "jury": [
    {
      "idEnseignant": 3001,
      "roleJury": "president"
    }
  ],
  "evaluations": [
    {
      "enseignantId": 3001,
      "roleJury": "PRESIDENT",
      "note": 16.0
    }
  ],
  "resultat": {
    "noteFinale": 16.0,
    "mention": "TRES_BIEN"
  }
}
```

Postman Tests:

```javascript
pm.test("Details aggregate multiple services", () => {
  pm.response.to.have.status(200);
  const json = pm.response.json();
  pm.expect(json.soutenance).to.be.an("object");
  pm.expect(json.jury).to.be.an("array");
  pm.expect(json.evaluations).to.be.an("array");
});
```

## 10. Manual State Transition Tests

### 10.1 Invalid Transition TERMINEE To PLANIFIEE

```http
PATCH {{gatewayUrl}}/api/soutenances/{{soutenanceId}}/etat
```

Body:

```json
{
  "etat": "PLANIFIEE"
}
```

Expected:

```text
Status: 400 Bad Request
message contains: Transition d'etat invalide
```

### 10.2 Not Found Soutenance

```http
GET {{gatewayUrl}}/api/soutenances/999999
```

Expected:

```text
Status: 404 Not Found
message contains: Soutenance introuvable
```

## 11. Delete Endpoint Checks

Use delete tests at the end only, because they remove data needed by previous scenarios.

### 11.1 Delete Affectation

```http
DELETE {{gatewayUrl}}/api/affectations-jury/{{affExaminateurId}}
```

Expected:

```text
Status: 200 OK
success = true
```

### 11.2 Delete Member

```http
DELETE {{gatewayUrl}}/api/membres-jury/{{membreExaminateurMongoId}}
```

Expected:

```text
Status: 200 OK
success = true
```

### 11.3 Delete Soutenance

```http
DELETE {{gatewayUrl}}/api/soutenances/{{soutenanceId}}
```

Expected:

```text
Status: 204 No Content
```

### 11.4 Delete Salle

```http
DELETE {{gatewayUrl}}/api/salles/{{salleId}}
```

Expected:

```text
Status: 204 No Content
```

## 12. Mention Prediction Table

The average is rounded to 2 decimals.

| Average | Expected Mention |
| ---: | --- |
| `< 10` | `AJOURNE` |
| `10 <= moyenne < 12` | `PASSABLE` |
| `12 <= moyenne < 14` | `ASSEZ_BIEN` |
| `14 <= moyenne < 16` | `BIEN` |
| `>= 16` | `TRES_BIEN` |

Examples:

| Notes | Average | Mention |
| --- | ---: | --- |
| 16, 14, 15 | 15.0 | BIEN |
| 16, 14, 18 | 16.0 | TRES_BIEN |
| 9, 10, 8 | 9.0 | AJOURNE |
| 11, 10, 10 | 10.33 | PASSABLE |
| 13, 12, 13 | 12.67 | ASSEZ_BIEN |

## 13. Coverage Checklist

| Requirement | Covered By |
| --- | --- |
| Creation/modification/consultation soutenance | Sections 4, 9, 10 |
| Gestion salles | Section 3 |
| Gestion creneaux et conflits | Section 4.4 to 4.10 |
| Etudiant must exist before planning | Section 4.4 |
| Encadrant must exist before planning | Section 4.5 |
| Room cannot be occupied twice | Section 4.8 |
| Encadrant cannot have two soutenances same time | Section 4.9 |
| Student cannot have two soutenances same time | Section 4.10 |
| Gestion jurys | Sections 5 and 6 |
| President/rapporteur/examinateur | Sections 6.1 to 6.3 |
| Notes by jury members | Section 8 |
| Automatic average and mention | Sections 8.9, 8.14, 12 |
| Result consultation by student | Section 8.10 |
| OpenFeign cooperation visible | Sections 6, 8, 9 |

## 14. Recommended Postman Collection Structure

```text
Gestion Soutenances Backend
  00 - Auth
  01 - References
  02 - Salles
  03 - Soutenances Planning
  04 - Planning Conflicts
  05 - Jury Members
  06 - Jury Affectations
  07 - Jury Conflicts
  08 - Notes Students
  09 - Evaluations
  10 - Results
  11 - Details Orchestration
  12 - Deletes
```

## 15. Notes About Predictable Responses

Most business errors return JSON with fields like:

```json
{
  "timestamp": "...",
  "status": 409,
  "error": "Conflict",
  "message": "Business message",
  "path": "/api/..."
}
```

`jury-service` errors may return:

```json
{
  "timestamp": "...",
  "message": "Business message",
  "errorCode": "CONFLIT_AFFECTATION",
  "path": "/api/..."
}
```

For stable tests, assert the HTTP status and a substring of `message`, not the exact timestamp.
