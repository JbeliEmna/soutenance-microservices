# Rapport API Backend Final - Gestion des Soutenances

Ce rapport decrit l'integration finale des microservices du backend. L'objectif est de couvrir les exigences du fichier `context/project.md`:

- planification des soutenances, dates et salles;
- verification des conflits de planning;
- affectation du jury: president, rapporteur, examinateur;
- saisie des notes par les membres du jury;
- calcul automatique de la moyenne et de la mention;
- consultation des resultats par les etudiants;
- cooperation entre services avec OpenFeign.

## Architecture Logique

| Service | Port | Responsabilite |
| --- | ---: | --- |
| discovery-service | 8761 | Annuaire Eureka pour decouverte des services |
| config-service | 8888 | Configuration centralisee Spring Cloud Config |
| gateway-service | 8089 | Point d'entree HTTP et routage vers les services |
| auth-service | 8085 | Authentification, roles et JWT |
| soutenance-service | 8084 | Soutenances, salles, references, planning et conflits |
| jury-service | 8082 | Membres jury et affectations aux soutenances |
| notes-service | 8088 | Etudiants, evaluations, calcul et consultation des resultats |
| planning-service | 8083 | Module reserve au planning, la logique active est portee par soutenance-service |

## Cooperation OpenFeign

| Service appelant | Service appele | But |
| --- | --- | --- |
| soutenance-service | jury-service | Recuperer les affectations jury d'une soutenance |
| soutenance-service | notes-service | Recuperer evaluations et resultat d'une soutenance |
| jury-service | soutenance-service | Verifier qu'une soutenance existe avant affectation |
| notes-service | soutenance-service | Verifier la soutenance et mettre a jour son etat |
| notes-service | jury-service | Verifier que l'enseignant qui saisit une note est affecte au jury avec le bon role |
| soutenance-service | auth-service | Verifier qu'un etudiant, encadrant ou enseignant existe avec le bon role |

## Workflow Metier Final

1. Creer les utilisateurs dans `auth-service` avec un `externalId`: etudiant en `ROLE_ETUDIANT`, encadrant et enseignants en `ROLE_ENSEIGNANT`.
2. Creer les salles dans `soutenance-service`.
3. Planifier une soutenance via `POST /api/soutenances`.
4. Le service soutenance verifie:
   - etudiant existant dans `auth-service` avec `ROLE_ETUDIANT`;
   - encadrant existant dans `auth-service` avec `ROLE_ENSEIGNANT`;
   - salle existante;
   - date debut avant date fin;
   - pas de conflit salle;
   - pas de conflit encadrant;
   - pas de conflit etudiant.
5. Creer les membres jury dans `jury-service`.
6. Affecter exactement les roles jury via `POST /api/affectations-jury`.
7. Le service jury verifie par Feign que la soutenance existe et refuse:
   - un meme membre deux fois;
   - un meme role deux fois;
   - plus de 3 membres pour une soutenance.
8. Creer les etudiants dans `notes-service`, puis les associer a la soutenance.
9. Saisir les notes via `POST /api/evaluations`.
10. Le service notes verifie:
   - soutenance existante;
   - 1 ou 2 etudiants associes a la soutenance;
   - enseignant affecte au jury de cette soutenance;
   - role de jury coherent;
   - note entre 0 et 20;
   - une seule note par enseignant;
   - une seule note par role;
   - maximum 3 evaluations.
11. Apres chaque note, le resultat est recalcule automatiquement.
12. La soutenance passe a `EN_COURS` a la premiere evaluation, puis a `TERMINEE` quand les 3 evaluations sont saisies.
13. Les details complets sont consultables via `GET /api/soutenances/{id}/details`.

## Gateway

Base URL conseillee:

```text
http://localhost:8089
```

Routes principales:

| Path | Service cible |
| --- | --- |
| `/api/auth/**` | auth-service |
| `/api/soutenances/**` | soutenance-service ou notes-service selon route specifique |
| `/api/salles/**` | soutenance-service |
| `/api/references/**` | soutenance-service |
| `/api/juries/**` | soutenance-service |
| `/api/membres-jury/**` | jury-service |
| `/api/affectations-jury/**` | jury-service |
| `/api/evaluations/**` | notes-service |
| `/api/resultats/**` | notes-service |
| `/api/etudiants/**` | notes-service |

## Auth Service

Base directe: `http://localhost:8085`.

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/auth/register` | Inscription utilisateur, role etudiant par defaut |
| POST | `/api/auth/login` | Connexion et generation JWT |
| GET | `/api/auth/me` | Profil utilisateur connecte |
| POST | `/api/auth/admin/create-user` | Creation utilisateur par admin |

Exemple `POST /api/auth/register`:

```json
{
  "nom": "Ben Ali",
  "prenom": "Ahmed",
  "email": "ahmed@etudiant.com",
  "password": "password123",
  "role": "ROLE_ETUDIANT"
}
```

Roles disponibles:

```text
ROLE_ADMIN, ROLE_ETUDIANT, ROLE_ENSEIGNANT
```

## Soutenance Service

Base directe: `http://localhost:8084`

### References

Ces endpoints existent encore pour compatibilite, mais la validation de planification utilise maintenant `auth-service` par OpenFeign. La source de verite des utilisateurs est donc `auth-service`.

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/references/etudiants` | Creer une reference etudiant pour la planification |
| GET | `/api/references/etudiants` | Lister les references etudiants |
| POST | `/api/references/encadrants` | Creer une reference encadrant |
| GET | `/api/references/encadrants` | Lister les encadrants |
| POST | `/api/references/enseignants` | Creer une reference enseignant |
| GET | `/api/references/enseignants` | Lister les enseignants |

Body reference:

```json
{
  "id": 1,
  "nomComplet": "Mohamed Ben Salem"
}
```

### Salles

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/salles` | Creer une salle |
| GET | `/api/salles` | Lister les salles |
| GET | `/api/salles/{id}` | Consulter une salle |
| PUT | `/api/salles/{id}` | Modifier une salle |
| DELETE | `/api/salles/{id}` | Supprimer une salle |

Body salle:

```json
{
  "nom": "Salle A1"
}
```

### Soutenances

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/soutenances` | Planifier une soutenance |
| GET | `/api/soutenances` | Lister les soutenances |
| GET | `/api/soutenances/{id}` | Consulter une soutenance |
| GET | `/api/soutenances/{id}/details` | Consulter soutenance + jury + notes + resultat |
| PUT | `/api/soutenances/{id}` | Modifier la planification |
| PATCH | `/api/soutenances/{id}/etat` | Changer l'etat |
| DELETE | `/api/soutenances/{id}` | Supprimer une soutenance |

Body creation soutenance:

```json
{
  "etudiantId": 1,
  "encadrantId": 10,
  "salle": "Salle A1",
  "dateDebut": "2026-05-10T09:00:00",
  "dateFin": "2026-05-10T10:00:00"
}
```

Body changement etat:

```json
{
  "etat": "EN_COURS"
}
```

Etats:

```text
PLANIFIEE, EN_COURS, TERMINEE
```

Regles de transition:

```text
PLANIFIEE -> EN_COURS -> TERMINEE
```

Endpoint orchestration:

```http
GET /api/soutenances/{id}/details
```

Retour logique:

```json
{
  "soutenance": {},
  "jury": [],
  "evaluations": [],
  "resultat": {}
}
```

## Jury Service

Base directe: `http://localhost:8082`

### Membres Jury

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/membres-jury` | Creer un membre jury |
| GET | `/api/membres-jury` | Lister les membres |
| GET | `/api/membres-jury/{id}` | Consulter un membre |
| GET | `/api/membres-jury/enseignant/{idEnseignant}` | Rechercher par id enseignant |
| PUT | `/api/membres-jury/{id}` | Modifier un membre |
| DELETE | `/api/membres-jury/{id}` | Supprimer un membre |

Body membre jury:

```json
{
  "idEnseignant": 20,
  "nom": "Trabelsi",
  "prenom": "Sami",
  "grade": "Maitre assistant",
  "email": "sami.trabelsi@univ.tn"
}
```

### Affectations Jury

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/affectations-jury` | Affecter un enseignant a une soutenance |
| GET | `/api/affectations-jury` | Lister toutes les affectations |
| GET | `/api/affectations-jury/{id}` | Consulter une affectation |
| GET | `/api/affectations-jury/soutenance/{idSoutenance}` | Lister le jury d'une soutenance |
| GET | `/api/affectations-jury/soutenance/{idSoutenance}/jury-complet` | Verifier le jury complet |
| PUT | `/api/affectations-jury/{id}` | Modifier une affectation |
| DELETE | `/api/affectations-jury/{id}` | Supprimer une affectation |

Body affectation:

```json
{
  "idSoutenance": 1,
  "idEnseignant": 20,
  "roleJury": "president"
}
```

Roles acceptes:

```text
president, rapporteur, examinateur
```

Regles:

- la soutenance doit exister dans `soutenance-service`;
- un enseignant ne peut pas etre affecte deux fois a la meme soutenance;
- un role ne peut pas etre affecte deux fois a la meme soutenance;
- une soutenance ne peut pas depasser 3 membres jury.

## Notes Service

Base directe: `http://localhost:8088`

### Etudiants

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/etudiants` | Creer un etudiant pour les notes/resultats |
| GET | `/api/etudiants` | Lister les etudiants |
| GET | `/api/etudiants/{id}` | Consulter un etudiant |

Body etudiant:

```json
{
  "matricule": "ETU2026001",
  "nom": "Ben Ali",
  "prenom": "Ahmed"
}
```

### Association Etudiants Soutenance

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/soutenances/etudiants/assignations` | Associer 1 ou 2 etudiants a une soutenance |
| GET | `/api/soutenances/{soutenanceId}/etudiants` | Lister les etudiants d'une soutenance |

Body association:

```json
{
  "soutenanceId": 1,
  "etudiantIds": [1, 2]
}
```

Regles:

- la soutenance doit exister;
- 1 ou 2 etudiants maximum;
- chaque etudiant doit exister dans `notes-service`;
- pas de doublons.

### Evaluations

| Methode | Endpoint | Description |
| --- | --- | --- |
| POST | `/api/evaluations` | Saisir une note |
| PUT | `/api/evaluations/{id}` | Modifier une note |
| GET | `/api/evaluations/soutenance/{soutenanceId}` | Lister les notes d'une soutenance |
| DELETE | `/api/evaluations/{id}` | Supprimer une note |

Body evaluation:

```json
{
  "soutenanceId": 1,
  "enseignantId": 20,
  "roleJury": "PRESIDENT",
  "note": 16.5
}
```

Roles notes:

```text
PRESIDENT, RAPPORTEUR, EXAMINATEUR
```

Regles:

- la soutenance doit exister;
- la soutenance doit avoir 1 ou 2 etudiants associes;
- l'enseignant doit etre affecte dans `jury-service`;
- le role envoye doit correspondre au role affecte;
- note entre 0 et 20;
- maximum 3 evaluations par soutenance;
- un enseignant ne saisit qu'une note par soutenance;
- un role ne saisit qu'une note par soutenance.

### Resultats

| Methode | Endpoint | Description |
| --- | --- | --- |
| GET | `/api/resultats/soutenances/{soutenanceId}` | Resultat d'une soutenance |
| GET | `/api/resultats/etudiants/{etudiantId}` | Resultats d'un etudiant |

Le resultat est cree ou recalcule automatiquement apres chaque evaluation.

Retour logique:

```json
{
  "soutenanceId": 1,
  "noteFinale": 15.67,
  "mention": "BIEN",
  "etudiantIds": [1, 2]
}
```

## Ordre de Test Recommande

1. Demarrer `discovery-service`.
2. Demarrer `config-service`.
3. Demarrer `gateway-service`.
4. Demarrer `soutenance-service`, `jury-service`, `notes-service`, `auth-service`.
5. Creer references et salles.
6. Creer une soutenance.
7. Creer membres jury.
8. Affecter president, rapporteur, examinateur.
9. Creer etudiants notes.
10. Associer 1 ou 2 etudiants a la soutenance.
11. Saisir les 3 evaluations.
12. Consulter:
    - `/api/resultats/soutenances/{id}`;
    - `/api/resultats/etudiants/{id}`;
    - `/api/soutenances/{id}/details`.

## Verification Technique

Commande executee:

```bash
mvn -q -DskipTests compile
```

Resultat:

```text
Compilation globale OK
```

Note: les tests complets peuvent necessiter une connexion MongoDB Atlas et Eureka disponibles selon l'environnement local.
