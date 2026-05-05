'use client';

import { useState } from 'react';
import { Salle } from '@/types';
import { X, Save, Home } from 'lucide-react';

interface SalleFormProps {
  initialData?: Salle;
  onSubmit: (data: { nom: string }) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
}

export default function SalleForm({ initialData, onSubmit, onCancel, isLoading }: SalleFormProps) {
  const [nom, setNom] = useState(initialData?.nom || '');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({ nom });
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Home className="w-5 h-5 text-blue-500" />
          {initialData ? 'Modifier la salle' : 'Ajouter une salle'}
        </h3>
        <button onClick={onCancel} className="text-slate-400 hover:text-slate-600 transition-colors">
          <X className="w-5 h-5" />
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Nom de la salle</label>
          <input
            type="text"
            required
            placeholder="ex: Salle A1, Amphi B..."
            value={nom}
            onChange={(e) => setNom(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            autoFocus
          />
        </div>

        <div className="pt-4 flex justify-end gap-3">
          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
          >
            Annuler
          </button>
          <button
            type="submit"
            disabled={isLoading || !nom.trim()}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Save className="w-4 h-4" />
            {isLoading ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </div>
      </form>
    </div>
  );
}
