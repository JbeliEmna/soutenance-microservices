import api from './api-client';
import { Evaluation, ResultatSoutenance, MentionFinale } from '@/types';

export const notesService = {
  async getEvaluationsBySoutenance(soutenanceId: number): Promise<Evaluation[]> {
    const response = await api.get<Evaluation[]>(`/api/evaluations/soutenance/${soutenanceId}`);
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

  async getResultatBySoutenance(soutenanceId: number): Promise<ResultatSoutenance> {
    const response = await api.get<ResultatSoutenance>(`/api/resultats/soutenance/${soutenanceId}`);
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
