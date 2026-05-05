'use client';

import { useState, useEffect } from 'react';
import { soutenanceService } from '@/lib/soutenance-service';
import { Soutenance, Salle } from '@/types';
import SoutenanceList from '@/components/planning/SoutenanceList';
import SoutenanceForm from '@/components/planning/SoutenanceForm';
import { Calendar, Plus, ArrowLeft, Home as HomeIcon } from 'lucide-react';
import Link from 'next/link';

export default function PlanningPage() {
  const [soutenances, setSoutenances] = useState<Soutenance[]>([]);
  const [salles, setSalles] = useState<Salle[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingSoutenance, setEditingSoutenance] = useState<Soutenance | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchData = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const [soutenancesData, sallesData] = await Promise.all([
        soutenanceService.getAllSoutenances(),
        soutenanceService.getAllSalles()
      ]);
      
      // Sort by date (newest first)
      const sortedSoutenances = soutenancesData.sort((a, b) => 
        new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime()
      );
      
      setSoutenances(sortedSoutenances);
      setSalles(sallesData);
    } catch (err) {
      console.error('Failed to fetch data', err);
      setError('Erreur lors du chargement des données. Veuillez vérifier la connexion au backend.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleCreateOrUpdate = async (data: any) => {
    try {
      setIsSubmitting(true);
      setError(null);
      
      if (editingSoutenance) {
        await soutenanceService.updateSoutenance(editingSoutenance.id, data);
      } else {
        await soutenanceService.createSoutenance(data);
      }
      setIsFormOpen(false);
      setEditingSoutenance(undefined);
      fetchData();
    } catch (err: any) {
      console.error('Failed to save soutenance', err);
      const message = err.response?.data?.message || 'Une erreur est survenue lors de l\'enregistrement (conflit d\'horaire possible).';
      setError(message);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette soutenance ?')) {
      try {
        setError(null);
        await soutenanceService.deleteSoutenance(id);
        fetchData();
      } catch (err: any) {
        console.error('Failed to delete soutenance', err);
        const message = err.response?.data?.message || 'Impossible de supprimer la soutenance.';
        setError(message);
      }
    }
  };

  const handleEdit = (soutenance: Soutenance) => {
    setEditingSoutenance(soutenance);
    setIsFormOpen(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <div className="max-w-5xl mx-auto space-y-8">
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
            <Calendar className="w-8 h-8 text-blue-600" />
            Planification des Soutenances
          </h1>
          <p className="text-slate-500 mt-1">Gérer les dates, les salles et éviter les conflits horaires.</p>
        </div>

        <div className="flex gap-2">
          <Link
            href="/planning/salles"
            className="flex items-center justify-center gap-2 px-4 py-2.5 bg-slate-100 text-slate-700 rounded-xl hover:bg-slate-200 transition-all shadow-sm"
          >
            <HomeIcon className="w-4 h-4" />
            Salles
          </Link>
          {!isFormOpen && (
            <button
              onClick={() => {
                setEditingSoutenance(undefined);
                setIsFormOpen(true);
              }}
              className="flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-sm hover:shadow-md"
            >
              <Plus className="w-5 h-5" />
              Planifier
            </button>
          )}
        </div>
      </div>

      {error && (
        <div className="p-4 bg-red-50 border border-red-100 text-red-600 rounded-xl text-sm font-medium animate-in fade-in slide-in-from-top-2">
          {error}
        </div>
      )}

      {isFormOpen && (
        <div className="animate-in fade-in slide-in-from-top-4 duration-300">
          <SoutenanceForm
            initialData={editingSoutenance}
            salles={salles}
            onSubmit={handleCreateOrUpdate}
            onCancel={() => {
              setIsFormOpen(false);
              setEditingSoutenance(undefined);
              setError(null);
            }}
            isLoading={isSubmitting}
          />
        </div>
      )}

      <div className="space-y-4">
        <div className="flex items-center justify-between border-b border-slate-200 pb-2">
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-500">
            Soutenances ({soutenances.length})
          </h2>
        </div>
        <SoutenanceList
          soutenances={soutenances}
          onEdit={handleEdit}
          onDelete={handleDelete}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
}
