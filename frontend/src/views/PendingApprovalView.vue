<script setup>
import { useRouter } from 'vue-router'
import { Clock, LogOut } from 'lucide-vue-next'

const router = useRouter()

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden z-0 pointer-events-none">
      <div class="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-[#7B61FF] rounded-full blur-[120px] opacity-20 animate-pulse"></div>
    </div>

    <div class="z-10 w-full max-w-md bg-[#14141A] rounded-2xl shadow-2xl border border-white/5 p-8">
      <div class="text-center">
        <div class="w-24 h-24 bg-[#1C1C24] rounded-full flex items-center justify-center mx-auto mb-6 relative">
          <div class="absolute inset-0 border-2 border-[#7B61FF] border-dashed rounded-full opacity-50" style="animation: spin 4s linear infinite"></div>
          <Clock class="w-10 h-10 text-[#7B61FF]" />
        </div>

        <h2 class="text-2xl font-bold text-white mb-3">We're reviewing your application</h2>
        <p class="text-gray-400 mb-8 leading-relaxed">
          Our team is verifying your details. This usually takes 1-2 business days.
        </p>

        <button
          @click="logout"
          class="w-full text-gray-500 hover:text-white py-3 font-medium transition-colors flex items-center justify-center gap-2"
        >
          <LogOut class="w-4 h-4" />
          Sign Out
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
