<script setup>
import { useRouter } from 'vue-router'
import { Clock, LogOut, HelpCircle } from 'lucide-vue-next'

const router = useRouter()

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">

    <!-- Background blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none" aria-hidden="true">
      <div class="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-[#7B61FF] rounded-full blur-[120px] opacity-20 animate-pulse"></div>
      <div class="absolute top-[60%] -right-[10%] w-[40%] h-[40%] bg-[#00D9A3] rounded-full blur-[100px] opacity-10"></div>
    </div>

    <div class="z-10 w-full max-w-md bg-[#14141A] rounded-2xl shadow-2xl border border-white/5 p-8">

      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] mb-4 shadow-lg shadow-[#7B61FF]/20" aria-hidden="true">
          <span class="text-white font-bold text-xl tracking-tighter">NL</span>
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-white">Nova Bank</h1>
        <p class="text-sm text-gray-400 mt-2">Premium banking, reimagined.</p>
      </div>

      <div class="text-center">
        <div class="w-24 h-24 bg-[#1C1C24] rounded-full flex items-center justify-center mx-auto mb-6 relative">
          <div class="absolute inset-0 border-2 border-[#7B61FF] border-dashed rounded-full opacity-50" style="animation: spin-slow 4s linear infinite" aria-hidden="true"></div>
          <Clock class="w-10 h-10 text-[#7B61FF]" aria-hidden="true" />
        </div>

        <h2 class="text-2xl font-bold text-white mb-3">We're reviewing your application</h2>
        <p class="text-gray-400 mb-8 leading-relaxed">
          Our team is verifying your details to ensure the safety of your account. This usually takes 1–2 business days.
        </p>

        <div class="space-y-3">
          <button
            class="w-full bg-[#1C1C24] hover:bg-[#252530] text-white rounded-xl py-3.5 font-medium transition-all flex items-center justify-center gap-2 border border-white/5"
            aria-label="Contact support"
          >
            <HelpCircle class="w-5 h-5" aria-hidden="true" />
            Contact Support
          </button>

          <button
            @click="logout"
            class="w-full text-gray-500 hover:text-white py-3 font-medium transition-colors flex items-center justify-center gap-2"
            aria-label="Sign out"
          >
            <LogOut class="w-4 h-4" aria-hidden="true" />
            Sign Out
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin-slow {
  to { transform: rotate(360deg); }
}
</style>
