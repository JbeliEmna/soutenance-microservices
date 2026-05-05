'use client';

import { MembreJury } from '@/types';
import { Edit2, Trash2, Mail, GraduationCap } from 'lucide-react';

interface MembreJuryListProps {
  membres: MembreJury[];
  onEdit: (membre: MembreJury) => void;
  onDelete: (id: string) => Promise<void>;
  isLoading: boolean;
}

export default function MembreJuryList({ membres, onEdit, onDelete, isLoading }: MembreJuryListProps) {
  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-24 bg-slate-100 animate-pulse rounded-xl border border-slate-200" />
        ))}
      </div>
    );
  }

  if (membres.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-xl border border-dashed border-slate-300">
        <p className="text-slate-500">Aucun membre de jury trouvé.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-4">
      {membres.map((membre) => (
        <div
          key={membre.id}
          className="group bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:border-blue-200 hover:shadow-md transition-all flex flex-col sm:flex-row sm:items-center justify-between gap-4"
        >
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-full bg-blue-50 flex items-center justify-center text-blue-600 font-bold text-lg border border-blue-100">
              {membre.nom[0]}{membre.prenom[0]}
            </div>
            <div>
              <h4 className="font-bold text-slate-900 text-lg">
                {membre.grade} {membre.prenom} {membre.nom}
              </h4>
              <div className="flex flex-wrap gap-y-1 gap-x-4 mt-1">
                <div className="flex items-center gap-1.5 text-sm text-slate-500">
                  <Mail className="w-4 h-4 text-slate-400" />
                  {membre.email}
                </div>
                <div className="flex items-center gap-1.5 text-sm text-slate-500">
                  <GraduationCap className="w-4 h-4 text-slate-400" />
                  ID: {membre.idEnseignant}
                </div>
              </div>
            </div>
          </div>

          <div className="flex items-center gap-2 self-end sm:self-center opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={() => onEdit(membre)}
              className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
              title="Modifier"
            >
              <Edit2 className="w-5 h-5" />
            </button>
            <button
              onClick={() => onDelete(membre.id)}
              className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              title="Supprimer"
            >
              <Trash2 className="w-5 h-5" />
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
