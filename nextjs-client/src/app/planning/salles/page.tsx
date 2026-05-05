'use client';

import { useState, useEffect } from 'react';
import { soutenanceService } from '@/lib/soutenance-service';
import { Salle } from '@/types';
import SalleList from '@/components/planning/SalleList';
import SalleForm from '@/components/planning/SalleForm';
import { Home, Plus, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function SallesPage() {
  const [salles, setSalles] = useState<Salle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingSalle, setEditingSalle] = useState<Salle | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSalles = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const data = await soutenanceService.getAllSalles();
      setSalles(data);
    } catch (err) {
      console.error('Failed to fetch salles', err);
      setError('Erreur lors du chargement des salles. Veuillez vérifier la connexion au backend.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchSalles();
  }, []);

  const handleCreateOrUpdate = async (data: { nom: string }) => {
    try {
      setIsSubmitting(true);
      setError(null);
      if (editingSalle) {
        await soutenanceService.updateSalle(editingSalle.id, data);
      } else {
        await soutenanceService.createSalle(data);
      }
      setIsFormOpen(false);
      setEditingSalle(undefined);
      fetchSalles();
    } catch (err: any) {
      console.error('Failed to save salle', err);
      const message = err.response?.data?.message || 'Une erreur est survenue lors de l\'enregistrement.';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette salle ?')) {
      try {
        setError(null);
        await soutenanceService.deleteSalle(id);
        fetchSalles();
      } catch (err: any) {
        console.error('Failed to delete salle', err);
        const message = err.response?.data?.message || 'Impossible de supprimer la salle. Elle est peut-être utilisée par une soutenance.';
        setError(message);
      }
    }
  };

  const handleEdit = (salle: Salle) => {
    setEditingSalle(salle);
    setIsFormOpen(true);
  };

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <Link 
            href="/" 
            className="text-sm text-slate-500 hover:text-blue-600 flex items-center gap-1 mb-2 transition-colors"
          >
            <ArrowLeft className="w-4 h-4" />
            Retour au tableau de bord
          </Link>
          <h1 className="text-3xl font-bold tracking-tight text-slate-900 flex items-center gap-3">
            <Home className="w-8 h-8 text-blue-600" />
            Gestion des Salles
          </h1>
          <p className="text-slate-500 mt-1">Gérer les locaux disponibles pour les soutenances.</p>
        </div>

        {!isFormOpen && (
          <button
            onClick={() => {
              setEditingSalle(undefined);
              setIsFormOpen(true);
            }}
            className="flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-sm hover:shadow-md"
          >
            <Plus className="w-5 h-5" />
            Nouvelle Salle
          </button>
        )}
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-100 text-red-600 rounded-xl text-sm font-medium animate-in fade-in slide-in-from-top-2">
          {error}
        </div>
      )}

      {isFormOpen && (
        <div className="animate-in fade-in slide-in-from-top-4 duration-300">
          <SalleForm
            initialData={editingSalle}
            onSubmit={handleCreateOrUpdate}
            onCancel={() => {
              setIsFormOpen(false);
              setEditingSalle(undefined);
              setError(null);
            }}
            isLoading={isSubmitting}
          />
        </div>
      )}

      <div className="space-y-4">
        <div className="flex items-center justify-between border-b border-slate-200 pb-2">
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-500">
            Salles enregistrées ({salles.length})
          </h2>
        </div>
        <SalleList
          salles={salles}
          onEdit={handleEdit}
          onDelete={handleDelete}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
}
