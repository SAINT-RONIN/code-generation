import { ReactNode } from 'react';

export default function AuthLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">
      {/* Subtle animated background gradient */}
      <div className="absolute top-0 left-0 w-full h-full overflow-hidden z-0 pointer-events-none">
        <div className="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-[#7B61FF] rounded-full blur-[120px] opacity-20 animate-pulse"></div>
        <div className="absolute top-[60%] -right-[10%] w-[40%] h-[40%] bg-[#00D9A3] rounded-full blur-[100px] opacity-10"></div>
      </div>
      
      <div className="z-10 w-full max-w-md bg-[#14141A] rounded-2xl shadow-2xl border border-white/5 p-8">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] mb-4 shadow-lg shadow-[#7B61FF]/20">
            <span className="text-white font-bold text-xl tracking-tighter">NL</span>
          </div>
          <h1 className="text-2xl font-bold tracking-tight text-white">Nova Bank</h1>
          <p className="text-sm text-gray-400 mt-2">Premium banking, reimagined.</p>
        </div>
        
        {children}
      </div>
    </div>
  );
}