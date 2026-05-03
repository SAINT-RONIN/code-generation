import { useBankStore } from '../../../store';
import CustomerLayout from '../../layout/CustomerLayout';
import { clsx } from 'clsx';
import { CreditCard, MoreVertical, Plus } from 'lucide-react';

export default function CustomerAccounts() {
  const { accounts, currentUser } = useBankStore();
  const userAccounts = accounts.filter(a => a.userId === currentUser?.id);

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  return (
    <CustomerLayout>
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-3xl font-bold">Accounts</h1>
        <button className="flex items-center gap-2 bg-[#14141A] hover:bg-[#1A1A22] text-white px-4 py-2 rounded-xl border border-white/10 transition-colors">
          <Plus className="w-4 h-4" />
          <span className="text-sm font-medium">Add Account</span>
        </button>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {userAccounts.map(account => (
          <div 
            key={account.id} 
            className="bg-[#14141A] border border-white/5 rounded-2xl p-6 relative overflow-hidden group hover:border-white/10 transition-colors"
          >
            <div className="flex justify-between items-start mb-8">
              <div className="flex items-center gap-3">
                <div className={clsx(
                  "w-12 h-12 rounded-xl flex items-center justify-center",
                  account.type === 'checking' ? "bg-[#7B61FF]/10 text-[#7B61FF]" : "bg-[#00D9A3]/10 text-[#00D9A3]"
                )}>
                  <CreditCard className="w-6 h-6" />
                </div>
                <div>
                  <h3 className="font-medium capitalize text-lg">{account.type} Account</h3>
                  <p className="text-sm text-gray-400 font-mono">{account.iban}</p>
                </div>
              </div>
              <button className="p-2 text-gray-500 hover:text-white transition-colors rounded-lg hover:bg-white/5">
                <MoreVertical className="w-5 h-5" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div>
                <p className="text-sm text-gray-400 mb-1">Available Balance</p>
                <h4 className="text-3xl font-bold tabular-nums">{formatCurrency(account.balance)}</h4>
              </div>
              
              <div className="grid grid-cols-2 gap-4 pt-4 border-t border-white/5">
                <div>
                  <p className="text-xs text-gray-500 mb-1">Daily Limit</p>
                  <p className="font-medium">{formatCurrency(account.dailyLimit)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">Absolute Limit</p>
                  <p className="font-medium">{formatCurrency(account.absoluteLimit)}</p>
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    </CustomerLayout>
  );
}