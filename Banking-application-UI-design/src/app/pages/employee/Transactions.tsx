import { useBankStore } from '../../../store';
import EmployeeLayout from '../../layout/EmployeeLayout';
import { Search, ArrowUpRight, ArrowDownLeft, FileText, ArrowLeftRight } from 'lucide-react';
import { useState } from 'react';
import { clsx } from 'clsx';

export default function EmployeeTransactions() {
  const { transactions, accounts, users } = useBankStore();
  const [searchTerm, setSearchTerm] = useState('');

  const filteredTransactions = transactions.filter(t => 
    t.description.toLowerCase().includes(searchTerm.toLowerCase()) || 
    t.type.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  return (
    <EmployeeLayout>
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">System Ledger</h1>
          <p className="text-gray-400">Global view of all platform transactions.</p>
        </div>
        
        <div className="relative md:w-80">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input 
            type="text" 
            placeholder="Search transactions..." 
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="w-full bg-[#14141A] border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-sm focus:outline-none focus:border-[#FF5E5B] transition-colors placeholder:text-gray-600"
          />
        </div>
      </div>

      <div className="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
        {filteredTransactions.length > 0 ? (
          <div className="divide-y divide-white/5">
            {filteredTransactions.map(tx => {
              const fromAccount = accounts.find(a => a.id === tx.fromAccountId);
              const toAccount = accounts.find(a => a.id === tx.toAccountId);
              const isSystem = !fromAccount || !toAccount; // simple logic for system icon

              let Icon = ArrowLeftRight;
              let iconColor = 'text-[#7B61FF] bg-[#7B61FF]/10';
              if (tx.type === 'deposit') { Icon = ArrowDownLeft; iconColor = 'text-[#00D9A3] bg-[#00D9A3]/10'; }
              if (tx.type === 'withdrawal') { Icon = ArrowUpRight; iconColor = 'text-[#FF5E5B] bg-[#FF5E5B]/10'; }

              return (
                <div key={tx.id} className="flex flex-col md:flex-row md:items-center gap-4 p-5 hover:bg-white/[0.02] transition-colors">
                  <div className="flex items-center gap-4 flex-1">
                    <div className={clsx("w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0", iconColor)}>
                      <Icon className="w-5 h-5" />
                    </div>
                    <div>
                      <p className="font-bold text-white text-lg mb-1">{tx.description}</p>
                      <p className="text-sm text-gray-400 capitalize flex items-center gap-2">
                        {tx.type} • {new Date(tx.timestamp).toLocaleString()}
                        <span className="bg-white/10 px-2 py-0.5 rounded text-xs">{tx.initiator}</span>
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex items-center justify-between md:flex-col md:items-end gap-1 ml-16 md:ml-0">
                    <span className="font-bold text-xl tabular-nums">{formatCurrency(tx.amount)}</span>
                    <span className="text-xs text-gray-500 font-mono">ID: {tx.id.substring(0,8)}</span>
                  </div>
                </div>
              );
            })}
          </div>
        ) : (
          <div className="p-12 text-center text-gray-500 flex flex-col items-center">
            <FileText className="w-10 h-10 mb-4 opacity-20" />
            <p>No transactions found matching your search.</p>
          </div>
        )}
      </div>
    </EmployeeLayout>
  );
}