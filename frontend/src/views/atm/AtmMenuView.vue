<script setup>
import { useRouter } from 'vue-router'
import { ArrowUpCircle, ArrowDownCircle, LogOut, Cpu } from 'lucide-vue-next'

const router = useRouter()

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0F] text-white flex flex-col items-center justify-center relative overflow-hidden">
    <!-- Background blobs -->
    <div class="absolute top-0 left-1/4 w-96 h-96 bg-[#7B61FF]/5 rounded-full blur-3xl pointer-events-none"></div>
    <div class="absolute bottom-0 right-1/4 w-96 h-96 bg-[#00D9A3]/5 rounded-full blur-3xl pointer-events-none"></div>

    <!-- Top bar -->
    <div class="absolute top-0 left-0 right-0 flex items-center justify-between px-8 py-5 border-b border-white/[0.04]">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center">
          <Cpu class="w-4.5 h-4.5 text-white" />
        </div>
        <div>
          <p class="text-sm font-bold text-white tracking-tight">Nova ATM</p>
          <p class="text-[10px] text-gray-600">Terminal #402</p>
        </div>
      </div>
      <button
        @click="logout"
        class="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/[0.04] hover:bg-[#FF5E5B]/10 text-gray-500 hover:text-[#FF5E5B] border border-white/[0.06] hover:border-[#FF5E5B]/20 text-xs font-semibold transition-all"
      >
        <LogOut class="w-3.5 h-3.5" />
        End Session
      </button>
    </div>

    <!-- Main content -->
    <div class="w-full max-w-lg px-6">
      <div class="text-center mb-10">
        <p class="text-xs font-semibold text-gray-600 uppercase tracking-widest mb-2">Welcome</p>
        <h1 class="text-3xl font-bold text-white">What would you like to do?</h1>
      </div>

      <div class="grid grid-cols-2 gap-4">
        <!-- Withdraw -->
        <RouterLink
          to="/atm/withdraw"
          class="group relative bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-8 flex flex-col items-center justify-center gap-4 hover:border-[#7B61FF]/30 hover:bg-[#7B61FF]/5 transition-all overflow-hidden"
        >
          <div class="absolute inset-0 bg-gradient-to-b from-[#7B61FF]/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
          <div class="w-16 h-16 rounded-2xl bg-[#7B61FF]/10 group-hover:bg-[#7B61FF]/20 flex items-center justify-center transition-colors relative">
            <ArrowUpCircle class="w-8 h-8 text-[#7B61FF]" />
          </div>
          <div class="text-center relative">
            <p class="text-lg font-bold text-white">Withdraw</p>
            <p class="text-xs text-gray-600 mt-0.5">Get cash</p>
          </div>
        </RouterLink>

        <!-- Deposit -->
        <RouterLink
          to="/atm/deposit"
          class="group relative bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-8 flex flex-col items-center justify-center gap-4 hover:border-[#00D9A3]/30 hover:bg-[#00D9A3]/5 transition-all overflow-hidden"
        >
          <div class="absolute inset-0 bg-gradient-to-b from-[#00D9A3]/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity"></div>
          <div class="w-16 h-16 rounded-2xl bg-[#00D9A3]/10 group-hover:bg-[#00D9A3]/20 flex items-center justify-center transition-colors relative">
            <ArrowDownCircle class="w-8 h-8 text-[#00D9A3]" />
          </div>
          <div class="text-center relative">
            <p class="text-lg font-bold text-white">Deposit</p>
            <p class="text-xs text-gray-600 mt-0.5">Add cash</p>
          </div>
        </RouterLink>
      </div>

      <!-- Finish -->
      <button
        @click="logout"
        class="w-full mt-4 flex items-center justify-center gap-2 py-3.5 rounded-2xl bg-[#0D0D14] border border-white/[0.05] text-gray-600 hover:text-[#FF5E5B] hover:border-[#FF5E5B]/20 text-sm font-semibold transition-all"
      >
        <LogOut class="w-4 h-4" />
        Return Card &amp; Finish
      </button>
    </div>
  </div>
</template>
