import { useBankStore } from '../../../store';
import CustomerLayout from '../../layout/CustomerLayout';
import { ArrowUpRight, ArrowDownLeft, Search, Filter } from 'lucide-react';
import { clsx } from 'clsx';
import { useState } from 'react';

export default function CustomerTransactions() {
  const { transactions, accounts, currentUser } = useBankStore();
  const [searchTerm, setSearchTerm] = useState('');
  
  const userAccounts = accounts.filter(a => a.userId === currentUser?.id);
  const userAccountIds = userAccounts.map(a => a.id);
  
  const userTransactions = transactions
    .filter(t => userAccountIds.includes(t.fromAccountId!) || userAccountIds.includes(t.toAccountId!))
    .filter(t => t.description.toLowerCase().includes(searchTerm.toLowerCase()));

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  return (
    <CustomerLayout>
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <h1 className="text-3xl font-bold">Transactions</h1>
        
        <div className="flex items-center gap-3">
          <div className="relative flex-1 md:w-64">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
            <input 
              type="text" 
              placeholder="Search transactions..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full bg-[#14141A] border border-white/10 rounded-xl py-2 pl-9 pr-4 text-sm focus:outline-none focus:border-[#7B61FF] transition-colors placeholder:text-gray-600"
            />
          </div>
          <button className="bg-[#14141A] border border-white/10 p-2.5 rounded-xl hover:bg-white/5 transition-colors">
            <Filter className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
        {userTransactions.length > 0 ? (
          <div className="divide-y divide-white/5">
            {userTransactions.map(tx => {
              const isOutgoing = userAccountIds.includes(tx.fromAccountId!);
              const Icon = isOutgoing ? ArrowUpRight : ArrowDownLeft;
              const iconColor = isOutgoing ? 'text-[#FF5E5B] bg-[#FF5E5B]/10' : 'text-[#00D9A3] bg-[#00D9A3]/10';
              
              return (
                <div key={tx.id} className="flex items-center gap-4 p-4 hover:bg-white/[0.02] transition-colors cursor-pointer">
                  <div className={clsx("w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0", iconColor)}>
                    <Icon className="w-5 h-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-white truncate text-lg">{tx.description}</p>
                    <p className="text-sm text-gray-500 capitalize">{tx.type} • {new Date(tx.timestamp).toLocaleString()}</p>
                  </div>
                  <div className={clsx("font-bold tabular-nums whitespace-nowrap text-lg", isOutgoing ? "text-white" : "text-[#00D9A3]")}>
                    {isOutgoing ? '-' : '+'}{formatCurrency(tx.amount)}
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-12 text-center text-gray-500">
            <p>No transactions found.</p>
          </div>
        )}
      </div>
    </CustomerLayout>
  );
}