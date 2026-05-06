import api from './api-client';
import { Evaluation, ResultatSoutenance, MentionFinale } from '@/types';

export const notesService = {
  // Evaluations
  async getAllEvaluations(): Promise<Evaluation[]> {
    const response = await api.get<Evaluation[]>('/api/evaluations');
    return response.data;
  },

  async getEvaluationsBySoutenance(soutenanceId: number): Promise<Evaluation[]> {
    const response = await api.get<Evaluation[]>(`/api/evaluations/soutenances/${soutenanceId}`);
    return response.data;
  },

  async createEvaluation(data: {
    soutenanceId: number;
    enseignantId: number;
    roleJury: string;
    note: number;
  }): Promise<Evaluation> {
    const response = await api.post<Evaluation>('/api/evaluations', data);
    return response.data;
  },

  async updateEvaluation(id: number, data: {
    soutenanceId: number;
    enseignantId: number;
    roleJury: string;
    note: number;
  }): Promise<Evaluation> {
    const response = await api.put<Evaluation>(`/api/evaluations/${id}`, data);
    return response.data;
  },

  async deleteEvaluation(id: number): Promise<void> {
    await api.delete(`/api/evaluations/${id}`);
  },

  // Résultats
  async getAllResultats(): Promise<ResultatSoutenance[]> {
    const response = await api.get<ResultatSoutenance[]>('/api/resultats');
    return response.data;
  },

  async getResultatBySoutenance(soutenanceId: number): Promise<ResultatSoutenance> {
    const response = await api.get<ResultatSoutenance>(`/api/resultats/soutenances/${soutenanceId}`);
    return response.data;
  },

  async getResultatsByEtudiant(etudiantId: number): Promise<ResultatSoutenance[]> {
    const response = await api.get<ResultatSoutenance[]>(`/api/resultats/etudiants/${etudiantId}`);
    return response.data;
  },

  calculateMention(moyenne: number): MentionFinale {
    if (moyenne >= 16) return 'EXCELLENT';
    if (moyenne >= 14) return 'TRES_BIEN';
    if (moyenne >= 12) return 'BIEN';
    if (moyenne >= 10) return 'ASSEZ_BIEN';
    return 'PASSABLE';
  }
};
