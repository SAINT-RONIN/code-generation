import { useState } from 'react';
import { useBankStore } from '../../../store';
import { useNavigate } from 'react-router';
import { ArrowLeft, ChevronRight, Eye, EyeOff } from 'lucide-react';

export default function AtmBalance() {
  const { accounts } = useBankStore();
  const navigate = useNavigate();
  
  const [step, setStep] = useState(1);
  const [iban, setIban] = useState('');
  const [error, setError] = useState('');
  const [showBalance, setShowBalance] = useState(false);
  const [balance, setBalance] = useState<number | null>(null);
  
  const handleNext = () => {
    setError('');
    const account = accounts.find(a => a.iban.replace(/\s/g, '') === iban.replace(/\s/g, '').toUpperCase());
    if (!account) return setError('Invalid Card / IBAN');
    
    setBalance(account.balance);
    setStep(2);
  };

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  return (
    <div className="min-h-screen bg-black text-white p-8 font-sans flex flex-col items-center justify-center relative">
      <button 
        onClick={() => navigate('/atm')}
        className="absolute top-8 left-8 flex items-center gap-2 bg-white/10 hover:bg-white/20 px-6 py-4 rounded-xl text-xl font-bold uppercase tracking-wider transition-colors border-2 border-white/20"
      >
        <ArrowLeft className="w-6 h-6" />
        Done
      </button>

      <div className="w-full max-w-4xl mt-12">
        {step === 1 && (
          <div className="text-center animate-in fade-in zoom-in duration-300">
            <h2 className="text-5xl font-bold mb-12 uppercase tracking-wider">Insert Card / Enter IBAN</h2>
            
            {error && <p className="text-red-500 text-2xl font-bold mb-8 uppercase bg-red-500/10 py-4 rounded-xl">{error}</p>}
            
            <div className="max-w-xl mx-auto flex gap-4">
              <input 
                type="text" 
                value={iban}
                onChange={e => setIban(e.target.value)}
                placeholder="NL00INHO0000000000"
                className="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-3xl font-mono text-center uppercase focus:outline-none focus:border-purple-500 transition-colors"
              />
              <button 
                onClick={handleNext}
                className="bg-purple-600 hover:bg-purple-500 px-8 rounded-2xl border-4 border-purple-500 flex items-center justify-center transition-colors"
              >
                <ChevronRight className="w-12 h-12" />
              </button>
            </div>
          </div>
        )}

        {step === 2 && balance !== null && (
          <div className="text-center animate-in slide-in-from-right-12 duration-300">
            <h2 className="text-5xl font-bold mb-16 uppercase tracking-wider text-gray-400">Current Balance</h2>
            
            <div className="bg-gray-900 border-4 border-white/10 rounded-3xl p-16 max-w-2xl mx-auto relative overflow-hidden">
              <div className="absolute top-0 right-0 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl -mr-32 -mt-32 pointer-events-none" />
              
              <p className="text-2xl text-gray-500 mb-6 uppercase tracking-widest font-mono">{iban.replace(/(.{4})/g, '$1 ').trim()}</p>
              
              <div className="flex items-center justify-center gap-6">
                <span className="text-7xl font-bold tabular-nums text-white drop-shadow-lg">
                  {showBalance ? formatCurrency(balance) : '€ * * * * *'}
                </span>
                <button 
                  onClick={() => setShowBalance(!showBalance)}
                  className="p-4 bg-white/10 hover:bg-white/20 rounded-full transition-colors border-2 border-white/20 text-purple-400 hover:text-purple-300"
                >
                  {showBalance ? <EyeOff className="w-10 h-10" /> : <Eye className="w-10 h-10" />}
                </button>
              </div>
            </div>
            
            <button 
              onClick={() => navigate('/atm')}
              className="mt-16 bg-purple-600 hover:bg-purple-500 py-6 px-16 rounded-2xl border-4 border-purple-500 text-3xl font-bold uppercase tracking-wider shadow-2xl transform hover:scale-105 active:scale-95 transition-all"
            >
              Finish
            </button>
          </div>
        )}
      </div>
    </div>
  );
}