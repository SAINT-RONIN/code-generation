import { ReactNode } from 'react';
import { Users, FileText, CheckCircle, ArrowLeftRight, Activity, LogOut, Shield } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router';
import { useBankStore } from '../../store';
import { clsx } from 'clsx';

export default function EmployeeLayout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();
  const { logout, currentUser } = useBankStore();
  const navigate = useNavigate();

  const navItems = [
    { icon: Activity, label: 'Overview', path: '/employee' },
    { icon: Users, label: 'Customers', path: '/employee/customers' },
    { icon: CheckCircle, label: 'Approvals', path: '/employee/approvals' },
    { icon: ArrowLeftRight, label: 'Transfers', path: '/employee/transfer' },
    { icon: FileText, label: 'Transactions', path: '/employee/transactions' },
  ];

  return (
    <div className="flex h-screen bg-[#0A0A0F]">
      {/* Desktop Sidebar */}
      <aside className="w-64 bg-[#14141A] border-r border-white/5 flex flex-col hidden md:flex">
        <div className="p-6 flex items-center gap-3 border-b border-white/5">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#FF5E5B] to-[#D93836] flex items-center justify-center">
            <Shield className="w-4 h-4 text-white" />
          </div>
          <div>
            <span className="font-bold text-sm tracking-tight text-white block">Nova Bank Staff</span>
            <span className="text-[10px] text-gray-500 font-mono tracking-widest uppercase text-white/50">Internal</span>
          </div>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-1">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={clsx(
                "flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm",
                pathname === item.path || (pathname.startsWith(item.path) && item.path !== '/employee')
                  ? "bg-[#FF5E5B]/10 text-[#FF5E5B]" 
                  : "text-gray-400 hover:text-white hover:bg-white/5"
              )}
            >
              <item.icon className="w-5 h-5" />
              {item.label}
            </Link>
          ))}
        </nav>
        
        <div className="p-4 border-t border-white/5">
          <div className="flex items-center gap-3 px-4 py-3 mb-2">
            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-gray-700 to-gray-600 flex items-center justify-center">
              {currentUser?.name.charAt(0)}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium text-white truncate">{currentUser?.name}</p>
              <p className="text-xs text-gray-500">Staff</p>
            </div>
          </div>
          <button 
            onClick={() => { logout(); navigate('/login'); }}
            className="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-400 hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/10 rounded-xl transition-all"
          >
            <LogOut className="w-5 h-5" />
            Sign out
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 overflow-y-auto">
        {/* Mobile Header */}
        <div className="md:hidden flex items-center justify-between p-4 bg-[#14141A] border-b border-white/5 sticky top-0 z-20">
           <div className="flex items-center gap-2">
             <Shield className="w-5 h-5 text-[#FF5E5B]" />
             <span className="font-bold text-sm">Staff Portal</span>
           </div>
           {/* Add a simple hamburger or just logout for mobile for now */}
           <button onClick={() => { logout(); navigate('/login'); }}>
             <LogOut className="w-5 h-5 text-gray-400" />
           </button>
        </div>
        
        <div className="p-4 md:p-8 max-w-7xl mx-auto">
          {children}
        </div>
      </main>
      
      {/* Mobile nav fallback for employee (simplified) */}
      <div className="md:hidden fixed bottom-0 left-0 w-full bg-[#14141A] border-t border-white/5 flex justify-around p-2 pb-safe z-50">
        {navItems.slice(0, 4).map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={clsx(
              "flex flex-col items-center gap-1 p-2 rounded-lg min-w-[64px]",
              pathname === item.path || (pathname.startsWith(item.path) && item.path !== '/employee')
                ? "text-[#FF5E5B]" 
                : "text-gray-500"
            )}
          >
            <item.icon className="w-5 h-5" />
            <span className="text-[10px]">{item.label}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}