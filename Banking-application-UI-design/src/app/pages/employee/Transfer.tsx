import { useState } from 'react';
import { useBankStore } from '../../../store';
import EmployeeLayout from '../../layout/EmployeeLayout';
import { ArrowRight, CheckCircle2, AlertCircle } from 'lucide-react';

export default function EmployeeTransfer() {
  const { accounts, transfer } = useBankStore();
  
  const [fromIban, setFromIban] = useState('');
  const [toIban, setToIban] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const numAmount = parseFloat(amount.replace(',', '.'));

  const handleTransfer = () => {
    setError('');
    setSuccess(false);

    const fromAccount = accounts.find(a => a.iban.replace(/\s/g, '') === fromIban.replace(/\s/g, ''));
    if (!fromAccount) return setError('Source IBAN not found in system.');
    
    if (!toIban || toIban.length < 10) return setError('Enter a valid destination IBAN.');
    if (isNaN(numAmount) || numAmount <= 0) return setError('Enter a valid amount.');

    const result = transfer(fromAccount.id, toIban, numAmount, description || 'Admin Override Transfer');
    if (result) {
      setSuccess(true);
      setFromIban('');
      setToIban('');
      setAmount('');
      setDescription('');
    } else {
      setError('Transfer failed. Check account limits and balance.');
    }
  };

  return (
    <EmployeeLayout>
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Manual Transfer</h1>
        <p className="text-gray-400 mb-8">Override tool to move funds between any accounts.</p>
        
        {success && (
          <div className="mb-8 p-4 bg-[#00D9A3]/10 border border-[#00D9A3]/20 rounded-xl flex items-center gap-3 text-[#00D9A3] animate-in fade-in">
            <CheckCircle2 className="w-6 h-6" />
            <p className="font-medium">Transfer executed successfully.</p>
          </div>
        )}

        {error && (
          <div className="mb-8 p-4 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl text-[#FF5E5B] flex items-center gap-3 animate-in fade-in">
            <AlertCircle className="w-6 h-6" />
            <p className="font-medium">{error}</p>
          </div>
        )}

        <div className="bg-[#14141A] border border-white/5 rounded-2xl p-6 space-y-6">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Source IBAN</label>
            <input 
              type="text" 
              value={fromIban}
              onChange={e => setFromIban(e.target.value.toUpperCase())}
              placeholder="NL00 INHO 0000 0000 00"
              className="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B] font-mono uppercase"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Destination IBAN</label>
            <input 
              type="text" 
              value={toIban}
              onChange={e => setToIban(e.target.value.toUpperCase())}
              placeholder="NL00 INHO 0000 0000 00"
              className="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B] font-mono uppercase"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Amount (€)</label>
            <input 
              type="number" 
              value={amount}
              onChange={e => setAmount(e.target.value)}
              placeholder="0.00"
              className="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B]"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-2">Internal Note</label>
            <input 
              type="text" 
              value={description}
              onChange={e => setDescription(e.target.value)}
              placeholder="Reason for manual override"
              className="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B]"
            />
          </div>

          <button 
            onClick={handleTransfer}
            className="w-full bg-[#FF5E5B] hover:bg-[#E54542] text-white p-4 rounded-xl font-bold text-lg transition-colors flex items-center justify-center gap-2 mt-4"
          >
            Execute Transfer <ArrowRight className="w-5 h-5" />
          </button>
        </div>
      </div>
    </EmployeeLayout>
  );
}