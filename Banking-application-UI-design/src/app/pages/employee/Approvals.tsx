import { useState } from 'react';
import { useBankStore } from '../../../store';
import EmployeeLayout from '../../layout/EmployeeLayout';
import { CheckCircle, XCircle, Search, Filter } from 'lucide-react';
import { clsx } from 'clsx';

export default function EmployeeApprovals() {
  const { users, approveCustomer } = useBankStore();
  const pendingUsers = users.filter(u => u.status === 'pending');
  
  const [selectedUser, setSelectedUser] = useState<string | null>(null);
  const [dailyLimit, setDailyLimit] = useState('2000');
  const [absoluteLimit, setAbsoluteLimit] = useState('0');

  const handleApprove = (userId: string) => {
    approveCustomer(userId, {
      daily: parseFloat(dailyLimit),
      absolute: parseFloat(absoluteLimit)
    });
    setSelectedUser(null);
  };

  return (
    <EmployeeLayout>
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
        <div>
          <h1 className="text-2xl font-bold tracking-tight">Pending Approvals</h1>
          <p className="text-gray-400 text-sm">{pendingUsers.length} customers waiting for review</p>
        </div>
        
        <div className="flex items-center gap-2">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
            <input 
              type="text" 
              placeholder="Search..." 
              className="bg-[#14141A] border border-white/5 rounded-lg py-2 pl-9 pr-4 text-sm w-48 focus:w-64 transition-all outline-none focus:ring-1 focus:ring-[#FF5E5B]/50"
            />
          </div>
          <button className="p-2 bg-[#14141A] border border-white/5 rounded-lg text-gray-400 hover:text-white">
            <Filter className="w-4 h-4" />
          </button>
        </div>
      </div>

      {pendingUsers.length === 0 ? (
        <div className="text-center py-20 bg-[#14141A] border border-white/5 rounded-2xl">
          <div className="w-16 h-16 bg-[#00D9A3]/10 text-[#00D9A3] rounded-full flex items-center justify-center mx-auto mb-4">
            <CheckCircle className="w-8 h-8" />
          </div>
          <h2 className="text-xl font-bold mb-1">All caught up</h2>
          <p className="text-gray-500">No pending approvals at the moment.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
          {pendingUsers.map(user => (
            <div key={user.id} className="bg-[#14141A] border border-white/5 rounded-2xl p-5 flex flex-col">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-gray-700 to-gray-600 flex items-center justify-center font-bold">
                    {user.name.charAt(0)}
                  </div>
                  <div>
                    <h3 className="font-bold">{user.name}</h3>
                    <p className="text-xs text-gray-500">{user.email}</p>
                  </div>
                </div>
                <span className="text-xs font-medium px-2 py-1 rounded bg-yellow-500/10 text-yellow-500">
                  Review
                </span>
              </div>
              
              <div className="space-y-2 mb-6 flex-1 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-500">Phone</span>
                  <span className="text-gray-300">{user.phone || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">BSN</span>
                  <span className="text-gray-300">{user.bsn || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-500">Applied</span>
                  <span className="text-gray-300">{new Date(user.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
              
              {selectedUser === user.id ? (
                <div className="space-y-3 pt-4 border-t border-white/5 animate-in slide-in-from-top-2">
                  <div className="grid grid-cols-2 gap-2">
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Daily Limit (€)</label>
                      <input 
                        type="number" 
                        value={dailyLimit}
                        onChange={e => setDailyLimit(e.target.value)}
                        className="w-full bg-[#1C1C24] border border-white/10 rounded px-3 py-1.5 text-sm"
                      />
                    </div>
                    <div>
                      <label className="block text-xs text-gray-500 mb-1">Absolute Limit</label>
                      <input 
                        type="number" 
                        value={absoluteLimit}
                        onChange={e => setAbsoluteLimit(e.target.value)}
                        className="w-full bg-[#1C1C24] border border-white/10 rounded px-3 py-1.5 text-sm"
                      />
                    </div>
                  </div>
                  <div className="flex gap-2">
                    <button 
                      onClick={() => setSelectedUser(null)}
                      className="flex-1 px-3 py-2 text-sm text-gray-400 hover:text-white bg-white/5 rounded-lg"
                    >
                      Cancel
                    </button>
                    <button 
                      onClick={() => handleApprove(user.id)}
                      className="flex-1 px-3 py-2 text-sm font-bold text-white bg-[#00D9A3] hover:bg-[#00c091] rounded-lg flex items-center justify-center gap-1 shadow-lg shadow-[#00D9A3]/20"
                    >
                      <CheckCircle className="w-4 h-4" /> Approve
                    </button>
                  </div>
                </div>
              ) : (
                <div className="flex gap-2 pt-4 border-t border-white/5 mt-auto">
                  <button className="px-3 py-2 text-sm text-[#FF5E5B] hover:bg-[#FF5E5B]/10 rounded-lg flex items-center gap-1 transition-colors">
                    <XCircle className="w-4 h-4" /> Deny
                  </button>
                  <button 
                    onClick={() => setSelectedUser(user.id)}
                    className="flex-1 px-3 py-2 text-sm font-bold text-white bg-[#7B61FF] hover:bg-[#6A52E5] rounded-lg transition-colors"
                  >
                    Review & Set Limits
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </EmployeeLayout>
  );
}