import { useBankStore } from '../../../store';
import CustomerLayout from '../../layout/CustomerLayout';
import { User, ArrowRight, Search } from 'lucide-react';
import { Link } from 'react-router';

export default function CustomerSendMoney() {
  const mockContacts = [
    { id: '1', name: 'Alice Cooper', iban: 'NL12INHO0123456789' },
    { id: '2', name: 'Bob Marley', iban: 'NL34INHO0987654321' },
    { id: '3', name: 'Charlie Puth', iban: 'NL56INHO0112233445' },
  ];

  return (
    <CustomerLayout>
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold tracking-tight mb-8">Send Money</h1>
        
        <div className="relative mb-8">
          <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" />
          <input 
            type="text" 
            placeholder="Name, @username, email, phone" 
            className="w-full bg-[#14141A] border border-white/5 rounded-2xl py-4 pl-12 pr-4 text-white focus:outline-none focus:border-[#7B61FF] transition-colors placeholder:text-gray-600 text-lg"
          />
        </div>

        <h2 className="text-lg font-bold mb-4">Recent Contacts</h2>
        <div className="space-y-3">
          {mockContacts.map(contact => (
            <Link 
              key={contact.id}
              to="/customer/transfer"
              className="flex items-center justify-between bg-[#14141A] border border-white/5 rounded-2xl p-4 hover:border-white/10 transition-colors group"
            >
              <div className="flex items-center gap-4">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-[#7B61FF] to-[#00D9A3] flex items-center justify-center text-white font-bold">
                  {contact.name.charAt(0)}
                </div>
                <div>
                  <h3 className="font-medium">{contact.name}</h3>
                  <p className="text-sm text-gray-500 font-mono">{contact.iban}</p>
                </div>
              </div>
              <ArrowRight className="w-5 h-5 text-gray-600 group-hover:text-white transition-colors" />
            </Link>
          ))}
        </div>
      </div>
    </CustomerLayout>
  );
}