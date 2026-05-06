import api from './api-client';
import { AuthResponse, LoginRequest, RegisterRequest, User } from '@/types';

export const authService = {
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/login', credentials);
    if (response.data.token) {
      localStorage.setItem('token', response.data.token);
      localStorage.setItem('user', JSON.stringify(response.data));
    }
    return response.data;
  },

  async register(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/register', data);
    return response.data;
  },

  async adminCreateUser(data: RegisterRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/api/auth/admin/create-user', data);
    return response.data;
  },

  async getMe(): Promise<string> {
    const response = await api.get<string>('/api/auth/me');
    return response.data;
  },

  async getAllStudents(): Promise<User[]> {
    const response = await api.get<User[]>('/api/users/internal/etudiants');
    return response.data;
  },

  async getAllTeachers(): Promise<User[]> {
    const response = await api.get<User[]>('/api/users/internal/enseignants');
    return response.data;
  },

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
  },

  getCurrentUser(): AuthResponse | null {
    if (typeof window !== 'undefined') {
      const user = localStorage.getItem('user');
      return user ? JSON.parse(user) : null;
    }
    return null;
  },

  isAuthenticated(): boolean {
    if (typeof window !== 'undefined') {
      return !!localStorage.getItem('token');
    }
    return false;
  }
};
