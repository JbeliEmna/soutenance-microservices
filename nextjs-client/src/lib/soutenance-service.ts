import api from './api-client';
import { Salle, Soutenance } from '@/types';

export const soutenanceService = {
  // Salles CRUD
  async getAllSalles(): Promise<Salle[]> {
    const response = await api.get<Salle[]>('/api/salles');
    return response.data;
  },

  async getSalleById(id: number): Promise<Salle> {
    const response = await api.get<Salle>(`/api/salles/${id}`);
    return response.data;
  },

  async createSalle(data: { nom: string }): Promise<Salle> {
    const response = await api.post<Salle>('/api/salles', data);
    return response.data;
  },

  async updateSalle(id: number, data: { nom: string }): Promise<Salle> {
    const response = await api.put<Salle>(`/api/salles/${id}`, data);
    return response.data;
  },

  async deleteSalle(id: number): Promise<void> {
    await api.delete(`/api/salles/${id}`);
  },

  // Soutenances CRUD (Basics)
  async getAllSoutenances(): Promise<Soutenance[]> {
    const response = await api.get<Soutenance[]>('/api/soutenances');
    return response.data;
  },

  async getSoutenanceById(id: number): Promise<Soutenance> {
    const response = await api.get<Soutenance>(`/api/soutenances/${id}`);
    return response.data;
  },

  async createSoutenance(data: any): Promise<Soutenance> {
    const response = await api.post<Soutenance>('/api/soutenances', data);
    return response.data;
  },

  async updateSoutenance(id: number, data: any): Promise<Soutenance> {
    const response = await api.put<Soutenance>(`/api/soutenances/${id}`, data);
    return response.data;
  },

  async updateEtat(id: number, etat: string): Promise<Soutenance> {
    const response = await api.patch<Soutenance>(`/api/soutenances/${id}/etat`, { etat });
    return response.data;
  },

  async deleteSoutenance(id: number): Promise<void> {
    await api.delete(`/api/soutenances/${id}`);
  }
};
