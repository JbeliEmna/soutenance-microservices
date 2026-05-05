'use client';

import { useState, useEffect } from 'react';
import { Soutenance, Salle, User } from '@/types';
import { X, Save, Calendar, Search, Check, User as UserIcon } from 'lucide-react';
import { authService } from '@/lib/auth-service';

interface SoutenanceFormProps {
  initialData?: Soutenance;
  salles: Salle[];
  onSubmit: (data: any) => Promise<void>;
  onCancel: () => void;
  isLoading: boolean;
}

export default function SoutenanceForm({ initialData, salles, onSubmit, onCancel, isLoading }: SoutenanceFormProps) {
  const [students, setStudents] = useState<User[]>([]);
  const [teachers, setTeachers] = useState<User[]>([]);
  const [isDataLoading, setIsDataLoading] = useState(true);

  const [selectedStudentIds, setSelectedStudentIds] = useState<number[]>(initialData?.etudiantIds || []);
  const [selectedEncadrantId, setSelectedEncadrantId] = useState<number | ''>(initialData?.encadrantId || '');
  const [salle, setSalle] = useState(initialData?.salle || '');
  const [dateDebut, setDateDebut] = useState(initialData?.dateDebut ? initialData.dateDebut.slice(0, 16) : '');
  const [dateFin, setDateFin] = useState(initialData?.dateFin ? initialData.dateFin.slice(0, 16) : '');

  const [studentSearch, setStudentSearch] = useState('');
  const [teacherSearch, setTeachersSearch] = useState('');

  useEffect(() => {
    const fetchData = async () => {
      try {
        setIsDataLoading(true);
        const [studentsData, teachersData] = await Promise.all([
          authService.getAllStudents(),
          authService.getAllTeachers()
        ]);
        setStudents(studentsData);
        setTeachers(teachersData);
      } catch (error) {
        console.error('Failed to fetch users', error);
      } finally {
        setIsDataLoading(false);
      }
    };
    fetchData();
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (selectedStudentIds.length === 0) {
      alert('Veuillez sélectionner au moins un étudiant.');
      return;
    }
    if (selectedEncadrantId === '') {
      alert('Veuillez sélectionner un encadrant.');
      return;
    }

    await onSubmit({
      etudiantIds: selectedStudentIds,
      encadrantId: selectedEncadrantId,
      salle,
      dateDebut,
      dateFin
    });
  };

  const filteredStudents = students.filter(s => 
    `${s.nom} ${s.prenom}`.toLowerCase().includes(studentSearch.toLowerCase()) ||
    s.email.toLowerCase().includes(studentSearch.toLowerCase()) ||
    s.externalId.toString().includes(studentSearch)
  );

  const filteredTeachers = teachers.filter(t => 
    `${t.nom} ${t.prenom}`.toLowerCase().includes(teacherSearch.toLowerCase()) ||
    t.email.toLowerCase().includes(teacherSearch.toLowerCase()) ||
    t.externalId.toString().includes(teacherSearch)
  );

  const toggleStudent = (id: number) => {
    if (selectedStudentIds.includes(id)) {
      setSelectedStudentIds(selectedStudentIds.filter(sid => sid !== id));
    } else {
      if (selectedStudentIds.length < 2) {
        setSelectedStudentIds([...selectedStudentIds, id]);
      } else {
        alert('Maximum 2 étudiants autorisés.');
      }
    }
  };

  return (
    <div className="bg-white p-6 rounded-xl border border-slate-200 shadow-sm max-h-[90vh] overflow-y-auto">
      <div className="flex justify-between items-center mb-6">
        <h3 className="text-lg font-bold text-slate-900 flex items-center gap-2">
          <Calendar className="w-5 h-5 text-blue-500" />
          {initialData ? 'Modifier la soutenance' : 'Planifier une soutenance'}
        </h3>
        <button type="button" onClick={onCancel} className="text-slate-400 hover:text-slate-600 transition-colors">
          <X className="w-5 h-5" />
        </button>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Date and Time Section */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Date de début</label>
            <input
              type="datetime-local"
              required
              value={dateDebut}
              onChange={(e) => setDateDebut(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
          <div className="space-y-1">
            <label className="text-sm font-medium text-slate-700">Date de fin</label>
            <input
              type="datetime-local"
              required
              value={dateFin}
              onChange={(e) => setDateFin(e.target.value)}
              className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
            />
          </div>
        </div>

        {/* Room Section */}
        <div className="space-y-1">
          <label className="text-sm font-medium text-slate-700">Salle</label>
          <select
            required
            value={salle}
            onChange={(e) => setSalle(e.target.value)}
            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none bg-white"
          >
            <option value="" disabled>Sélectionner une salle</option>
            {salles.map((s) => (
              <option key={s.id} value={s.nom}>{s.nom}</option>
            ))}
          </select>
        </div>

        {/* Students Selection Section */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700 flex justify-between">
            Étudiants (max 2)
            <span className="text-xs font-normal text-slate-500">{selectedStudentIds.length}/2 sélectionnés</span>
          </label>
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Rechercher par nom, email ou ID..."
              value={studentSearch}
              onChange={(e) => setStudentSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
            />
          </div>
          <div className="border rounded-lg max-h-40 overflow-y-auto divide-y">
            {isDataLoading ? (
              <div className="p-4 text-center text-sm text-slate-500">Chargement...</div>
            ) : filteredStudents.length > 0 ? (
              filteredStudents.map((s) => (
                <div
                  key={s.externalId}
                  onClick={() => toggleStudent(s.externalId)}
                  className={`p-2 px-3 flex items-center justify-between cursor-pointer hover:bg-slate-50 transition-colors ${selectedStudentIds.includes(s.externalId) ? 'bg-blue-50' : ''}`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-600">
                      {s.nom[0]}{s.prenom[0]}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-900">{s.prenom} {s.nom}</p>
                      <p className="text-xs text-slate-500">{s.email} | ID: {s.externalId}</p>
                    </div>
                  </div>
                  {selectedStudentIds.includes(s.externalId) && (
                    <Check className="w-4 h-4 text-blue-600" />
                  )}
                </div>
              ))
            ) : (
              <div className="p-4 text-center text-sm text-slate-500">Aucun étudiant trouvé</div>
            )}
          </div>
        </div>

        {/* Supervisor Selection Section */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-slate-700">Encadrant</label>
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <input
              type="text"
              placeholder="Rechercher par nom, email ou ID..."
              value={teacherSearch}
              onChange={(e) => setTeachersSearch(e.target.value)}
              className="w-full pl-9 pr-3 py-2 border rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-sm"
            />
          </div>
          <div className="border rounded-lg max-h-40 overflow-y-auto divide-y">
            {isDataLoading ? (
              <div className="p-4 text-center text-sm text-slate-500">Chargement...</div>
            ) : filteredTeachers.length > 0 ? (
              filteredTeachers.map((t) => (
                <div
                  key={t.externalId}
                  onClick={() => setSelectedEncadrantId(t.externalId)}
                  className={`p-2 px-3 flex items-center justify-between cursor-pointer hover:bg-slate-50 transition-colors ${selectedEncadrantId === t.externalId ? 'bg-purple-50' : ''}`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center text-xs font-bold text-purple-600">
                      {t.nom[0]}{t.prenom[0]}
                    </div>
                    <div>
                      <p className="text-sm font-bold text-slate-900">{t.prenom} {t.nom}</p>
                      <p className="text-xs text-slate-500">{t.email} | ID: {t.externalId}</p>
                    </div>
                  </div>
                  {selectedEncadrantId === t.externalId && (
                    <Check className="w-4 h-4 text-purple-600" />
                  )}
                </div>
              ))
            ) : (
              <div className="p-4 text-center text-sm text-slate-500">Aucun enseignant trouvé</div>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="pt-4 flex justify-end gap-3 border-t">
          <button
            type="button"
            onClick={onCancel}
            className="px-4 py-2 text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
          >
            Annuler
          </button>
          <button
            type="submit"
            disabled={isLoading || isDataLoading}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2 disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            {isLoading ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </div>
      </form>
    </div>
  );
}
