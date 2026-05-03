import { useBankStore } from '../../../store';
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Search, MapPin, ChevronRight, History } from 'lucide-react';
import CustomerLayout from '../../layout/CustomerLayout';
import { clsx } from 'clsx';
import { Link } from 'react-router';

export default function CustomerDashboard() {
  const { accounts, transactions, currentUser } = useBankStore();
  const userAccounts = accounts.filter(a => a.userId === currentUser?.id);
  const totalBalance = userAccounts.reduce((sum, a) => sum + a.balance, 0);
  
  // Get recent transactions across all user accounts
  const userAccountIds = userAccounts.map(a => a.id);
  const recentTx = transactions
    .filter(t => userAccountIds.includes(t.fromAccountId!) || userAccountIds.includes(t.toAccountId!))
    .slice(0, 5);

  const formatCurrency = (val: number) => 
    new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val);

  return (
    <CustomerLayout>
      {/* Hero */}
      <div className="mb-10 text-center md:text-left mt-4 md:mt-0">
        <p className="text-gray-400 font-medium mb-2">Total Balance</p>
        <h1 className="text-5xl md:text-7xl font-bold tracking-tighter tabular-nums mb-3">
          {formatCurrency(totalBalance)}
        </h1>
        <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#00D9A3]/10 text-[#00D9A3] text-sm font-medium">
          <ArrowUpRight className="w-4 h-4" />
          <span>+€123.45 this month</span>
        </div>
      </div>

      {/* Account Cards */}
      <div className="flex overflow-x-auto gap-4 pb-6 snap-x hide-scrollbar -mx-4 px-4 md:mx-0 md:px-0">
        {userAccounts.map(account => (
          <div 
            key={account.id} 
            className={clsx(
              "flex-none w-72 md:w-80 h-48 rounded-2xl p-5 flex flex-col justify-between snap-center relative overflow-hidden group hover:-translate-y-1 transition-transform cursor-pointer shadow-xl",
              account.type === 'checking' 
                ? "bg-gradient-to-br from-[#2D1B69] to-[#1A1040] shadow-[#2D1B69]/20" 
                : "bg-gradient-to-br from-[#2A2A35] to-[#14141A] shadow-black/40"
            )}
          >
            <div className="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none" />
            
            <div>
              <p className="text-white/70 text-sm font-medium mb-1 capitalize">{account.type} Account</p>
              <h3 className="text-2xl font-bold tabular-nums">{formatCurrency(account.balance)}</h3>
            </div>
            
            <div className="space-y-1">
              <p className="font-mono text-sm tracking-widest text-white/50">{account.iban.replace(/(.{4})/g, '$1 ').trim()}</p>
              <div className="w-full h-8 flex items-end gap-1 opacity-50 group-hover:opacity-100 transition-opacity">
                {/* Mock Sparkline */}
                {[...Array(12)].map((_, i) => (
                  <div key={i} className="flex-1 bg-white/20 rounded-t-sm" style={{ height: `${Math.random() * 100}%` }} />
                ))}
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-4 gap-3 md:gap-6 mb-10">
        {[
          { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer', color: 'bg-[#7B61FF]' },
          { icon: ArrowUpRight, label: 'Send', path: '/customer/send', color: 'bg-[#2A2A35]' },
          { icon: Search, label: 'Find IBAN', path: '/customer/find', color: 'bg-[#2A2A35]' },
          { icon: MapPin, label: 'ATM', path: '/atm', color: 'bg-[#2A2A35]' },
        ].map((action, i) => (
          <Link key={i} to={action.path} className="flex flex-col items-center gap-2 group">
            <div className={clsx("w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-105 shadow-lg", action.color)}>
              <action.icon className="w-6 h-6 text-white" />
            </div>
            <span className="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors">{action.label}</span>
          </Link>
        ))}
      </div>

      {/* Recent Transactions */}
      <div>
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-bold">Recent Activity</h3>
          <Link to="/customer/transactions" className="text-sm font-medium text-[#7B61FF] hover:text-[#907aff] flex items-center gap-1">
            View All <ChevronRight className="w-4 h-4" />
          </Link>
        </div>
        
        <div className="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
          {recentTx.length > 0 ? (
            <div className="divide-y divide-white/5">
              {recentTx.map(tx => {
                const isOutgoing = userAccountIds.includes(tx.fromAccountId!);
                const Icon = isOutgoing ? ArrowUpRight : ArrowDownLeft;
                const iconColor = isOutgoing ? 'text-[#FF5E5B] bg-[#FF5E5B]/10' : 'text-[#00D9A3] bg-[#00D9A3]/10';
                
                return (
                  <div key={tx.id} className="flex items-center gap-4 p-4 hover:bg-white/[0.02] transition-colors cursor-pointer">
                    <div className={clsx("w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0", iconColor)}>
                      <Icon className="w-5 h-5" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <p className="font-medium text-white truncate">{tx.description}</p>
                      <p className="text-xs text-gray-500 capitalize">{tx.type} • {new Date(tx.timestamp).toLocaleDateString()}</p>
                    </div>
                    <div className={clsx("font-bold tabular-nums whitespace-nowrap", isOutgoing ? "text-white" : "text-[#00D9A3]")}>
                      {isOutgoing ? '-' : '+'}{formatCurrency(tx.amount)}
                    </div>
                  </div>
                );
              })}
            </div>
          ) : (
            <div className="p-8 text-center text-gray-500 flex flex-col items-center gap-3">
              <History className="w-8 h-8 opacity-20" />
              <p>No recent transactions</p>
            </div>
          )}
        </div>
      </div>
    </CustomerLayout>
  );
}