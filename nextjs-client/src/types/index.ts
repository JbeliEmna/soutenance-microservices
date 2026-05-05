export type Role = 'ROLE_ADMIN' | 'ROLE_ETUDIANT' | 'ROLE_ENSEIGNANT';
export type EtatSoutenance = 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE';
export type MentionFinale = 'PASSABLE' | 'ASSEZ_BIEN' | 'BIEN' | 'TRES_BIEN' | 'EXCELLENT';
export type RoleJury = 'president' | 'rapporteur' | 'examinateur';

export interface AuthResponse {
  token: string;
  externalId: number;
  email: string;
  role: Role;
  nom: string;
  prenom: string;
  message?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  externalId?: number;
  nom: string;
  prenom: string;
  email: string;
  password: string;
  role: Role;
}

export interface User {
  id: string;
  externalId: number;
  email: string;
  nom: string;
  prenom: string;
  role: Role;
  enabled: boolean;
}

export interface Salle {
  id: number;
  nom: string;
}

export interface Soutenance {
  id: number;
  etudiantIds: number[];
  encadrantId: number;
  salle: string;
  dateDebut: string;
  dateFin: string;
  etat: EtatSoutenance;
  createdAt?: string;
  updatedAt?: string;
}

export interface MembreJury {
  id: string;
  idEnseignant: number;
  nom: string;
  prenom: string;
  grade: string;
  email: string;
}

export interface AffectationJury {
  id: string;
  idSoutenance: number;
  idEnseignant: number;
  roleJury: RoleJury;
}

export interface Evaluation {
  id: number;
  soutenanceId: number;
  enseignantId: number;
  roleJury: string;
  note: number;
}

export interface ResultatSoutenance {
  id: number;
  soutenanceId: number;
  noteFinale: number;
  mention: MentionFinale;
}

export interface SoutenanceDetails {
  soutenance: Soutenance;
  jury: AffectationJury[];
  evaluations: Evaluation[];
  resultat: ResultatSoutenance;
}
