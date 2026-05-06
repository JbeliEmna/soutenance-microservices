'use client';

import { useState, useEffect } from 'react';
import { authService } from '@/lib/auth-service';
import { AuthResponse } from '@/types';
import EvaluationsModule from '@/components/jury/EvaluationsModule';
import StudentResultModule from '@/components/jury/StudentResultModule';
import { ClipboardCheck, GraduationCap } from 'lucide-react';

export default function NotesPage() {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
    setUser(authService.getCurrentUser());
  }, []);

  if (!isMounted) return null;

  const isStudent = user?.role === 'ROLE_ETUDIANT';

  return (
    <div className="max-w-6xl mx-auto space-y-8 animate-in fade-in duration-500">
      <div className="flex flex-col gap-2">
        <h1 className="text-xl font-black tracking-tight text-slate-900 flex items-center gap-3">
          {isStudent ? (
            <>
              <GraduationCap className="w-10 h-10 text-blue-600" />
              Mon Résultat
            </>
          ) : (
            <>
              <ClipboardCheck className="w-10 h-10 text-blue-600" />
              Évaluations & Notes
            </>
          )}
        </h1>
        <p className="text-slate-500 font-medium text-lg">
          {isStudent 
            ? "Consultez votre note finale et votre mention après délibération du jury."
            : "Saisissez les notes attribuées par chaque membre du jury et consultez les moyennes calculées."}
        </p>
      </div>

      <div className="pt-4">
        {isStudent ? (
          <StudentResultModule />
        ) : (
          <EvaluationsModule />
        )}
      </div>
    </div>
  );
}
