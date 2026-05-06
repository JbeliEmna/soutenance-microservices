'use client';

import { useState, useEffect } from 'react';
import { juryService } from '@/lib/jury-service';
import { soutenanceService } from '@/lib/soutenance-service';
import { AffectationJury, MembreJury, Soutenance, RoleJury } from '@/types';
import { UserCheck, Plus, Trash2, Calendar, User, ShieldAlert, X, Search, Check } from 'lucide-react';
import { cn } from '@/lib/utils';

export default function AffectationJuryModule() {
  const [affectations, setAffectations] = useState<AffectationJury[]>([]);
  const [membres, setMembres] = useState<MembreJury[]>([]);
  const [soutenances, setSoutenances] = useState<Soutenance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  
  // Selection state
  const [selection, setSelection] = useState<{
    soutenanceId: number;
    role: RoleJury;
  } | null>(null);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [affData, memData, soutData] = await Promise.all([
        juryService.getAllAffectations(),
        juryService.getAllMembres(),
        soutenanceService.getAllSoutenances()
      ]);
      setAffectations(affData);
      setMembres(memData);
      setSoutenances(soutData);
    } catch (error) {
      console.error('Failed to fetch affectation data', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleAffect = async (idEnseignant: number) => {
    if (!selection) return;

    try {
      setIsSubmitting(true);
      await juryService.createAffectation({
        idSoutenance: selection.soutenanceId,
        idEnseignant,
        roleJury: selection.role
      });
      await fetchData();
      setSelection(null);
    } catch (error: any) {
      console.error('Failed to affect jury member', error);
      alert(error.response?.data?.message || 'Erreur lors de l\'affectation (conflit possible).');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRemove = async (id: string) => {
    if (!confirm('Voulez-vous retirer ce membre de cette soutenance ?')) return;

    try {
      setIsSubmitting(true);
      await juryService.deleteAffectation(id);
      await fetchData();
    } catch (error) {
      console.error('Failed to remove affectation', error);
      alert('Erreur lors du retrait du membre.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const filteredMembres = membres.filter(m => 
    `${m.nom} ${m.prenom} ${m.grade}`.toLowerCase().includes(searchQuery.toLowerCase())
  );

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500 font-bold">Chargement du module d'affectations...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="bg-amber-50 border border-amber-200 p-4 rounded-xl flex items-start gap-3">
        <ShieldAlert className="w-5 h-5 text-amber-600 mt-0.5" />
        <div>
          <h4 className="text-amber-900 font-bold text-sm">Gestion des Commissions</h4>
          <p className="text-amber-700 text-xs mt-1">
            Chaque soutenance doit avoir un Président, un Rapporteur et un Examinateur. 
            Les conflits d'horaires sont vérifiés automatiquement par le service backend.
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4">
        {soutenances.map(sout => (
          <div key={sout.id} className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
            <div className="p-4 bg-slate-50 border-b border-slate-200 flex justify-between items-center">
              <div className="flex items-center gap-3">
                <Calendar className="w-5 h-5 text-blue-600" />
                <span className="font-black text-slate-900">Soutenance #{sout.id}</span>
                <span className="text-xs font-bold text-slate-500 bg-slate-200 px-2 py-0.5 rounded-full">
                  {new Date(sout.dateDebut).toLocaleDateString()}
                </span>
              </div>
            </div>
            <div className="p-4 grid grid-cols-1 md:grid-cols-3 gap-4">
              {(['president', 'rapporteur', 'examinateur'] as RoleJury[]).map(role => {
                const aff = affectations.find(a => a.idSoutenance === sout.id && a.roleJury === role);
                const membre = aff ? membres.find(m => m.idEnseignant === aff.idEnseignant) : null;

                return (
                  <div 
                    key={role} 
                    onClick={() => !membre && setSelection({ soutenanceId: sout.id, role })}
                    className={cn(
                      "p-4 rounded-xl border-2 border-dashed flex flex-col items-center justify-center text-center gap-2 transition-all",
                      membre 
                        ? "border-blue-100 bg-blue-50/30" 
                        : "border-slate-200 bg-slate-50/50 hover:border-blue-300 hover:bg-blue-50/20 cursor-pointer group"
                    )}
                  >
                    <span className="text-[10px] font-black uppercase tracking-widest text-slate-400 group-hover:text-blue-500 transition-colors">{role}</span>
                    {membre ? (
                      <>
                        <User className="w-8 h-8 text-blue-600" />
                        <p className="font-bold text-slate-900 text-sm leading-tight">{membre.prenom} {membre.nom}</p>
                        <p className="text-[10px] text-slate-400 font-bold">{membre.grade}</p>
                        <button 
                          onClick={(e) => {
                            e.stopPropagation();
                            handleRemove(aff.id);
                          }}
                          className="text-[10px] font-black uppercase text-red-600 hover:text-red-700 mt-2 flex items-center gap-1"
                        >
                          <Trash2 className="w-3 h-3" /> Retirer
                        </button>
                      </>
                    ) : (
                      <>
                        <Plus className="w-8 h-8 text-slate-300 group-hover:text-blue-400 transition-colors" />
                        <p className="text-xs font-bold text-slate-400 group-hover:text-blue-600 transition-colors">Affecter</p>
                      </>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))}
      </div>

      {/* Teacher Selection Modal */}
      {selection && (
        <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-[2rem] shadow-2xl w-full max-w-lg overflow-hidden animate-in zoom-in-95 duration-200">
            <div className="p-6 border-b border-slate-100 flex items-center justify-between">
              <div>
                <h3 className="text-xl font-black text-slate-900">Choisir un Enseignant</h3>
                <p className="text-sm text-slate-500 font-bold">Rôle : <span className="text-blue-600 uppercase">{selection.role}</span></p>
              </div>
              <button 
                onClick={() => setSelection(null)}
                className="p-2 hover:bg-slate-100 rounded-full transition-colors"
              >
                <X className="w-6 h-6 text-slate-400" />
              </button>
            </div>

            <div className="p-4 bg-slate-50 border-b border-slate-100">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input 
                  type="text" 
                  placeholder="Rechercher par nom ou grade..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 bg-white border border-slate-200 rounded-xl text-sm font-medium focus:ring-2 focus:ring-blue-500 outline-none"
                  autoFocus
                />
              </div>
            </div>

            <div className="max-h-96 overflow-y-auto p-4 space-y-2">
              {filteredMembres.length > 0 ? (
                filteredMembres.map(membre => (
                  <button
                    key={membre.id}
                    disabled={isSubmitting}
                    onClick={() => handleAffect(membre.idEnseignant)}
                    className="w-full flex items-center justify-between p-4 bg-white border border-slate-100 hover:border-blue-500 hover:bg-blue-50 rounded-2xl transition-all group"
                  >
                    <div className="flex items-center gap-4">
                      <div className="w-10 h-10 bg-slate-100 rounded-xl flex items-center justify-center group-hover:bg-blue-100">
                        <User className="w-5 h-5 text-slate-400 group-hover:text-blue-600" />
                      </div>
                      <div className="text-left">
                        <p className="font-bold text-slate-900">{membre.prenom} {membre.nom}</p>
                        <p className="text-xs text-slate-500 font-bold uppercase tracking-wider">{membre.grade}</p>
                      </div>
                    </div>
                    <Check className="w-5 h-5 text-blue-600 opacity-0 group-hover:opacity-100" />
                  </button>
                ))
              ) : (
                <div className="p-8 text-center">
                  <p className="text-slate-400 font-bold italic">Aucun enseignant trouvé.</p>
                </div>
              )}
            </div>
            
            <div className="p-4 bg-slate-50 text-center">
              <p className="text-[10px] text-slate-400 font-black uppercase tracking-widest">
                {filteredMembres.length} Enseignants disponibles
              </p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
