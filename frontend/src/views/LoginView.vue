<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/auth'
import { Mail, Lock, LogIn } from 'lucide-vue-next'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')

async function handleLogin() {
  error.value = ''
  try {
    const { data } = await login(email.value, password.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    router.push(data.role === 'EMPLOYEE' ? '/employee' : '/customer')
  } catch {
    error.value = 'Invalid email or password.'
  }
}
</script>

<template>
  <div class="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden z-0 pointer-events-none">
      <div class="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-[#7B61FF] rounded-full blur-[120px] opacity-20 animate-pulse"></div>
      <div class="absolute top-[60%] -right-[10%] w-[40%] h-[40%] bg-[#00D9A3] rounded-full blur-[100px] opacity-10"></div>
    </div>

    <div class="z-10 w-full max-w-md bg-[#14141A] rounded-2xl shadow-2xl border border-white/5 p-8">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] mb-4 shadow-lg shadow-[#7B61FF]/20">
          <span class="text-white font-bold text-xl tracking-tighter">NL</span>
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-white">Nova Bank</h1>
        <p class="text-sm text-gray-400 mt-2">Premium banking, reimagined.</p>
      </div>

      <form @submit.prevent="handleLogin" class="space-y-6">
        <div v-if="error" class="bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3 rounded-lg">
          {{ error }}
        </div>

        <div class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail class="h-5 w-5 text-gray-500" />
              </div>
              <input
                v-model="email"
                type="email"
                required
                placeholder="Enter your email"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">Password</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock class="h-5 w-5 text-gray-500" />
              </div>
              <input
                v-model="password"
                type="password"
                required
                placeholder="••••••••"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              />
            </div>
          </div>
        </div>

        <button
          type="submit"
          class="w-full bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2"
        >
          <LogIn class="w-5 h-5" />
          Sign In
        </button>

        <div class="pt-4 text-center border-t border-white/5">
          <p class="text-sm text-gray-400">
            New to Nova Bank?
            <RouterLink to="/register" class="text-[#7B61FF] hover:text-[#907aff] font-medium transition-colors">
              Open an account
            </RouterLink>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>
