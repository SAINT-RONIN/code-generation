import { useBankStore } from '../../../store';
import CustomerLayout from '../../layout/CustomerLayout';
import { User, Mail, Phone, Hash, Calendar } from 'lucide-react';

export default function CustomerProfile() {
  const { currentUser } = useBankStore();

  if (!currentUser) return null;

  return (
    <CustomerLayout>
      <h1 className="text-3xl font-bold mb-8">Profile</h1>

      <div className="max-w-2xl">
        <div className="bg-[#14141A] border border-white/5 rounded-3xl p-8 mb-6 flex flex-col md:flex-row items-center md:items-start gap-6">
          <div className="w-24 h-24 bg-gradient-to-br from-[#7B61FF] to-[#00D9A3] rounded-full flex items-center justify-center text-3xl font-bold text-white shadow-xl flex-shrink-0">
            {currentUser.name.charAt(0)}
          </div>
          <div className="text-center md:text-left flex-1">
            <h2 className="text-2xl font-bold mb-1">{currentUser.name}</h2>
            <p className="text-gray-400 text-sm mb-4">Customer since {new Date(currentUser.createdAt).getFullYear()}</p>
            <span className="inline-flex items-center px-3 py-1 rounded-full bg-green-500/10 text-green-500 text-xs font-medium uppercase tracking-wider">
              {currentUser.status}
            </span>
          </div>
        </div>

        <div className="bg-[#14141A] border border-white/5 rounded-3xl overflow-hidden">
          <div className="p-6 border-b border-white/5">
            <h3 className="font-semibold text-lg">Personal Information</h3>
          </div>
          <div className="p-6 space-y-6">
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-gray-400">
                <User className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-0.5">Full Name</p>
                <p className="font-medium">{currentUser.name}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-gray-400">
                <Mail className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-0.5">Email Address</p>
                <p className="font-medium">{currentUser.email}</p>
              </div>
            </div>
            
            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-gray-400">
                <Hash className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-0.5">BSN (Burgerservicenummer)</p>
                <p className="font-medium font-mono">{currentUser.bsn || 'Not provided'}</p>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="w-10 h-10 rounded-full bg-white/5 flex items-center justify-center text-gray-400">
                <Phone className="w-5 h-5" />
              </div>
              <div>
                <p className="text-xs text-gray-500 mb-0.5">Phone Number</p>
                <p className="font-medium">{currentUser.phone || 'Not provided'}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </CustomerLayout>
  );
}