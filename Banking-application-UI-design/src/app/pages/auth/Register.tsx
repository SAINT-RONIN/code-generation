import { useState } from 'react';
import { useNavigate, Link } from 'react-router';
import { useBankStore } from '../../../store';
import { User as UserIcon, Mail, Phone, Lock, CreditCard, ChevronRight } from 'lucide-react';

export default function Register() {
  const [step, setStep] = useState(1);
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    bsn: '',
    password: ''
  });
  
  const { register } = useBankStore();
  const navigate = useNavigate();

  const handleNext = (e: React.FormEvent) => {
    e.preventDefault();
    if (step < 3) setStep(step + 1);
    else {
      register({
        name: `${formData.firstName} ${formData.lastName}`,
        email: formData.email,
        phone: formData.phone,
        bsn: formData.bsn,
      });
      navigate('/pending-approval');
    }
  };

  const updateField = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  return (
    <div>
      <div className="flex gap-2 mb-8">
        {[1, 2, 3].map(i => (
          <div key={i} className={`h-1.5 flex-1 rounded-full ${step >= i ? 'bg-[#7B61FF]' : 'bg-white/10'}`} />
        ))}
      </div>
      
      <form onSubmit={handleNext} className="space-y-6">
        {step === 1 && (
          <div className="space-y-4 animate-in fade-in slide-in-from-right-4 duration-300">
            <h2 className="text-xl font-bold mb-4">Personal Details</h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1.5">First Name</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <UserIcon className="h-5 w-5 text-gray-500" />
                  </div>
                  <input required type="text" value={formData.firstName} onChange={e => updateField('firstName', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1.5">Last Name</label>
                <input required type="text" value={formData.lastName} onChange={e => updateField('lastName', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 px-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Mail className="h-5 w-5 text-gray-500" />
                </div>
                <input required type="email" value={formData.email} onChange={e => updateField('email', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
              </div>
            </div>
          </div>
        )}

        {step === 2 && (
          <div className="space-y-4 animate-in fade-in slide-in-from-right-4 duration-300">
            <h2 className="text-xl font-bold mb-4">Identity</h2>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Phone Number</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Phone className="h-5 w-5 text-gray-500" />
                </div>
                <input required type="tel" value={formData.phone} onChange={e => updateField('phone', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5 flex items-center justify-between">
                <span>BSN Number</span>
                <span className="text-xs text-gray-500" title="Burgerservicenummer for Dutch identification">What is this?</span>
              </label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <CreditCard className="h-5 w-5 text-gray-500" />
                </div>
                <input required type="text" placeholder="123456789" value={formData.bsn} onChange={e => updateField('bsn', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
              </div>
            </div>
          </div>
        )}

        {step === 3 && (
          <div className="space-y-4 animate-in fade-in slide-in-from-right-4 duration-300">
            <h2 className="text-xl font-bold mb-4">Security</h2>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1.5">Create Password</label>
              <div className="relative">
                <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <Lock className="h-5 w-5 text-gray-500" />
                </div>
                <input required type="password" value={formData.password} onChange={e => updateField('password', e.target.value)} className="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
              </div>
              {formData.password && (
                <div className="mt-2 h-1.5 w-full bg-white/10 rounded-full overflow-hidden">
                  <div className={`h-full ${formData.password.length > 8 ? 'bg-[#00D9A3] w-full' : formData.password.length > 4 ? 'bg-yellow-500 w-1/2' : 'bg-[#FF5E5B] w-1/4'}`} />
                </div>
              )}
            </div>
          </div>
        )}
        
        <div className="flex gap-3 pt-4">
          {step > 1 && (
            <button type="button" onClick={() => setStep(step - 1)} className="px-6 py-3 rounded-xl border border-white/10 text-white font-medium hover:bg-white/5 transition-colors">
              Back
            </button>
          )}
          <button type="submit" className="flex-1 bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2">
            {step === 3 ? 'Complete Registration' : 'Continue'}
            {step !== 3 && <ChevronRight className="w-4 h-4" />}
          </button>
        </div>
        
        <div className="text-center pt-2">
          <Link to="/login" className="text-sm text-gray-400 hover:text-white transition-colors">
            Already have an account? Sign in
          </Link>
        </div>
      </form>
    </div>
  );
}