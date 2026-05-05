'use client';

import { useState, useEffect } from 'react';
import { juryService } from '@/lib/jury-service';
import { MembreJury, CreerMembreJuryRequest } from '@/types';
import MembreJuryList from '@/components/jury/MembreJuryList';
import MembreJuryForm from '@/components/jury/MembreJuryForm';
import { Users, Plus, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export default function JuryPage() {
  const [membres, setMembres] = useState<MembreJury[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingMembre, setEditingMembre] = useState<MembreJury | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchMembres = async () => {
    try {
      setIsLoading(true);
      const data = await juryService.getAllMembres();
      setMembres(data);
    } catch (error) {
      console.error('Failed to fetch jury members', error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMembres();
  }, []);

  const handleCreateOrUpdate = async (data: CreerMembreJuryRequest) => {
    try {
      setIsSubmitting(true);
      if (editingMembre) {
        await juryService.updateMembre(editingMembre.id, data);
      } else {
        await juryService.createMembre(data);
      }
      setIsFormOpen(false);
      setEditingMembre(undefined);
      fetchMembres();
    } catch (error) {
      console.error('Failed to save member', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce membre ?')) {
      try {
        await juryService.deleteMembre(id);
        fetchMembres();
      } catch (error) {
        console.error('Failed to delete member', error);
      }
    }
  };

  const handleEdit = (membre: MembreJury) => {
    setEditingMembre(membre);
    setIsFormOpen(true);
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
            <Users className="w-8 h-8 text-blue-600" />
            Gestion des Membres de Jury
          </h1>
          <p className="text-slate-500 mt-1">Gérer les enseignants habilités à siéger dans les jurys de soutenance.</p>
        </div>

        {!isFormOpen && (
          <button
            onClick={() => setIsFormOpen(true)}
            className="flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-xl hover:bg-blue-700 transition-all shadow-sm hover:shadow-md"
          >
            <Plus className="w-5 h-5" />
            Nouveau Membre
          </button>
        )}
      </div>

      {isFormOpen && (
        <div className="animate-in fade-in slide-in-from-top-4 duration-300">
          <MembreJuryForm
            initialData={editingMembre}
            onSubmit={handleCreateOrUpdate}
            onCancel={() => {
              setIsFormOpen(false);
              setEditingMembre(undefined);
            }}
            isLoading={isSubmitting}
          />
        </div>
      )}

      <div className="space-y-4">
        <div className="flex items-center justify-between border-b border-slate-200 pb-2">
          <h2 className="text-sm font-bold uppercase tracking-wider text-slate-500">
            Liste des Enseignants ({membres.length})
          </h2>
        </div>
        <MembreJuryList
          membres={membres}
          onEdit={handleEdit}
          onDelete={handleDelete}
          isLoading={isLoading}
        />
      </div>
    </div>
  );
}
