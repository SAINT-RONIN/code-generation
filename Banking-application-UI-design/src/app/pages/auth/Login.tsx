import { useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { useBankStore } from '../../../store';
import { Mail, Lock, LogIn, Monitor } from 'lucide-react';

export default function Login() {
  const [email, setEmail] = useState('john@example.com');
  const [password, setPassword] = useState('password123');
  const [error, setError] = useState('');
  const { login } = useBankStore();
  const navigate = useNavigate();

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    
    // Simple mock auth
    const user = login(email);
    if (user) {
      if (user.role === 'customer') navigate('/customer');
      else if (user.role === 'employee') navigate('/employee');
      else if (user.role === 'atm') navigate('/atm');
    } else {
      setError('Invalid credentials. Try: john@example.com, staff@bank.nl, or atm@bank.nl');
    }
  };

  return (
    <form onSubmit={handleLogin} className="space-y-6">
      {error && (
        <div className="bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3 rounded-lg">
          {error}
        </div>
      )}
      
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Mail className="h-5 w-5 text-gray-500" />
            </div>
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              placeholder="Enter your email"
              required
            />
          </div>
        </div>
        
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-1.5">Password</label>
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Lock className="h-5 w-5 text-gray-500" />
            </div>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              placeholder="••••••••"
              required
            />
          </div>
        </div>
      </div>
      
      <button
        type="submit"
        className="w-full bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2"
      >
        <LogIn className="w-5 h-5" />
        Sign In
      </button>
      
      <div className="pt-4 text-center border-t border-white/5 space-y-3">
        <p className="text-sm text-gray-400">
          New to Nova Bank?{' '}
          <Link to="/register" className="text-[#7B61FF] hover:text-[#907aff] font-medium transition-colors">
            Open an account
          </Link>
        </p>
        
        <button 
          type="button" 
          onClick={() => {
            setEmail('atm@bank.nl');
            setPassword('1234');
          }}
          className="inline-flex items-center gap-1.5 text-xs text-gray-500 hover:text-gray-300 transition-colors"
        >
          <Monitor className="w-3.5 h-3.5" />
          Access ATM Terminal
        </button>
      </div>
    </form>
  );
}