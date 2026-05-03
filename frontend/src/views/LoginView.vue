<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/auth'
import { Mail, Lock, LogIn, Eye, EyeOff } from 'lucide-vue-next'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)

async function handleLogin() {
  if (loading.value) return
  error.value = ''
  loading.value = true
  try {
    const { data } = await login(email.value, password.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    router.push(data.role === 'EMPLOYEE' ? '/employee' : '/customer')
  } catch {
    error.value = 'Incorrect email or password.'
  } finally {
    loading.value = false
  }
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

      <form @submit.prevent="handleLogin" novalidate aria-label="Sign in form" class="space-y-6">

        <!-- Error -->
        <div
          v-if="error"
          role="alert"
          class="bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3 rounded-lg"
        >
          {{ error }}
        </div>

        <div class="space-y-4">
          <!-- Email -->
          <div>
            <label for="login-email" class="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <Mail class="h-5 w-5 text-gray-500" />
              </div>
              <input
                id="login-email"
                v-model="email"
                type="email"
                required
                autocomplete="email"
                placeholder="Enter your email"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label for="login-password" class="block text-sm font-medium text-gray-300 mb-1.5">Password</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <Lock class="h-5 w-5 text-gray-500" />
              </div>
              <input
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                required
                autocomplete="current-password"
                placeholder="••••••••"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-11 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded text-gray-500 hover:text-gray-300 transition-colors"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                :aria-pressed="showPassword"
              >
                <EyeOff v-if="showPassword" class="w-4 h-4" aria-hidden="true" />
                <Eye v-else class="w-4 h-4" aria-hidden="true" />
              </button>
            </div>
          </div>
        </div>

        <!-- Submit -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2 disabled:opacity-60"
          :aria-busy="loading"
        >
          <svg v-if="loading" class="w-4 h-4 spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          <LogIn v-else class="w-5 h-5" aria-hidden="true" />
          {{ loading ? 'Signing in…' : 'Sign In' }}
        </button>

        <div class="pt-4 text-center border-t border-white/5">
          <p class="text-sm text-gray-400">
            New to Nova Bank?
            <RouterLink to="/register" class="text-[#7B61FF] hover:text-[#907aff] font-medium transition-colors ml-1">
              Open an account
            </RouterLink>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>
