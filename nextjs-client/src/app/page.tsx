'use client';

import { useEffect, useState } from 'react';
import api from '@/lib/api-client';
import { authService } from '@/lib/auth-service';
import { Activity, CheckCircle, XCircle, LogOut, User as UserIcon } from 'lucide-react';
import { AuthResponse } from '@/types';
import Link from 'next/link';

export default function Home() {
  const [status, setStatus] = useState<'loading' | 'up' | 'down'>('loading');
  const [user, setUser] = useState<AuthResponse | null>(null);

  useEffect(() => {
    setUser(authService.getCurrentUser());

    const checkBackend = async () => {
      try {
        const baseUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8089';
        const response = await fetch(`${baseUrl}/actuator/health`, {
          cache: 'no-store',
          mode: 'cors'
        });
        
        if (response.ok) {
          setStatus('up');
        } else {
          // Fallback to 127.0.0.1 if localhost fails
          const fallbackResponse = await fetch(`http://127.0.0.1:8089/actuator/health`, {
            cache: 'no-store',
            mode: 'cors'
          });
          if (fallbackResponse.ok) setStatus('up');
          else setStatus('down');
        }
      } catch (error) {
        setStatus('down');
      }
    };

    checkBackend();
  }, []);

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <header className="border-b pb-4 flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900">Gestion des Soutenances</h1>
          <p className="text-slate-500">Tableau de bord de l'administration</p>
        </div>
        {user && (
          <div className="flex items-center space-x-4">
            <div className="text-right hidden sm:block">
              <p className="text-sm font-semibold text-slate-900">{user.prenom} {user.nom}</p>
              <p className="text-xs text-slate-500 font-medium">{user.role}</p>
            </div>
            <button
              onClick={() => authService.logout()}
              className="p-2 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-full transition-colors"
              title="Déconnexion"
            >
              <LogOut className="w-5 h-5" />
            </button>
          </div>
        )}
      </header>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
        {/* Card: Backend Status */}
        <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <Activity className="w-5 h-5 text-blue-500" />
              <h2 className="font-semibold text-xs uppercase tracking-wider text-slate-500">
                Statut Backend
              </h2>
            </div>
            {status === 'loading' && <span className="h-2 w-2 rounded-full bg-slate-300 animate-pulse" />}
            {status === 'up' && <CheckCircle className="w-5 h-5 text-green-500" />}
            {status === 'down' && <XCircle className="w-5 h-5 text-red-500" />}
          </div>
          <div className="mt-4">
            <p className="text-2xl font-bold text-slate-900">
              {status === 'loading' ? 'Vérification...' : status === 'up' ? 'Connecté' : 'Erreur'}
            </p>
            <p className="text-xs text-slate-400 mt-1">
              Gateway API (Port 8089)
            </p>
          </div>
        </div>

        {/* Card: User Profile */}
        {user && (
          <div className="p-6 bg-white rounded-xl shadow-sm border border-slate-200">
            <div className="flex items-center space-x-2 mb-4">
              <UserIcon className="w-5 h-5 text-purple-500" />
              <h2 className="font-semibold text-xs uppercase tracking-wider text-slate-500">
                Session Active
              </h2>
            </div>
            <p className="text-lg font-bold text-slate-900 truncate">{user.email}</p>
            <span className="mt-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-purple-100 text-purple-800">
              {user.role.replace('ROLE_', '')}
            </span>
          </div>
        )}
      </div>

      <section className="bg-white p-8 rounded-xl border border-slate-200 shadow-sm">
        <h3 className="font-bold text-slate-900 mb-6 text-xl">Fonctionnalités disponibles</h3>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-slate-600">
          <Link href="/planning/salles" className="p-4 rounded-lg bg-slate-50 border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all group">
            <p className="font-semibold text-slate-900 mb-1 group-hover:text-blue-600">Planification</p>
            <p className="text-sm">Gérer les dates, les salles et les créneaux horaires.</p>
          </Link>
          <Link href="/jury" className="p-4 rounded-lg bg-slate-50 border border-slate-100 hover:border-blue-200 hover:bg-blue-50 transition-all group">
            <p className="font-semibold text-slate-900 mb-1 group-hover:text-blue-600">Jurys</p>
            <p className="text-sm">Affecter les présidents, rapporteurs et examinateurs.</p>
          </Link>
          <div className="p-4 rounded-lg bg-slate-50 border border-slate-100">
            <p className="font-semibold text-slate-900 mb-1">Évaluations</p>
            <p className="text-sm">Saisir les notes et consulter les délibérations.</p>
          </div>
          <div className="p-4 rounded-lg bg-slate-50 border border-slate-100">
            <p className="font-semibold text-slate-900 mb-1">Résultats</p>
            <p className="text-sm">Calcul automatique des moyennes et mentions.</p>
          </div>
        </div>
      </section>
    </div>
  );
}
