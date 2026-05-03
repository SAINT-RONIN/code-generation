import { ReactNode } from 'react';
import { Home, CreditCard, ArrowLeftRight, History, User, LogOut } from 'lucide-react';
import { Link, useLocation, useNavigate } from 'react-router';
import { useBankStore } from '../../store';
import { clsx } from 'clsx';

export default function CustomerLayout({ children }: { children: ReactNode }) {
  const { pathname } = useLocation();
  const { logout, currentUser } = useBankStore();
  const navigate = useNavigate();

  const navItems = [
    { icon: Home, label: 'Home', path: '/customer' },
    { icon: CreditCard, label: 'Accounts', path: '/customer/accounts' },
    { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer' },
    { icon: History, label: 'Transactions', path: '/customer/transactions' },
    { icon: User, label: 'Profile', path: '/customer/profile' },
  ];

  return (
    <div className="flex h-screen bg-[#0A0A0F]">
      {/* Desktop Sidebar */}
      <aside className="hidden md:flex w-64 bg-[#14141A] border-r border-white/5 flex-col">
        <div className="p-6 flex items-center gap-3">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center">
            <span className="text-white font-bold text-sm">NL</span>
          </div>
          <span className="font-bold text-lg tracking-tight text-white">Nova Bank</span>
        </div>
        
        <nav className="flex-1 px-4 py-6 space-y-1">
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              className={clsx(
                "flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm",
                pathname === item.path || (pathname.startsWith(item.path) && item.path !== '/customer')
                  ? "bg-[#7B61FF]/10 text-[#7B61FF]" 
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
      <main className="flex-1 overflow-y-auto pb-20 md:pb-0 relative">
        <div className="absolute top-0 left-0 w-full h-64 bg-gradient-to-b from-[#7B61FF]/5 to-transparent pointer-events-none" />
        <div className="p-4 md:p-8 max-w-5xl mx-auto relative z-10">
          {children}
        </div>
      </main>

      {/* Mobile Bottom Nav */}
      <div className="md:hidden fixed bottom-0 left-0 w-full bg-[#14141A]/90 backdrop-blur-xl border-t border-white/5 flex justify-around p-2 pb-safe z-50">
        {navItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            className={clsx(
              "flex flex-col items-center gap-1 p-2 rounded-lg min-w-[64px]",
              pathname === item.path || (pathname.startsWith(item.path) && item.path !== '/customer')
                ? "text-[#7B61FF]" 
                : "text-gray-500"
            )}
          >
            <item.icon className="w-6 h-6" />
            <span className="text-[10px] font-medium">{item.label}</span>
          </Link>
        ))}
      </div>
    </div>
  );
}