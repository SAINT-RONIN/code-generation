<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/auth'
import { Mail, Lock, LogIn, Eye, EyeOff } from 'lucide-vue-next'
import { useBubbles } from '../composables/useBubbles'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)
const { blobs } = useBubbles()

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
    error.value = 'Invalid email or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">
    <!-- Bouncing background blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none">
      <div
        v-for="(blob, i) in blobs"
        :key="i"
        class="absolute rounded-full"
        :style="{
          width: blob.size + 'px',
          height: blob.size + 'px',
          background: blob.color,
          opacity: 0.13,
          filter: `blur(${blob.size * 0.2}px)`,
          transform: `translate(${blob.x}px, ${blob.y}px)`,
          willChange: 'transform',
        }"
      ></div>
    </div>

    <div class="relative z-10 w-full max-w-md animate-fade-up">
      <!-- Logo -->
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-16 h-16 rounded-2xl mb-5 animate-pulse-glow" style="background: linear-gradient(135deg, #7B61FF, #5C45CC);">
          <span class="text-white font-bold text-2xl tracking-tighter">N</span>
        </div>
        <h1 class="text-3xl font-bold tracking-tight gradient-text">Nova Bank</h1>
        <p class="text-sm text-gray-500 mt-1.5">Premium banking, reimagined.</p>
      </div>

      <!-- Card -->
      <div class="gradient-border backdrop-blur-xl p-8 shadow-2xl" style="background: rgba(14,14,23,0.85); box-shadow: 0 25px 80px rgba(0,0,0,0.5), 0 0 0 1px rgba(255,255,255,0.05);">
        <h2 class="text-xl font-semibold text-white mb-6">Sign in to your account</h2>

        <form @submit.prevent="handleLogin" class="space-y-5">
          <!-- Error -->
          <div v-if="error" class="flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3.5 rounded-xl">
            <div class="w-1.5 h-1.5 rounded-full bg-[#FF5E5B] flex-shrink-0"></div>
            {{ error }}
          </div>

          <!-- Email -->
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-2">Email address</label>
            <div class="relative">
              <Mail class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4.5 w-4.5 text-gray-600" />
              <input
                v-model="email"
                type="email"
                required
                autocomplete="email"
                placeholder="you@example.com"
                class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-11 pr-4 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 focus:border-[#7B61FF]/50 transition-all placeholder:text-gray-700"
              />
            </div>
          </div>

          <!-- Password -->
          <div>
            <label class="block text-sm font-medium text-gray-400 mb-2">Password</label>
            <div class="relative">
              <Lock class="absolute left-3.5 top-1/2 -translate-y-1/2 h-4.5 w-4.5 text-gray-600" />
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                required
                autocomplete="current-password"
                placeholder="••••••••"
                class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-11 pr-11 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 focus:border-[#7B61FF]/50 transition-all placeholder:text-gray-700"
              />
              <button type="button" @click="showPassword = !showPassword" class="absolute right-3.5 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-400 transition-colors">
                <EyeOff v-if="showPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </div>

          <!-- Submit -->
          <button
            type="submit"
            :disabled="loading"
            class="w-full bg-gradient-to-r from-[#7B61FF] to-[#6050D0] text-white rounded-xl py-3 text-sm font-semibold shadow-lg shadow-[#7B61FF]/20 hover:shadow-[#7B61FF]/35 hover:from-[#8B71FF] hover:to-[#7060E0] disabled:opacity-60 disabled:cursor-not-allowed transition-all flex items-center justify-center gap-2"
          >
            <svg v-if="loading" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            <LogIn v-else class="w-4 h-4" />
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <div class="mt-6 pt-6 border-t border-white/[0.06] text-center">
          <p class="text-sm text-gray-500">
            New to Nova Bank?
            <RouterLink to="/register" class="text-[#7B61FF] hover:text-[#9B81FF] font-medium transition-colors ml-1">
              Open an account
            </RouterLink>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

