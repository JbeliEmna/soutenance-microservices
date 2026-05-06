'use client';

import { useState, useEffect } from 'react';
import { juryService } from '@/lib/jury-service';
import { MembreJury, CreerMembreJuryRequest } from '@/types';
import MembreJuryList from '@/components/jury/MembreJuryList';
import MembreJuryForm from '@/components/jury/MembreJuryForm';
import AffectationJuryModule from '@/components/jury/AffectationJuryModule';
import { Users, Plus, UserPlus, UserCheck, ShieldCheck } from 'lucide-react';
import { useRouter } from 'next/navigation'; // Import useRouter
import { authService } from '@/lib/auth-service'; // Import authService

type Tab = 'membres' | 'affectations';

export default function JuryPage() {
  const router = useRouter(); // Initialize router
  const [activeTab, setActiveTab] = useState<Tab>('membres');
  const [membres, setMembres] = useState<MembreJury[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingMembre, setEditingMembre] = useState<MembreJury | undefined>();
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Check user role on mount and redirect if necessary
  useEffect(() => {
    const user = authService.getCurrentUser();
    if (user?.role === 'ROLE_ETUDIANT') {
      router.push('/'); // Redirect students to dashboard
    }
  }, [router]);

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
    // Fetch data only if the user is not a student (handled by redirect)
    const user = authService.getCurrentUser();
    if (user?.role !== 'ROLE_ETUDIANT') {
      if (activeTab === 'membres') {
        fetchMembres();
      }
    }
  }, [activeTab]);

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

  // This component renders only if the user is NOT a student (due to the redirect)
  return (
    <div className="max-w-6xl mx-auto space-y-8">
      {/* Header Section */}
      <div className="flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-2xl font-extrabold tracking-tight text-slate-900 flex items-center gap-3">
            <ShieldCheck className="w-10 h-10 text-blue-600" />
            Module Jury
          </h1>
          <p className="text-slate-500 mt-2 text-lg max-w-2xl">
            Gérez le vivier d'enseignants et organisez les commissions de soutenance en affectant les rôles stratégiques.
          </p>
        </div>

        {activeTab === 'membres' && !isFormOpen && (
          <button
            onClick={() => setIsFormOpen(true)}
            className="flex items-center justify-center gap-2 px-6 py-3 bg-blue-600 text-white font-bold rounded-xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-600/20 hover:scale-[1.02] active:scale-[0.98]"
          >
            <UserPlus className="w-5 h-5" />
            Ajouter un Enseignant
          </button>
        )}
      </div>

      {/* Navigation Tabs */}
      <div className="flex border-b border-slate-200">
        <button
          onClick={() => setActiveTab('membres')}
          className={`px-8 py-4 text-sm font-bold transition-all border-b-2 flex items-center gap-2 ${
            activeTab === 'membres' 
              ? 'border-blue-600 text-blue-600 bg-blue-50/50' 
              : 'border-transparent text-slate-500 hover:text-slate-700 hover:bg-slate-50'
          }`}
        >
          <Users className="w-4 h-4" />
          MEMBRES DE JURY
        </button>
        <button
          onClick={() => setActiveTab('affectations')}
          className={`px-8 py-4 text-sm font-bold transition-all border-b-2 flex items-center gap-2 ${
            activeTab === 'affectations' 
              ? 'border-blue-600 text-blue-600 bg-blue-50/50' 
              : 'border-transparent text-slate-500 hover:text-slate-700 hover:bg-slate-50'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          AFFECTATIONS
        </button>
      </div>

      {/* Content Area */}
      <div className="animate-in fade-in duration-500">
        {activeTab === 'membres' ? (
          <div className="space-y-8">
            {isFormOpen && (
              <div className="animate-in slide-in-from-top-4 duration-300">
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
              <div className="flex items-center justify-between border-b border-slate-100 pb-2">
                <h2 className="text-xs font-black uppercase tracking-[0.2em] text-slate-400">
                  Enseignants Référencés ({membres.length})
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
        ) : (
          <AffectationJuryModule />
        )}
      </div>
    </div>
  );
}
