import { useBankStore } from '../../../store';
import { useNavigate, Link } from 'react-router';
import { LogOut, ArrowUpCircle, ArrowDownCircle, Wallet } from 'lucide-react';
import { clsx } from 'clsx';

export default function AtmMenu() {
  const { logout } = useBankStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const actions = [
    { title: 'Withdraw', icon: ArrowUpCircle, color: 'bg-blue-600', path: '/atm/withdraw' },
    { title: 'Deposit', icon: ArrowDownCircle, color: 'bg-green-600', path: '/atm/deposit' },
    { title: 'Balance', icon: Wallet, color: 'bg-purple-600', path: '/atm/balance' },
  ];

  return (
    <div className="min-h-screen bg-black text-white p-8 font-sans flex flex-col items-center justify-center relative">
      <div className="absolute top-8 left-8 flex items-center gap-4">
        <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center border-4 border-white/20">
          <span className="text-white font-bold text-2xl tracking-tighter">NL</span>
        </div>
        <div className="text-left">
          <h1 className="text-3xl font-bold tracking-tight uppercase">Nova ATM</h1>
          <p className="text-gray-400 text-lg">Terminal #402</p>
        </div>
      </div>
      
      <button 
        onClick={handleLogout}
        className="absolute top-8 right-8 flex items-center gap-2 bg-red-600/20 text-red-500 hover:bg-red-600 hover:text-white px-6 py-4 rounded-xl text-xl font-bold uppercase tracking-wider transition-colors border-2 border-red-600/50"
      >
        <LogOut className="w-6 h-6" />
        Finish
      </button>

      <div className="w-full max-w-4xl mt-24">
        <h2 className="text-5xl font-bold mb-12 text-center uppercase tracking-wider">Please select a service</h2>
        
        <div className="grid grid-cols-2 gap-8">
          {actions.map((action, i) => (
            <Link 
              key={i}
              to={action.path}
              className={clsx(
                "flex flex-col items-center justify-center p-12 rounded-3xl border-4 border-white/10 hover:border-white transition-all transform hover:scale-105 active:scale-95 shadow-2xl",
                action.color
              )}
            >
              <action.icon className="w-24 h-24 mb-6 opacity-80" />
              <span className="text-4xl font-bold uppercase tracking-widest">{action.title}</span>
            </Link>
          ))}
          
          <button 
            onClick={handleLogout}
            className="flex flex-col items-center justify-center p-12 rounded-3xl border-4 border-white/10 hover:border-red-500 bg-gray-900 transition-all transform hover:scale-105 active:scale-95 shadow-2xl group"
          >
            <LogOut className="w-24 h-24 mb-6 opacity-50 group-hover:text-red-500 group-hover:opacity-100" />
            <span className="text-4xl font-bold uppercase tracking-widest text-gray-500 group-hover:text-red-500">Return Card</span>
          </button>
        </div>
      </div>
    </div>
  );
}