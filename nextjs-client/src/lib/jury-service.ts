import api from './api-client';
import { 
  MembreJury, 
  CreerMembreJuryRequest, 
  ReponseJuryDTO, 
  AffectationJury, 
  CreerAffectationRequest 
} from '@/types';

export const juryService = {
  // Membres Jury
  async getAllMembres(): Promise<MembreJury[]> {
    const response = await api.get<MembreJury[]>('/api/membres-jury');
    return response.data;
  },

  async getMembreById(id: string): Promise<MembreJury> {
    const response = await api.get<MembreJury>(`/api/membres-jury/${id}`);
    return response.data;
  },

  async createMembre(data: CreerMembreJuryRequest): Promise<MembreJury> {
    const response = await api.post<MembreJury>('/api/membres-jury', data);
    return response.data;
  },

  async updateMembre(id: string, data: CreerMembreJuryRequest): Promise<MembreJury> {
    const response = await api.put<MembreJury>(`/api/membres-jury/${id}`, data);
    return response.data;
  },

  async deleteMembre(id: string): Promise<ReponseJuryDTO> {
    const response = await api.delete<ReponseJuryDTO>(`/api/membres-jury/${id}`);
    return response.data;
  },

  // Affectations Jury
  async getAllAffectations(): Promise<AffectationJury[]> {
    const response = await api.get<AffectationJury[]>('/api/affectations-jury');
    return response.data;
  },

  async getAffectationById(id: string): Promise<AffectationJury> {
    const response = await api.get<AffectationJury>(`/api/affectations-jury/${id}`);
    return response.data;
  },

  async createAffectation(data: CreerAffectationRequest): Promise<AffectationJury> {
    const response = await api.post<AffectationJury>('/api/affectations-jury', data);
    return response.data;
  },

  async deleteAffectation(id: string): Promise<ReponseJuryDTO> {
    const response = await api.delete<ReponseJuryDTO>(`/api/affectations-jury/${id}`);
    return response.data;
  },

  async getAffectationsBySoutenance(idSoutenance: number): Promise<AffectationJury[]> {
    const response = await api.get<AffectationJury[]>(`/api/affectations-jury/soutenance/${idSoutenance}`);
    return response.data;
  }
};
