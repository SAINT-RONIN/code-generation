import { useBankStore } from '../../../store';
import EmployeeLayout from '../../layout/EmployeeLayout';
import { Search, User, MoreHorizontal, ShieldAlert, CheckCircle2 } from 'lucide-react';
import { useState } from 'react';

export default function EmployeeCustomers() {
  const { users, accounts } = useBankStore();
  const [searchTerm, setSearchTerm] = useState('');

  const customers = users.filter(u => u.role === 'customer');
  const filteredCustomers = customers.filter(c => 
    c.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    c.email.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <EmployeeLayout>
      <div className="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight mb-2">Customer Directory</h1>
          <p className="text-gray-400">View and manage all registered customers.</p>
        </div>
        
        <div className="relative md:w-80">
          <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
          <input 
            type="text" 
            placeholder="Search by name or email..." 
            value={searchTerm}
            onChange={e => setSearchTerm(e.target.value)}
            className="w-full bg-[#14141A] border border-white/10 rounded-xl py-2.5 pl-10 pr-4 text-sm focus:outline-none focus:border-[#FF5E5B] transition-colors placeholder:text-gray-600"
          />
        </div>
      </div>

      <div className="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-white/5 text-sm font-medium text-gray-400 bg-black/20">
                <th className="p-4 font-medium">Customer</th>
                <th className="p-4 font-medium">Status</th>
                <th className="p-4 font-medium">Accounts</th>
                <th className="p-4 font-medium">Joined</th>
                <th className="p-4 font-medium"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {filteredCustomers.map(customer => {
                const customerAccounts = accounts.filter(a => a.userId === customer.id);
                
                return (
                  <tr key={customer.id} className="hover:bg-white/[0.02] transition-colors group">
                    <td className="p-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-gray-400 flex-shrink-0">
                          <User className="w-5 h-5" />
                        </div>
                        <div>
                          <p className="font-medium text-white">{customer.name}</p>
                          <p className="text-sm text-gray-500">{customer.email}</p>
                        </div>
                      </div>
                    </td>
                    <td className="p-4">
                      {customer.status === 'approved' ? (
                        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#00D9A3]/10 text-[#00D9A3] text-xs font-medium">
                          <CheckCircle2 className="w-3.5 h-3.5" />
                          Approved
                        </div>
                      ) : (
                        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-[#FF5E5B]/10 text-[#FF5E5B] text-xs font-medium">
                          <ShieldAlert className="w-3.5 h-3.5" />
                          Pending
                        </div>
                      )}
                    </td>
                    <td className="p-4">
                      <p className="text-sm text-gray-300">{customerAccounts.length} accounts</p>
                    </td>
                    <td className="p-4">
                      <p className="text-sm text-gray-400">{new Date(customer.createdAt).toLocaleDateString()}</p>
                    </td>
                    <td className="p-4 text-right">
                      <button className="p-2 text-gray-500 hover:text-white transition-colors rounded-lg hover:bg-white/5 opacity-0 group-hover:opacity-100">
                        <MoreHorizontal className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          
          {filteredCustomers.length === 0 && (
            <div className="p-12 text-center text-gray-500">
              <p>No customers found matching your search.</p>
            </div>
          )}
        </div>
      </div>
    </EmployeeLayout>
  );
}