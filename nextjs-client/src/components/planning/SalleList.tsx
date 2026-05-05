'use client';

import { Salle } from '@/types';
import { Edit2, Trash2, Home } from 'lucide-react';

interface SalleListProps {
  salles: Salle[];
  onEdit: (salle: Salle) => void;
  onDelete: (id: number) => Promise<void>;
  isLoading: boolean;
}

export default function SalleList({ salles, onEdit, onDelete, isLoading }: SalleListProps) {
  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-16 bg-slate-100 animate-pulse rounded-xl border border-slate-200" />
        ))}
      </div>
    );
  }

  if (salles.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-xl border border-dashed border-slate-300">
        <p className="text-slate-500">Aucune salle trouvée.</p>
      </div>
    );
  }

  return (
    <div className="grid gap-3">
      {salles.map((salle) => (
        <div
          key={salle.id}
          className="group bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:border-blue-200 hover:shadow-md transition-all flex items-center justify-between"
        >
          <div className="flex items-center gap-4">
            <div className="w-10 h-10 rounded-lg bg-blue-50 flex items-center justify-center text-blue-600 border border-blue-100">
              <Home className="w-5 h-5" />
            </div>
            <div>
              <h4 className="font-bold text-slate-900">
                {salle.nom}
              </h4>
              <p className="text-xs text-slate-400">ID: {salle.id}</p>
            </div>
          </div>

          <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              onClick={() => onEdit(salle)}
              className="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
              title="Modifier"
            >
              <Edit2 className="w-4 h-4" />
            </button>
            <button
              onClick={() => onDelete(salle.id)}
              className="p-2 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
              title="Supprimer"
            >
              <Trash2 className="w-4 h-4" />
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}
