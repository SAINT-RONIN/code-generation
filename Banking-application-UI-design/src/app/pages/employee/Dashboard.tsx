import { useBankStore } from '../../../store';
import EmployeeLayout from '../../layout/EmployeeLayout';
import { Users, UserPlus, FileText, ArrowLeftRight, TrendingUp } from 'lucide-react';
import { Link } from 'react-router';

export default function EmployeeDashboard() {
  const { users, transactions } = useBankStore();
  
  const customers = users.filter(u => u.role === 'customer');
  const pendingCount = customers.filter(c => c.status === 'pending').length;
  
  // Today's transactions
  const today = new Date().toISOString().split('T')[0];
  const todayTx = transactions.filter(t => t.timestamp.startsWith(today));
  const totalVolume = todayTx.reduce((sum, t) => sum + t.amount, 0);

  const stats = [
    { label: 'Total Customers', value: customers.length, icon: Users, color: 'text-blue-400', bg: 'bg-blue-400/10' },
    { label: 'Pending Approvals', value: pendingCount, icon: UserPlus, color: 'text-[#00D9A3]', bg: 'bg-[#00D9A3]/10' },
    { label: "Today's TXs", value: todayTx.length, icon: FileText, color: 'text-[#7B61FF]', bg: 'bg-[#7B61FF]/10' },
    { label: 'Volume Today', value: `€${totalVolume.toLocaleString()}`, icon: TrendingUp, color: 'text-[#FF5E5B]', bg: 'bg-[#FF5E5B]/10' },
  ];

  return (
    <EmployeeLayout>
      <div className="mb-8">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Overview</h1>
        <p className="text-gray-400">Welcome back to the staff portal.</p>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
        {stats.map((stat, i) => (
          <div key={i} className="bg-[#14141A] border border-white/5 rounded-2xl p-5 hover:bg-[#1C1C24] transition-colors">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center mb-4 ${stat.bg} ${stat.color}`}>
              <stat.icon className="w-5 h-5" />
            </div>
            <p className="text-sm font-medium text-gray-400 mb-1">{stat.label}</p>
            <p className="text-2xl font-bold tabular-nums">{stat.value}</p>
          </div>
        ))}
      </div>

      <h2 className="text-xl font-bold mb-4">Quick Tasks</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { title: 'Approve Customers', desc: `${pendingCount} waiting for review`, icon: UserPlus, path: '/employee/approvals' },
          { title: 'Customer Directory', desc: 'Search and manage accounts', icon: Users, path: '/employee/customers' },
          { title: 'System Transactions', desc: 'View global ledger', icon: FileText, path: '/employee/transactions' },
          { title: 'Manual Transfer', desc: 'Move funds between users', icon: ArrowLeftRight, path: '/employee/transfer' },
        ].map((task, i) => (
          <Link 
            key={i} 
            to={task.path}
            className="group bg-[#14141A] border border-white/5 rounded-2xl p-6 hover:border-[#FF5E5B]/30 hover:bg-[#1C1C24] transition-all"
          >
            <div className="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center mb-4 group-hover:bg-[#FF5E5B]/10 group-hover:text-[#FF5E5B] transition-colors">
              <task.icon className="w-6 h-6" />
            </div>
            <h3 className="font-bold mb-1">{task.title}</h3>
            <p className="text-sm text-gray-400">{task.desc}</p>
          </Link>
        ))}
      </div>
    </EmployeeLayout>
  );
}