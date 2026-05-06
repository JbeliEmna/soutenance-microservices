'use client';

import { useEffect, useState } from 'react';
import { authService } from '@/lib/auth-service';
import { Activity, CheckCircle, XCircle, Users, Calendar, ClipboardCheck, FileText, ArrowRight, ShieldCheck } from 'lucide-react';
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

  const stats = [
    { label: 'Planification', icon: Calendar, color: 'text-blue-600', bg: 'bg-blue-100', href: '/planning', desc: 'Gérer les créneaux et les salles' },
    { label: 'Membres Jury', icon: Users, color: 'text-purple-600', bg: 'bg-purple-100', href: '/jury', desc: 'Gérer les enseignants et affectations' },
    { label: 'Évaluations', icon: ClipboardCheck, color: 'text-green-600', bg: 'bg-green-100', href: '/jury/notes', desc: 'Saisie des notes de soutenance' }
  ];

  return (
    <div className="max-w-6xl mx-auto space-y-10 animate-in fade-in duration-700">
      {/* Welcome Section */}
      <section className="space-y-2">
        <h1 className="text-2xl font-black tracking-tight text-slate-900">
          Bienvenue, <span className="text-blue-600">{user?.prenom || 'Administrateur'}</span> 👋
        </h1>
        <p className="text-lg text-slate-500 font-medium">
          Voici l'état actuel de votre système de gestion des soutenances.
        </p>
      </section>

      
      

      {/* Navigation Grid */}
      <section className="space-y-6">
        <h3 className="font-black text-slate-900 text-xl tracking-tight">Accès Rapide</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {stats.map((item) => (
            <Link 
              key={item.label} 
              href={item.href} 
              className="group p-6 bg-white rounded-2xl border border-slate-200 hover:border-blue-300 hover:shadow-xl hover:shadow-blue-500/5 transition-all relative overflow-hidden"
            >
              <div className={`${item.bg} ${item.color} w-12 h-12 rounded-xl flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                <item.icon className="w-6 h-6" />
              </div>
              <h4 className="font-black text-slate-900 mb-1">{item.label}</h4>
              <p className="text-xs text-slate-500 font-medium leading-relaxed">{item.desc}</p>
              <div className="mt-4 flex items-center text-[10px] font-black uppercase tracking-widest text-blue-600 opacity-0 group-hover:opacity-100 transition-opacity">
                Ouvrir <ArrowRight className="ml-1 w-3 h-3" />
              </div>
              <div className="absolute -right-2 -bottom-2 opacity-[0.03] group-hover:opacity-[0.08] transition-opacity">
                <item.icon className="w-24 h-24" />
              </div>
            </Link>
          ))}
        </div>
      </section>

      {/* Info Box */}
      <footer className="p-8 bg-slate-900 rounded-3xl text-white overflow-hidden relative">
        <div className="relative z-10 space-y-4 max-w-2xl">
          <h4 className="text-2xl font-black">Besoin d'aide ?</h4>
          <p className="text-slate-400 font-medium">
            Le système est conçu pour automatiser la vérification des conflits horaires et le calcul des mentions finales. 
            Toutes les modifications sont synchronisées en temps réel avec les microservices.
          </p>
          <button className="px-6 py-2 bg-blue-600 hover:bg-blue-700 rounded-xl font-bold transition-colors">
            Consulter la documentation
          </button>
        </div>
        <div className="absolute right-0 top-0 h-full w-1/3 bg-gradient-to-l from-blue-600/20 to-transparent" />
        <ShieldCheck className="absolute -right-10 -bottom-10 w-64 h-64 text-white/[0.03]" />
      </footer>
    </div>
  );
}
