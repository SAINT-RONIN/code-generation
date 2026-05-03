import CustomerLayout from '../../layout/CustomerLayout';
import { Search, Copy, Check } from 'lucide-react';
import { useState } from 'react';
import { clsx } from 'clsx';

export default function CustomerFindIban() {
  const [copied, setCopied] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');

  const mockResult = searchTerm.length > 2 ? {
    name: "John Doe",
    iban: "NL99INHO0123456789"
  } : null;

  const handleCopy = () => {
    if (mockResult) {
      navigator.clipboard.writeText(mockResult.iban);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <CustomerLayout>
      <div className="max-w-2xl mx-auto">
        <h1 className="text-3xl font-bold tracking-tight mb-2">Find IBAN</h1>
        <p className="text-gray-400 mb-8">Search for registered users by their phone number or email to get their IBAN.</p>
        
        <div className="relative mb-8">
          <Search className="w-5 h-5 absolute left-4 top-1/2 -translate-y-1/2 text-gray-500" />
          <input 
            type="text" 
            placeholder="Enter email or phone number" 
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-[#14141A] border border-white/5 rounded-2xl py-4 pl-12 pr-4 text-white focus:outline-none focus:border-[#7B61FF] transition-colors placeholder:text-gray-600 text-lg"
          />
        </div>

        {mockResult ? (
          <div className="bg-[#14141A] border border-[#7B61FF]/30 rounded-2xl p-6 animate-in fade-in duration-300">
            <h3 className="text-sm text-gray-400 mb-1">Found Account</h3>
            <p className="text-xl font-bold mb-4">{mockResult.name}</p>
            
            <div className="flex items-center justify-between bg-black/20 p-4 rounded-xl border border-white/5">
              <span className="font-mono text-lg tracking-wider">{mockResult.iban}</span>
              <button 
                onClick={handleCopy}
                className={clsx(
                  "p-2 rounded-lg transition-colors",
                  copied ? "bg-[#00D9A3]/20 text-[#00D9A3]" : "bg-white/5 text-gray-400 hover:text-white hover:bg-white/10"
                )}
              >
                {copied ? <Check className="w-5 h-5" /> : <Copy className="w-5 h-5" />}
              </button>
            </div>
          </div>
        ) : searchTerm.length > 0 ? (
          <div className="text-center py-12 text-gray-500">
            <p>Keep typing to search...</p>
          </div>
        ) : null}
      </div>
    </CustomerLayout>
  );
}