'use client';

import { useState, useEffect } from 'react';
import { Soutenance, Salle } from '@/types';
import { X, Save, Calendar } from 'lucide-react';

interface SoutenanceFormProps {
  initialData?: Soutenance;
  salles: Salle[];
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
}

export default function SoutenanceForm({ initialData, salles, onSubmit, onCancel, isLoading }: SoutenanceFormProps) {
  const [formData, setFormData] = useState({
    etudiantIds: initialData?.etudiantIds ? initialData.etudiantIds.join(', ') : '',
    encadrantId: initialData?.encadrantId || '',
    salle: initialData?.salle || '',
    dateDebut: initialData?.dateDebut ? initialData.dateDebut.slice(0, 16) : '',
    dateFin: initialData?.dateFin ? initialData.dateFin.slice(0, 16) : '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Parse etudiantIds
    const etudiantIdsArray = formData.etudiantIds
      .split(',')
      .map(id => parseInt(id.trim()))
      .filter(id => !isNaN(id));

    await onSubmit({
      etudiantIds: etudiantIdsArray,
      encadrantId: parseInt(formData.encadrantId.toString()),
      salle: formData.salle,
      dateDebut: formData.dateDebut,
      dateFin: formData.dateFin
    });
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-blue-500" />
          {initialData ? 'Modifier la soutenance' : 'Planifier une soutenance'}
        </h3>
        <button type="button" onClick={onCancel} className="text-slate-400 hover:text-slate-600 transition-colors">
          <X className="w-5 h-5" />
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Date de début</label>
            <input
              type="datetime-local"
              required
              value={formData.dateDebut}
              onChange={(e) => setFormData({ ...formData, dateDebut: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Date de fin</label>
            <input
              type="datetime-local"
              required
              value={formData.dateFin}
              onChange={(e) => setFormData({ ...formData, dateFin: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            />
          </div>
        </div>

        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Salle</label>
          <select
            required
            value={formData.salle}
            onChange={(e) => setFormData({ ...formData, salle: e.target.value })}
            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all bg-white"
          >
            <option value="" disabled>Sélectionner une salle</option>
            {salles.map((salle) => (
              <option key={salle.id} value={salle.nom}>
                {salle.nom}
              </option>
            ))}
          </select>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">ID(s) Étudiant(s)</label>
            <input
              type="text"
              required
              placeholder="ex: 1001, 1002"
              value={formData.etudiantIds}
              onChange={(e) => setFormData({ ...formData, etudiantIds: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            />
            <p className="text-xs text-slate-500">Séparés par une virgule (max 2)</p>
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">ID Encadrant</label>
            <input
              type="number"
              required
              placeholder="ex: 2001"
              value={formData.encadrantId}
              onChange={(e) => setFormData({ ...formData, encadrantId: e.target.value })}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none transition-all"
            />
          </div>
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
            disabled={isLoading}
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
