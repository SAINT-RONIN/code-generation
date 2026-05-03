<script setup>
import { useRouter } from 'vue-router'
import { ArrowUpCircle, ArrowDownCircle, LogOut } from 'lucide-vue-next'

const router = useRouter()

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen bg-black text-white p-8 font-sans flex flex-col items-center justify-center relative">
    <div class="absolute top-8 left-8 flex items-center gap-4">
      <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center border-4 border-white/20">
        <span class="text-white font-bold text-2xl tracking-tighter">NL</span>
      </div>
      <div class="text-left">
        <h1 class="text-3xl font-bold tracking-tight uppercase">Nova ATM</h1>
        <p class="text-gray-400 text-lg">Terminal #402</p>
      </div>
    </div>

    <button
      @click="logout"
      class="absolute top-8 right-8 flex items-center gap-2 bg-red-600/20 text-red-500 hover:bg-red-600 hover:text-white px-6 py-4 rounded-xl text-xl font-bold uppercase tracking-wider transition-colors border-2 border-red-600/50"
    >
      <LogOut class="w-6 h-6" />
      Finish
    </button>

    <div class="w-full max-w-4xl mt-24">
      <h2 class="text-5xl font-bold mb-12 text-center uppercase tracking-wider">Please select a service</h2>

      <div class="grid grid-cols-2 gap-8">
        <RouterLink
          to="/atm/withdraw"
          class="flex flex-col items-center justify-center p-12 rounded-3xl border-4 border-white/10 hover:border-white transition-all transform hover:scale-105 active:scale-95 shadow-2xl bg-blue-600"
        >
          <ArrowUpCircle class="w-24 h-24 mb-6 opacity-80" />
          <span class="text-4xl font-bold uppercase tracking-widest">Withdraw</span>
        </RouterLink>

        <RouterLink
          to="/atm/deposit"
          class="flex flex-col items-center justify-center p-12 rounded-3xl border-4 border-white/10 hover:border-white transition-all transform hover:scale-105 active:scale-95 shadow-2xl bg-green-600"
        >
          <ArrowDownCircle class="w-24 h-24 mb-6 opacity-80" />
          <span class="text-4xl font-bold uppercase tracking-widest">Deposit</span>
        </RouterLink>

        <button
          @click="logout"
          class="col-span-2 flex flex-col items-center justify-center p-8 rounded-3xl border-4 border-white/10 hover:border-red-500 bg-gray-900 transition-all transform hover:scale-105 active:scale-95 shadow-2xl group"
        >
          <LogOut class="w-16 h-16 mb-4 opacity-50 group-hover:text-red-500 group-hover:opacity-100 transition-all" />
          <span class="text-3xl font-bold uppercase tracking-widest text-gray-500 group-hover:text-red-500 transition-colors">Return Card</span>
        </button>
      </div>
    </div>
  </div>
</template>
