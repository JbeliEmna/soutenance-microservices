'use client';

import { useState, useEffect } from 'react';
import { soutenanceService } from '@/lib/soutenance-service';
import { juryService } from '@/lib/jury-service';
import { notesService } from '@/lib/notes-service';
import { Soutenance, AffectationJury, Evaluation } from '@/types';
import { Calendar, UserCheck, Star, Clock, MapPin, Search } from 'lucide-react';
import GradeForm from '@/components/jury/GradeForm';

export default function JurySoutenancesPage() {
  const [soutenances, setSoutenances] = useState<Soutenance[]>([]);
  const [affectations, setAffectations] = useState<AffectationJury[]>([]);
  const [evaluations, setEvaluations] = useState<Evaluation[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [editingGrade, setEditingGrade] = useState<{ soutenanceId: number, enseignantId: number } | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [soutenancesData, affectationsData] = await Promise.all([
        soutenanceService.getAllSoutenances(),
        juryService.getAllAffectations()
      ]);
      setSoutenances(soutenancesData);
      setAffectations(affectationsData);

      // Fetch all evaluations for these soutenances
      const allEvaluations = await Promise.all(
        soutenancesData.map(s => notesService.getEvaluationsBySoutenance(s.id))
      );
      setEvaluations(allEvaluations.flat());
    } catch (error) {
      console.error('Failed to fetch data', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSaveGrade = async (soutenanceId: number, enseignantId: number, roleJury: string, note: number) => {
    const roleJuryUpper = roleJury.toUpperCase();
    const existingEval = evaluations.find(e => e.soutenanceId === soutenanceId && e.enseignantId === enseignantId);
    
    try {
      if (existingEval) {
        await notesService.updateEvaluation(existingEval.id, { soutenanceId, enseignantId, roleJury: roleJuryUpper, note });
      } else {
        await notesService.createEvaluation({ soutenanceId, enseignantId, roleJury: roleJuryUpper, note });
      }
      
      setEditingGrade(null);
      fetchData();
    } catch (err: any) {
      console.error('Failed to save grade', err);
      const message = err.response?.data?.message || 'Erreur lors de l\'enregistrement de la note.';
      alert(message);
    }
  };

  const getSoutenanceAffectations = (soutenanceId: number) => {
    return affectations.filter(a => a.idSoutenance === soutenanceId);
  };

  const getEvaluation = (soutenanceId: number, enseignantId: number) => {
    return evaluations.find(e => e.soutenanceId === soutenanceId && e.enseignantId === enseignantId);
  };

  const calculateResults = (soutenanceId: number) => {
    const soutenanceEvals = evaluations.filter(e => e.soutenanceId === soutenanceId);
    if (soutenanceEvals.length === 0) return null;
    
    const sum = soutenanceEvals.reduce((acc, curr) => acc + curr.note, 0);
    const moyenne = sum / soutenanceEvals.length;
    const mention = notesService.calculateMention(moyenne);
    
    return { moyenne, mention, count: soutenanceEvals.length };
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  };

  const filteredSoutenances = soutenances.filter(s => 
    s.salle.toLowerCase().includes(searchTerm.toLowerCase()) ||
    s.id.toString().includes(searchTerm)
  );

  return (
    <div className="max-w-6xl mx-auto space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-slate-900 flex items-center gap-3">
          <UserCheck className="w-8 h-8 text-blue-600" />
          Saisie des Notes Jury
        </h1>
        <p className="text-slate-500 mt-1">Saisir et modifier les notes pour les soutenances affectées.</p>
      </div>

      <div className="relative">
        <Search className="w-5 h-5 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <input
          type="text"
          placeholder="Rechercher une soutenance (salle, ID)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none shadow-sm"
        />
      </div>

      {isLoading ? (
        <div className="grid gap-6">
          {[1, 2, 3].map(i => (
            <div key={i} className="h-48 bg-slate-100 animate-pulse rounded-xl" />
          ))}
        </div>
      ) : filteredSoutenances.length > 0 ? (
        <div className="grid gap-6">
          {filteredSoutenances.map(soutenance => {
            const soutenanceAffectations = getSoutenanceAffectations(soutenance.id);
            return (
              <div key={soutenance.id} className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden">
                <div className="p-5 border-b border-slate-100 bg-slate-50/50 flex justify-between items-center">
                  <div className="flex gap-6 text-sm">
                    <div className="flex items-center gap-2 text-slate-700">
                      <Calendar className="w-4 h-4 text-slate-400" />
                      <span className="font-medium">{formatDateTime(soutenance.dateDebut)}</span>
                    </div>
                    <div className="flex items-center gap-2 text-slate-700">
                      <MapPin className="w-4 h-4 text-slate-400" />
                      <span className="font-medium">Salle: {soutenance.salle}</span>
                    </div>
                    <div className="text-slate-400">ID: {soutenance.id}</div>
                  </div>
                  <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${
                    soutenance.etat === 'TERMINEE' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'
                  }`}>
                    {soutenance.etat}
                  </span>
                </div>
                
                <div className="p-5">
                  <div className="flex justify-between items-center mb-4">
                    <h3 className="text-sm font-bold uppercase tracking-wider text-slate-500">Membres du Jury & Notes</h3>
                    {calculateResults(soutenance.id) && (
                      <div className="flex gap-4">
                        <div className="flex items-center gap-2 bg-amber-50 px-3 py-1 rounded-lg border border-amber-100">
                          <span className="text-xs font-bold text-amber-700">MOYENNE:</span>
                          <span className="text-sm font-black text-amber-900">{calculateResults(soutenance.id)?.moyenne.toFixed(2)}/20</span>
                        </div>
                        <div className="flex items-center gap-2 bg-purple-50 px-3 py-1 rounded-lg border border-purple-100">
                          <span className="text-xs font-bold text-purple-700">MENTION:</span>
                          <span className="text-sm font-black text-purple-900">{calculateResults(soutenance.id)?.mention}</span>
                        </div>
                      </div>
                    )}
                  </div>
                  {soutenanceAffectations.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      {soutenanceAffectations.map(affectation => {
                        const evaluation = getEvaluation(soutenance.id, affectation.idEnseignant);
                        const isEditing = editingGrade?.soutenanceId === soutenance.id && editingGrade?.enseignantId === affectation.idEnseignant;

                        return (
                          <div key={affectation.id} className="p-4 border border-slate-100 rounded-lg bg-white shadow-sm flex flex-col justify-between">
                            <div>
                              <div className="flex justify-between items-start mb-2">
                                <span className="text-xs font-bold uppercase text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                                  {affectation.roleJury}
                                </span>
                                {evaluation && !isEditing && (
                                  <div className="flex items-center gap-1 text-amber-600 font-bold">
                                    <Star className="w-4 h-4 fill-current" />
                                    {evaluation.note}/20
                                  </div>
                                )}
                              </div>
                              <p className="text-sm font-medium text-slate-900 mb-4">
                                Enseignant ID: {affectation.idEnseignant}
                              </p>
                            </div>

                            {isEditing ? (
                              <GradeForm
                                soutenanceId={soutenance.id}
                                enseignantId={affectation.idEnseignant}
                                roleJury={affectation.roleJury}
                                initialGrade={evaluation?.note}
                                onSave={(note) => handleSaveGrade(soutenance.id, affectation.idEnseignant, affectation.roleJury, note)}
                                onCancel={() => setEditingGrade(null)}
                              />
                            ) : (
                              <button
                                onClick={() => setEditingGrade({ soutenanceId: soutenance.id, enseignantId: affectation.idEnseignant })}
                                className="w-full py-2 text-sm font-medium text-blue-600 border border-blue-100 rounded-lg hover:bg-blue-50 transition-colors"
                              >
                                {evaluation ? 'Modifier la note' : 'Saisir la note'}
                              </button>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  ) : (
                    <p className="text-sm text-slate-400 italic">Aucun jury affecté à cette soutenance.</p>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      ) : (
        <div className="text-center py-20 bg-white border border-dashed border-slate-300 rounded-2xl">
          <p className="text-slate-500">Aucune soutenance trouvée.</p>
        </div>
      )}
    </div>
  );
}
