'use client';

import { useState } from 'react';
import { Save, X } from 'lucide-react';

interface GradeFormProps {
  soutenanceId: number;
  enseignantId: number;
  roleJury: string;
  initialGrade?: number;
  onSave: (note: number) => Promise<void>;
  onCancel: () => void;
}

export default function GradeForm({ initialGrade, onSave, onCancel }: GradeFormProps) {
  const [note, setNote] = useState<number | ''>(initialGrade ?? '');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (note === '' || note < 0 || note > 20) {
      alert('Veuillez saisir une note entre 0 et 20.');
      return;
    }

    try {
      setIsSubmitting(true);
      await onSave(Number(note));
    } catch (error) {
      console.error('Failed to save grade', error);
      alert('Erreur lors de l\'enregistrement de la note.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex items-center gap-2">
      <input
        type="number"
        step="0.25"
        min="0"
        max="20"
        required
        value={note}
        onChange={(e) => setNote(e.target.value === '' ? '' : Number(e.target.value))}
        className="w-20 px-2 py-1 border rounded focus:ring-2 focus:ring-blue-500 outline-none text-sm"
        placeholder="Note"
        autoFocus
      />
      <button
        type="submit"
        disabled={isSubmitting}
        className="p-1.5 bg-green-600 text-white rounded hover:bg-green-700 transition-colors disabled:opacity-50"
        title="Enregistrer"
      >
        <Save className="w-4 h-4" />
      </button>
      <button
        type="button"
        onClick={onCancel}
        className="p-1.5 bg-slate-100 text-slate-600 rounded hover:bg-slate-200 transition-colors"
        title="Annuler"
      >
        <X className="w-4 h-4" />
      </button>
    </form>
  );
}
