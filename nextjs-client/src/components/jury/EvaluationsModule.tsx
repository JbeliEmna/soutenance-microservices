'use client';

import { useState, useEffect } from 'react';
import { notesService } from '@/lib/notes-service';
import { soutenanceService } from '@/lib/soutenance-service';
import { juryService } from '@/lib/jury-service';
import { authService } from '@/lib/auth-service';
import { Evaluation, Soutenance, AffectationJury, MembreJury, User as UserType } from '@/types';
import { ClipboardList, Plus, Star, Calculator, User, X, GraduationCap, School } from 'lucide-react';
import { cn } from '@/lib/utils';
import GradeForm from './GradeForm';

export default function EvaluationsModule() {
  const [soutenances, setSoutenances] = useState<Soutenance[]>([]);
  const [evaluations, setEvaluations] = useState<Evaluation[]>([]);
  const [affectations, setAffectations] = useState<AffectationJury[]>([]);
  const [membres, setMembres] = useState<MembreJury[]>([]);
  const [students, setStudents] = useState<UserType[]>([]);
  const [teachers, setTeachers] = useState<UserType[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [editingGrade, setEditingGrade] = useState<{
    soutenanceId: number;
    enseignantId: number;
    roleJury: string;
    initialGrade?: number;
  } | null>(null);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      const [soutData, evalData, affData, memData, studData, teachData] = await Promise.all([
        soutenanceService.getAllSoutenances(),
        notesService.getAllEvaluations(),
        juryService.getAllAffectations(),
        juryService.getAllMembres(),
        authService.getAllStudents(),
        authService.getAllTeachers()
      ]);

      setSoutenances(soutData);
      setEvaluations(evalData);
      setAffectations(affData);
      setMembres(memData);
      setStudents(studData);
      setTeachers(teachData);
    } catch (error) {
      console.error('Failed to fetch evaluation data', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleSaveGrade = async (note: number) => {
    if (!editingGrade) return;

    try {
      const existingEval = evaluations.find(
        e => e.soutenanceId === editingGrade.soutenanceId && e.enseignantId === editingGrade.enseignantId
      );

      const payload = {
        soutenanceId: editingGrade.soutenanceId,
        enseignantId: editingGrade.enseignantId,
        roleJury: editingGrade.roleJury.toUpperCase(),
        note: note
      };

      if (existingEval) {
        await notesService.updateEvaluation(existingEval.id, payload);
      } else {
        await notesService.createEvaluation(payload);
      }
      
      setEditingGrade(null);
      await fetchData();
    } catch (error) {
      console.error('Failed to save grade', error);
      throw error;
    }
  };

  if (isLoading) {
    return <div className="p-8 text-center text-slate-500 font-bold">Chargement des évaluations...</div>;
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6">
        {soutenances.map(sout => {
          const soutEvals = evaluations.filter(e => e.soutenanceId === sout.id);
          const soutAffs = affectations.filter(a => a.idSoutenance === sout.id);

          const validEvals = soutEvals.filter(e => e.note !== undefined && e.note !== null);
          const moyenne = validEvals.length > 0
            ? (validEvals.reduce((acc, curr) => acc + curr.note, 0) / validEvals.length).toFixed(2)
            : null;

          const soutStudents = students.filter(s => sout.etudiantIds.includes(s.externalId));
          const encadrant = membres.find(m => m.idEnseignant === sout.encadrantId) || teachers.find(t => t.externalId === sout.encadrantId);

          return (
            <div key={sout.id} className="bg-white border border-slate-200 rounded-3xl overflow-hidden shadow-sm hover:shadow-md transition-shadow">
              <div className="p-6 border-b border-slate-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div className="flex items-center gap-4">
                  <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center">
                    <ClipboardList className="w-6 h-6 text-blue-600" />
                  </div>
                  <div>
                    <h3 className="font-black text-slate-900">Soutenance #{sout.id}</h3>
                    <p className="text-sm text-slate-500 font-bold">
                      {new Date(sout.dateDebut).toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}
                    </p>
                  </div>
                </div>

                {moyenne && (
                  <div className="bg-slate-900 px-6 py-3 rounded-2xl flex items-center gap-3 text-white">
                    <Calculator className="w-5 h-5 text-blue-400" />
                    <div>
                      <p className="text-[10px] font-black uppercase tracking-widest text-slate-400 leading-none mb-1">Moyenne Actuelle</p>
                      <p className="text-xl font-black leading-none">{moyenne}/20</p>
                    </div>
                  </div>
                )}
              </div>

              {/* Nouveau bloc: Etudiants & Encadrant */}
              <div className="px-6 py-4 bg-white border-b border-slate-100 grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-slate-400">
                    <GraduationCap className="w-3 h-3" />
                    Étudiants
                  </div>
                  <div className="flex flex-wrap gap-2">
                    {soutStudents.length > 0 ? soutStudents.map(student => (
                      <span key={student.id} className="inline-flex items-center px-3 py-1 rounded-full bg-slate-100 text-slate-700 text-xs font-bold">
                        {student.prenom} {student.nom}
                      </span>
                    )) : <span className="text-xs text-slate-400 italic">Aucun étudiant assigné</span>}
                  </div>
                </div>

                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-[10px] font-black uppercase tracking-widest text-slate-400">
                    <School className="w-3 h-3" />
                    Encadrant
                  </div>
                  <div>
                    {encadrant ? (
                      <span className="inline-flex items-center px-3 py-1 rounded-full bg-blue-50 text-blue-700 text-xs font-bold">
                        {encadrant.prenom} {encadrant.nom}
                      </span>
                    ) : (
                      <span className="text-xs text-slate-400 italic">Non spécifié (ID: {sout.encadrantId})</span>
                    )}
                  </div>
                </div>
              </div>

              <div className="p-6 bg-slate-50/50">
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  {soutAffs.map(aff => {
                    const eval_ = soutEvals.find(e => e.enseignantId === aff.idEnseignant);
                    const membre = membres.find(m => m.idEnseignant === aff.idEnseignant);
                    const isEditing = editingGrade?.soutenanceId === sout.id && editingGrade?.enseignantId === aff.idEnseignant;

                    return (
                      <div key={aff.id} className="bg-white p-4 rounded-2xl border border-slate-200 flex flex-col gap-3">
                        <div className="flex items-center justify-between">
                          <span className="text-[10px] font-black uppercase tracking-widest text-blue-600 bg-blue-50 px-2 py-1 rounded-lg">
                            {aff.roleJury}
                          </span>
                          {eval_ && (
                            <div className="flex items-center gap-1 text-amber-500">
                              <Star className="w-3 h-3 fill-current" />
                              <span className="text-xs font-black">Saisi</span>
                            </div>
                          )}
                        </div>
                        
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 bg-slate-100 rounded-full flex items-center justify-center text-slate-500">
                            <User className="w-4 h-4" />
                          </div>
                          <p className="text-sm font-bold text-slate-900 truncate">
                            {membre ? `${membre.prenom} ${membre.nom}` : `ID: ${aff.idEnseignant}`}
                          </p>
                        </div>

                        <div className="mt-2 pt-3 border-t border-slate-50 flex items-center justify-between h-10">
                          {isEditing ? (
                            <GradeForm
                              soutenanceId={sout.id}
                              enseignantId={aff.idEnseignant}
                              roleJury={aff.roleJury}
                              initialGrade={eval_?.note}
                              onSave={handleSaveGrade}
                              onCancel={() => setEditingGrade(null)}
                            />
                          ) : (
                            <>
                              <span className="text-xs font-bold text-slate-400">Note :</span>
                              {eval_ ? (
                                <div className="flex items-center gap-3">
                                  <span className="text-lg font-black text-slate-900">{eval_.note}/20</span>
                                  <button 
                                    onClick={() => setEditingGrade({
                                      soutenanceId: sout.id,
                                      enseignantId: aff.idEnseignant,
                                      roleJury: aff.roleJury,
                                      initialGrade: eval_.note
                                    })}
                                    className="text-[10px] font-bold text-slate-400 hover:text-blue-600 uppercase"
                                  >
                                    Modifier
                                  </button>
                                </div>
                              ) : (
                                <button 
                                  onClick={() => setEditingGrade({
                                    soutenanceId: sout.id,
                                    enseignantId: aff.idEnseignant,
                                    roleJury: aff.roleJury
                                  })}
                                  className="text-xs font-black text-blue-600 hover:text-blue-700 flex items-center gap-1"
                                >
                                  <Plus className="w-3 h-3" /> Saisir la note
                                </button>
                              )}
                            </>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
