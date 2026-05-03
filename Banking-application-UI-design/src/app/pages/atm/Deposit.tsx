import { useState } from 'react';
import { useBankStore } from '../../../store';
import { useNavigate } from 'react-router';
import { ArrowLeft, ChevronRight, Inbox } from 'lucide-react';

export default function AtmDeposit() {
  const { accounts, deposit } = useBankStore();
  const navigate = useNavigate();
  
  const [step, setStep] = useState(1);
  const [iban, setIban] = useState('');
  const [amount, setAmount] = useState('');
  const [error, setError] = useState('');
  
  const handleNext = () => {
    setError('');
    const account = accounts.find(a => a.iban.replace(/\s/g, '') === iban.replace(/\s/g, '').toUpperCase());
    if (!account) return setError('Invalid Card / IBAN');
    setStep(2);
  };

  const handleDeposit = () => {
    setError('');
    const numAmount = parseInt(amount);
    if (isNaN(numAmount) || numAmount <= 0 || numAmount % 10 !== 0) {
      return setError('Invalid amount. Must be a multiple of 10.');
    }

    const account = accounts.find(a => a.iban.replace(/\s/g, '') === iban.replace(/\s/g, '').toUpperCase());
    if (!account) return;
    
    deposit(account.id, numAmount);
    setStep(3);
    setTimeout(() => {
      navigate('/atm');
    }, 5000);
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
                className="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-3xl font-mono text-center uppercase focus:outline-none focus:border-green-500 transition-colors"
              />
              <button 
                onClick={handleNext}
                className="bg-green-600 hover:bg-green-500 px-8 rounded-2xl border-4 border-green-500 flex items-center justify-center transition-colors"
              >
                <ChevronRight className="w-12 h-12" />
              </button>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="text-center animate-in slide-in-from-right-12 duration-300">
            <h2 className="text-5xl font-bold mb-12 uppercase tracking-wider">Insert Cash</h2>
            
            {error && <p className="text-red-500 text-2xl font-bold mb-8 uppercase bg-red-500/10 py-4 rounded-xl max-w-xl mx-auto">{error}</p>}
            
            <div className="max-w-xl mx-auto flex gap-4 mb-16">
              <span className="bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-4xl font-bold flex items-center">€</span>
              <input 
                type="number" 
                value={amount}
                onChange={e => setAmount(e.target.value)}
                placeholder="0"
                className="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-4xl font-bold text-center focus:outline-none focus:border-green-500 transition-colors"
              />
            </div>

            <button 
              onClick={handleDeposit}
              className="bg-green-600 hover:bg-green-500 py-6 px-16 rounded-2xl border-4 border-green-500 text-3xl font-bold uppercase tracking-wider shadow-2xl transform hover:scale-105 active:scale-95 transition-all mx-auto"
            >
              Deposit
            </button>
            
            <div className="mt-16 flex flex-col items-center opacity-50">
              <Inbox className="w-32 h-32 mb-4" />
              <p className="text-xl uppercase tracking-widest">Cash Slot Simulator</p>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="text-center animate-in zoom-in-95 duration-500">
            <div className="w-48 h-48 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-12">
              <Inbox className="w-24 h-24 text-white" />
            </div>
            <h2 className="text-5xl font-bold mb-6 uppercase tracking-wider text-green-400">Deposit Accepted</h2>
            <p className="text-3xl text-gray-400 font-mono">Amount: € {amount}.00</p>
            <p className="text-xl text-gray-500 mt-12 uppercase tracking-widest">Returning to menu...</p>
          </div>
        )}
      </div>
    </div>
  );
}