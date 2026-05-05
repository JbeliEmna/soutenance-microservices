'use client';

import { Soutenance } from '@/types';
import { Edit2, Trash2, Calendar, Clock, MapPin, Users, UserCheck } from 'lucide-react';

interface SoutenanceListProps {
  soutenances: Soutenance[];
  onEdit: (soutenance: Soutenance) => void;
  onDelete: (id: number) => Promise<void>;
  isLoading: boolean;
}

export default function SoutenanceList({ soutenances, onEdit, onDelete, isLoading }: SoutenanceListProps) {
  if (isLoading) {
    return (
      <div className="space-y-4">
        {[1, 2, 3].map((i) => (
          <div key={i} className="h-32 bg-slate-100 animate-pulse rounded-xl border border-slate-200" />
        ))}
      </div>
    );
  }

  if (soutenances.length === 0) {
    return (
      <div className="text-center py-12 bg-white rounded-xl border border-dashed border-slate-300">
        <p className="text-slate-500">Aucune soutenance planifiée.</p>
      </div>
    );
  }

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleString('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  };

  const getStatusColor = (etat: string) => {
    switch (etat) {
      case 'PLANIFIEE': return 'bg-blue-100 text-blue-800';
      case 'EN_COURS': return 'bg-amber-100 text-amber-800';
      case 'TERMINEE': return 'bg-green-100 text-green-800';
      default: return 'bg-slate-100 text-slate-800';
    }
  };

  return (
    <div className="grid gap-4">
      {soutenances.map((soutenance) => (
        <div
          key={soutenance.id}
          className="group bg-white p-5 rounded-xl border border-slate-200 shadow-sm hover:border-blue-200 hover:shadow-md transition-all flex flex-col gap-4"
        >
          <div className="flex justify-between items-start">
            <div className="flex items-center gap-2 mb-2">
              <span className={`px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(soutenance.etat)}`}>
                {soutenance.etat}
              </span>
              <span className="text-xs text-slate-400">ID: {soutenance.id}</span>
            </div>
            <div className="flex items-center gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
              <button
                onClick={() => onEdit(soutenance)}
                className="p-1.5 text-slate-400 hover:text-blue-600 hover:bg-blue-50 rounded-lg transition-colors"
                title="Modifier"
              >
                <Edit2 className="w-4 h-4" />
              </button>
              <button
                onClick={() => onDelete(soutenance.id)}
                className="p-1.5 text-slate-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                title="Supprimer"
              >
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
            <div className="space-y-2">
              <div className="flex items-center gap-2 text-slate-700">
                <Calendar className="w-4 h-4 text-slate-400" />
                <span className="font-medium">Début:</span> {formatDateTime(soutenance.dateDebut)}
              </div>
              <div className="flex items-center gap-2 text-slate-700">
                <Clock className="w-4 h-4 text-slate-400" />
                <span className="font-medium">Fin:</span> {formatDateTime(soutenance.dateFin)}
              </div>
              <div className="flex items-center gap-2 text-slate-700">
                <MapPin className="w-4 h-4 text-slate-400" />
                <span className="font-medium">Salle:</span> {soutenance.salle}
              </div>
            </div>
            <div className="space-y-2">
              <div className="flex items-start gap-2 text-slate-700">
                <Users className="w-4 h-4 text-slate-400 mt-0.5" />
                <div>
                  <span className="font-medium block">Étudiants:</span>
                  <span className="text-slate-500">
                    {soutenance.etudiantIds ? soutenance.etudiantIds.join(', ') : 'Aucun étudiant'}
                  </span>
                </div>
              </div>
              <div className="flex items-center gap-2 text-slate-700">
                <UserCheck className="w-4 h-4 text-slate-400" />
                <span className="font-medium">Encadrant:</span> {soutenance.encadrantId}
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
