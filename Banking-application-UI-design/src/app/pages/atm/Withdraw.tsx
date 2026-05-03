import { useState } from 'react';
import { useBankStore } from '../../../store';
import { useNavigate } from 'react-router';
import { ArrowLeft, CreditCard, ChevronRight } from 'lucide-react';

export default function AtmWithdraw() {
  const { accounts, withdraw } = useBankStore();
  const navigate = useNavigate();
  
  const [step, setStep] = useState(1);
  const [iban, setIban] = useState('');
  const [amount, setAmount] = useState<number | null>(null);
  const [error, setError] = useState('');
  
  const handleNext = () => {
    setError('');
    const account = accounts.find(a => a.iban.replace(/\s/g, '') === iban.replace(/\s/g, '').toUpperCase());
    if (!account) return setError('Invalid Card / IBAN');
    setStep(2);
  };

  const handleWithdraw = (selectedAmount: number) => {
    setError('');
    const account = accounts.find(a => a.iban.replace(/\s/g, '') === iban.replace(/\s/g, '').toUpperCase());
    if (!account) return;
    
    if (withdraw(account.id, selectedAmount)) {
      setAmount(selectedAmount);
      setStep(3);
      setTimeout(() => {
        navigate('/atm');
      }, 5000);
    } else {
      setError('Insufficient funds or limit exceeded.');
    }
  };

  return (
    <div className="min-h-screen bg-black text-white p-8 font-sans flex flex-col items-center justify-center relative">
      <button 
        onClick={() => navigate('/atm')}
        className="absolute top-8 left-8 flex items-center gap-2 bg-white/10 hover:bg-white/20 px-6 py-4 rounded-xl text-xl font-bold uppercase tracking-wider transition-colors border-2 border-white/20"
      >
        <ArrowLeft className="w-6 h-6" />
        Cancel
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
                className="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-3xl font-mono text-center uppercase focus:outline-none focus:border-blue-500 transition-colors"
              />
              <button 
                onClick={handleNext}
                className="bg-blue-600 hover:bg-blue-500 px-8 rounded-2xl border-4 border-blue-500 flex items-center justify-center transition-colors"
              >
                <ChevronRight className="w-12 h-12" />
              </button>
            </div>
            
            <div className="mt-16 flex flex-col items-center opacity-50">
              <CreditCard className="w-32 h-32 mb-4" />
              <p className="text-xl uppercase tracking-widest">Card Slot Simulator</p>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="animate-in slide-in-from-right-12 duration-300">
            <h2 className="text-5xl font-bold mb-12 text-center uppercase tracking-wider">Select Amount</h2>
            
            {error && <p className="text-red-500 text-2xl font-bold mb-8 text-center uppercase bg-red-500/10 py-4 rounded-xl mx-auto max-w-xl">{error}</p>}
            
            <div className="grid grid-cols-2 gap-6 max-w-3xl mx-auto">
              {[20, 50, 100, 200, 500].map(amt => (
                <button 
                  key={amt}
                  onClick={() => handleWithdraw(amt)}
                  className="bg-blue-600 hover:bg-blue-500 py-10 rounded-2xl border-4 border-blue-500 text-4xl font-bold tabular-nums shadow-2xl transform hover:scale-105 active:scale-95 transition-all"
                >
                  € {amt}
                </button>
              ))}
              <button className="bg-gray-800 hover:bg-gray-700 py-10 rounded-2xl border-4 border-gray-600 text-3xl font-bold uppercase tracking-wider shadow-2xl transform hover:scale-105 active:scale-95 transition-all">
                Other
              </button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="text-center animate-in zoom-in-95 duration-500">
            <div className="w-48 h-48 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-12 animate-pulse">
              <span className="text-6xl font-bold">€</span>
            </div>
            <h2 className="text-5xl font-bold mb-6 uppercase tracking-wider text-blue-400">Please take your cash</h2>
            <p className="text-3xl text-gray-400 font-mono">Amount: € {amount}.00</p>
            <p className="text-xl text-gray-500 mt-12 uppercase tracking-widest">Returning to menu...</p>
          </div>
        )}
      </div>
    </div>
  );
}