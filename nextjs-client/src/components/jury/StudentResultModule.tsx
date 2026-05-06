'use client';

import { useState, useEffect } from 'react';
import { notesService } from '@/lib/notes-service';
import { authService } from '@/lib/auth-service';
import { ResultatSoutenance, AuthResponse } from '@/types';
import { Award, TrendingUp, Calendar, BookOpen, CheckCircle2, ShieldAlert } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function StudentResultModule() {
  const [resultat, setResultat] = useState<ResultatSoutenance | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [user, setUser] = useState<AuthResponse | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsLoading(true);
        const currentUser = authService.getCurrentUser();
        setUser(currentUser);
        
        if (currentUser && (currentUser.externalId !== undefined && currentUser.externalId !== null)) {
          const res = await notesService.getResultatsByEtudiant(currentUser.externalId);
          if (Array.isArray(res) && res.length > 0) {
            setResultat(res[0]);
          } else {
            setResultat(null);
          }
        } else {
          console.warn('Current user missing externalId', currentUser);
          setResultat(null);
        }
      } catch (error) {
        console.error('Failed to fetch student result', error);
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, []);

  if (isLoading) {
    return <div className="p-12 text-center text-slate-500 font-bold">Chargement de votre résultat...</div>;
  }

  if (user && (user.externalId === undefined || user.externalId === null)) {
    return (
      <div className="bg-amber-50 border-2 border-dashed border-amber-200 rounded-3xl p-12 text-center space-y-4">
        <div className="w-16 h-16 bg-amber-100 rounded-2xl flex items-center justify-center mx-auto">
          <ShieldAlert className="w-8 h-8 text-amber-600" />
        </div>
        <div className="max-w-xs mx-auto">
          <h3 className="text-amber-900 font-black text-xl">Profil Incomplet</h3>
          <p className="text-amber-700 font-medium text-sm mt-2">
            Votre compte ne possède pas d'identifiant étudiant (externalId). Veuillez contacter l'administrateur.
          </p>
        </div>
      </div>
    );
  }

  if (!resultat) {
    return (
      <div className="bg-white border-2 border-dashed border-slate-200 rounded-3xl p-12 text-center space-y-4">
        <div className="w-16 h-16 bg-slate-50 rounded-2xl flex items-center justify-center mx-auto">
          <Calendar className="w-8 h-8 text-slate-300" />
        </div>
        <div className="max-w-xs mx-auto">
          <h3 className="text-slate-900 font-black text-xl">Résultat non disponible</h3>
          <p className="text-slate-500 font-medium text-sm mt-2">
            Votre soutenance n'a pas encore été évaluée ou la délibération est en cours.
          </p>
        </div>
      </div>
    );
  }

  const getMentionColor = (mention: string) => {
    switch (mention) {
      case 'EXCELLENT': return 'text-emerald-600 bg-emerald-50 border-emerald-100';
      case 'TRES_BIEN': return 'text-blue-600 bg-blue-50 border-blue-100';
      case 'BIEN': return 'text-indigo-600 bg-indigo-50 border-indigo-100';
      default: return 'text-slate-600 bg-slate-50 border-slate-100';
    }
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
      {/* Note Card */}
      <div className="md:col-span-2 bg-slate-900 rounded-[2.5rem] p-10 text-white relative overflow-hidden">
        <div className="relative z-10 space-y-8">
          <div className="flex items-center gap-4">
            <div className="w-12 h-12 bg-white/10 rounded-2xl flex items-center justify-center backdrop-blur-md">
              <Award className="w-6 h-6 text-blue-400" />
            </div>
            <div>
              <p className="text-slate-400 text-xs font-black uppercase tracking-[0.2em]">Félicitations</p>
              <h3 className="text-2xl font-black">{user?.prenom}, votre note est disponible</h3>
            </div>
          </div>

          <div className="flex items-end gap-2">
            <span className="text-8xl font-black tracking-tighter">
              {resultat.noteFinale !== undefined && resultat.noteFinale !== null 
                ? resultat.noteFinale.toFixed(2) 
                : 'N/A'}
            </span>
            <span className="text-3xl font-black text-slate-500 mb-4">/ 20</span>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="bg-white/5 rounded-2xl p-4 border border-white/10">
              <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-1">Mention</p>
              <p className="text-lg font-black text-blue-400">{resultat.mention}</p>
            </div>
            <div className="bg-white/5 rounded-2xl p-4 border border-white/10">
              <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-1">Statut</p>
              <p className="text-lg font-black text-emerald-400 flex items-center gap-2">
                Validé <CheckCircle2 className="w-5 h-5" />
              </p>
            </div>
          </div>
        </div>
        
        {/* Decoration */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-blue-600/20 blur-[100px] rounded-full -mr-32 -mt-32" />
        <TrendingUp className="absolute -right-10 -bottom-10 w-64 h-64 text-white/[0.03] rotate-12" />
      </div>

      {/* Info Card */}
      <div className="bg-white border border-slate-200 rounded-[2.5rem] p-8 space-y-6">
        <h4 className="text-slate-900 font-black text-lg flex items-center gap-2">
          <BookOpen className="w-5 h-5 text-blue-600" />
          Prochaines étapes
        </h4>
        
        <ul className="space-y-4">
          {[
            "Télécharger votre attestation provisoire",
            "Déposer la version finale du mémoire",
            "Récupérer votre quitus administratif",
            "Vérifier vos informations de diplôme"
          ].map((item, i) => (
            <li key={i} className="flex items-start gap-3">
              <div className="w-5 h-5 rounded-full bg-slate-100 flex items-center justify-center text-[10px] font-black text-slate-500 mt-0.5 shrink-0">
                {i + 1}
              </div>
              <p className="text-sm font-bold text-slate-600">{item}</p>
            </li>
          ))}
        </ul>

        <button className="w-full py-4 bg-slate-100 hover:bg-slate-200 text-slate-900 font-black rounded-2xl transition-all">
          Télécharger le PV
        </button>
      </div>
    </div>
  );
}
