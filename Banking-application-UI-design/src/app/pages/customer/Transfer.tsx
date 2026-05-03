import { useState } from 'react';
import { useBankStore } from '../../../store';
import CustomerLayout from '../../layout/CustomerLayout';
import { ArrowRight, CheckCircle2, AlertCircle } from 'lucide-react';
import { clsx } from 'clsx';

export default function CustomerTransfer() {
  const { accounts, transfer, currentUser } = useBankStore();
  const userAccounts = accounts.filter(a => a.userId === currentUser?.id);
  
  const [step, setStep] = useState(1);
  const [fromAccount, setFromAccount] = useState(userAccounts[0]?.id || '');
  const [toIban, setToIban] = useState('');
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  const selectedAccount = userAccounts.find(a => a.id === fromAccount);
  const numAmount = parseFloat(amount.replace(',', '.'));

  const handleReview = () => {
    setError('');
    if (!fromAccount) return setError('Select a source account');
    if (!toIban || toIban.length < 10) return setError('Enter a valid IBAN');
    if (isNaN(numAmount) || numAmount <= 0) return setError('Enter a valid amount');
    if (selectedAccount && selectedAccount.balance - numAmount < selectedAccount.absoluteLimit) {
      return setError(`Insufficient funds. Absolute limit is ${formatCurrency(selectedAccount.absoluteLimit)}`);
    }
    setStep(2);
  };

  const handleConfirm = () => {
    const success = transfer(fromAccount, toIban, numAmount, description || 'Transfer');
    if (success) setStep(3);
    else setError('Transfer failed. Please check details and try again.');
  };

  return (
    <CustomerLayout>
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold tracking-tight mb-8">Make a Transfer</h1>
        
        {step === 1 && (
          <div className="space-y-8 animate-in fade-in duration-300">
            {error && (
              <div className="p-4 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl text-[#FF5E5B] flex items-center gap-3">
                <AlertCircle className="w-5 h-5" />
                <p className="font-medium">{error}</p>
              </div>
            )}
            
            <section>
              <h2 className="text-lg font-bold mb-4">From Account</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                {userAccounts.map(account => (
                  <button
                    key={account.id}
                    onClick={() => setFromAccount(account.id)}
                    className={clsx(
                      "p-4 rounded-xl border text-left transition-all relative overflow-hidden",
                      fromAccount === account.id 
                        ? "border-[#7B61FF] bg-[#7B61FF]/10" 
                        : "border-white/5 bg-[#14141A] hover:bg-[#1C1C24]"
                    )}
                  >
                    {fromAccount === account.id && (
                      <div className="absolute top-0 right-0 p-3">
                        <div className="w-2 h-2 rounded-full bg-[#7B61FF]" />
                      </div>
                    )}
                    <p className="text-sm text-gray-400 capitalize mb-1">{account.type}</p>
                    <p className="text-xl font-bold tabular-nums mb-1">{formatCurrency(account.balance)}</p>
                    <p className="text-xs font-mono text-gray-500">{account.iban}</p>
                  </button>
                ))}
              </div>
            </section>

            <section className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">To IBAN</label>
                <input 
                  type="text" 
                  value={toIban}
                  onChange={e => setToIban(e.target.value.toUpperCase())}
                  placeholder="NL00 INHO 0000 0000 00"
                  className="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none font-mono text-lg transition-all uppercase"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Amount</label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-2xl text-gray-500">€</span>
                  <input 
                    type="number" 
                    value={amount}
                    onChange={e => setAmount(e.target.value)}
                    placeholder="0.00"
                    step="0.01"
                    className="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 pl-10 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none text-2xl font-bold tabular-nums transition-all"
                  />
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">Description (Optional)</label>
                <input 
                  type="text" 
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  placeholder="Dinner last night"
                  className="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all"
                />
              </div>
            </section>

            <button 
              onClick={handleReview}
              className="w-full bg-[#7B61FF] hover:bg-[#6A52E5] text-white p-4 rounded-xl font-bold text-lg transition-colors flex items-center justify-center gap-2 shadow-lg shadow-[#7B61FF]/20"
            >
              Review Transfer <ArrowRight className="w-5 h-5" />
            </button>
          </div>
        )}

        {step === 2 && selectedAccount && (
          <div className="space-y-6 animate-in slide-in-from-right-8 duration-300">
            <div className="bg-[#14141A] border border-white/5 rounded-2xl p-6">
              <h2 className="text-xl font-bold mb-6 pb-4 border-b border-white/5">Review Details</h2>
              
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-gray-400">Amount</span>
                  <span className="text-2xl font-bold tabular-nums">{formatCurrency(numAmount)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-400">From</span>
                  <div className="text-right">
                    <p className="font-medium capitalize">{selectedAccount.type}</p>
                    <p className="text-sm text-gray-500 font-mono">{selectedAccount.iban}</p>
                  </div>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-400">To</span>
                  <span className="font-medium font-mono">{toIban}</span>
                </div>
                {description && (
                  <div className="flex justify-between items-center">
                    <span className="text-gray-400">Description</span>
                    <span className="font-medium">{description}</span>
                  </div>
                )}
              </div>
            </div>

            <div className="flex gap-4">
              <button 
                onClick={() => setStep(1)}
                className="flex-1 bg-[#1C1C24] hover:bg-[#252530] text-white p-4 rounded-xl font-bold transition-colors"
              >
                Back
              </button>
              <button 
                onClick={handleConfirm}
                className="flex-[2] bg-[#7B61FF] hover:bg-[#6A52E5] text-white p-4 rounded-xl font-bold transition-colors shadow-lg shadow-[#7B61FF]/20"
              >
                Confirm Transfer
              </button>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="text-center py-12 animate-in zoom-in-95 duration-500">
            <div className="w-24 h-24 bg-[#00D9A3]/10 text-[#00D9A3] rounded-full flex items-center justify-center mx-auto mb-6">
              <CheckCircle2 className="w-12 h-12" />
            </div>
            <h2 className="text-3xl font-bold mb-2">Transfer Successful</h2>
            <p className="text-gray-400 mb-8">{formatCurrency(numAmount)} sent to {toIban}</p>
            
            <button 
              onClick={() => {
                setStep(1);
                setAmount('');
                setToIban('');
                setDescription('');
              }}
              className="bg-[#1C1C24] hover:bg-[#252530] text-white px-8 py-3 rounded-xl font-bold transition-colors inline-flex items-center gap-2"
            >
              Make another transfer
            </button>
          </div>
        )}
      </div>
    </CustomerLayout>
  );
}