import { useNavigate } from 'react-router';
import { useBankStore } from '../../../store';
import { Clock, LogOut, HelpCircle } from 'lucide-react';

export default function PendingApproval() {
  const { logout } = useBankStore();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="text-center animate-in fade-in zoom-in-95 duration-500">
      <div className="w-24 h-24 bg-[#1C1C24] rounded-full flex items-center justify-center mx-auto mb-6 relative">
        <div className="absolute inset-0 border-2 border-[#7B61FF] border-dashed rounded-full animate-[spin_4s_linear_infinite] opacity-50" />
        <Clock className="w-10 h-10 text-[#7B61FF]" />
      </div>
      
      <h2 className="text-2xl font-bold mb-3">We're reviewing your application</h2>
      <p className="text-gray-400 mb-8 leading-relaxed">
        Our team is verifying your details to ensure the safety of your account. This usually takes 1-2 business days.
      </p>
      
      <div className="space-y-3">
        <button className="w-full bg-[#1C1C24] hover:bg-[#252530] text-white rounded-xl py-3.5 font-medium transition-all flex items-center justify-center gap-2 border border-white/5">
          <HelpCircle className="w-5 h-5" />
          Contact Support
        </button>
        
        <button 
          onClick={handleLogout}
          className="w-full text-gray-500 hover:text-white py-3 font-medium transition-colors flex items-center justify-center gap-2"
        >
          <LogOut className="w-4 h-4" />
          Sign Out
        </button>
      </div>
    </div>
  );
}