'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { 
  LayoutDashboard, 
  Calendar, 
  Users, 
  ClipboardCheck, 
  Settings, 
  LogOut,
  GraduationCap
} from 'lucide-react';
import { authService } from '@/lib/auth-service';
import { cn } from '@/lib/utils';
import { AuthResponse } from '@/types';
import { useState, useEffect } from 'react'; // Import useState and useEffect

const navigation = [
  { name: 'Tableau de bord', href: '/', icon: LayoutDashboard },
  { name: 'Planification', href: '/planning', icon: Calendar },
  { name: 'Gestion des Jurys', href: '/jury', icon: Users },
  { name: 'Saisie des Notes', href: '/jury/notes', icon: ClipboardCheck },
];

export default function Sidebar() {
  const pathname = usePathname();
  const router = useRouter();
  
  // State to hold user info, initialized to null.
  const [user, setUser] = useState<AuthResponse | null>(null); 
  // State to track if we are on the client side.
  const [isClientSide, setIsClientSide] = useState(false); 
  
  useEffect(() => {
    // This effect runs only on the client-side after the component mounts.
    setIsClientSide(true); // Mark as client-side
    const currentUser = authService.getCurrentUser();
    setUser(currentUser);
  }, []); // Empty dependency array means this effect runs once on mount.

  // Determine navigation links.
  // This logic ensures that on the server, and initially on the client (before user data is loaded),
  // all navigation links are rendered. Filtering only occurs on the client once the user role is known.
  const navLinks = navigation.filter(item => {
    // If not on the client side, OR if user data is not yet available,
    // OR if the user role is NOT ROLE_ETUDIANT, show all links.
    // This ensures the server renders the superset of links.
    if (!isClientSide || !user || user.role !== 'ROLE_ETUDIANT') {
      return true;
    }
    // If it IS client side AND user IS ROLE_ETUDIANT, then filter to show only the dashboard link.
    return item.href === '/';
  });

  return (
    <div className="hidden md:flex flex-col w-64 bg-white border-r border-slate-200">
      <div className="p-6 flex items-center gap-3">
        <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center">
          <GraduationCap className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="text-sm font-black text-slate-900 leading-tight">ESMT</h1>
          <p className="text-[10px] font-bold text-blue-600 uppercase tracking-wider">Soutenances</p>
        </div>
      </div>

      <nav className="flex-1 px-4 space-y-1 mt-4">
        {navLinks.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.name}
              href={item.href}
              className={cn(
                "group flex items-center px-3 py-2.5 text-sm font-bold rounded-xl transition-all duration-200",
                isActive 
                  ? "bg-blue-50 text-blue-600" 
                  : "text-slate-600 hover:bg-slate-50 hover:text-slate-900"
              )}
            >
              <item.icon className={cn(
                "mr-3 h-5 w-5 transition-colors",
                isActive ? "text-blue-600" : "text-slate-400 group-hover:text-slate-600"
              )} />
              {item.name}
              {isActive && (
                <div className="ml-auto w-1.5 h-1.5 rounded-full bg-blue-600" />
              )}
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-100">
        <button
          onClick={() => {
            authService.logout();
            router.push('/login'); // Redirect to login after logout
          }}
          className="flex w-full items-center px-3 py-2.5 text-sm font-bold text-red-600 hover:bg-red-50 rounded-xl transition-all duration-200"
        >
          <LogOut className="mr-3 h-5 w-5" />
          Déconnexion
        </button>
      </div>
    </div>
  );
}
