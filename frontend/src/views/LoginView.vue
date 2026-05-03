<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '../services/auth'
import { Eye, EyeOff } from 'lucide-vue-next'
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
    error.value = 'Incorrect email or password.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-4 relative overflow-hidden" style="background:#08080D;">

    <!-- Blobs -->
    <div class="absolute inset-0 overflow-hidden pointer-events-none" aria-hidden="true">
      <div
        v-for="(blob, i) in blobs" :key="i"
        class="absolute rounded-full"
        :style="{
          width: blob.size + 'px', height: blob.size + 'px',
          background: blob.color, opacity: 0.07,
          filter: `blur(${blob.size * 0.22}px)`,
          transform: `translate(${blob.x}px, ${blob.y}px)`,
          willChange: 'transform',
        }"
      />
    </div>

    <div class="relative z-10 w-full max-w-sm fade-up">

      <!-- Logo -->
      <div class="text-center mb-10">
        <div
          class="inline-flex items-center justify-center w-12 h-12 rounded-2xl mb-6"
          style="background:linear-gradient(135deg,#7B61FF,#5C45CC);"
          aria-hidden="true"
        >
          <span class="text-white font-bold text-lg">N</span>
        </div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Welcome back</h1>
        <p class="text-sm mt-1" style="color:#6B6B7E;">Sign in to Nova Bank</p>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleLogin" novalidate aria-label="Sign in form">

        <!-- Error -->
        <div
          v-if="error"
          role="alert"
          class="flex items-center gap-2.5 text-sm px-4 py-3 rounded-xl mb-5"
          style="background:rgba(255,94,91,0.1); border:1px solid rgba(255,94,91,0.2); color:#FF5E5B;"
        >
          <div class="w-1.5 h-1.5 rounded-full bg-[#FF5E5B] flex-shrink-0" aria-hidden="true"></div>
          {{ error }}
        </div>

        <!-- Email -->
        <div class="mb-4">
          <label for="login-email" class="block text-sm font-medium mb-2" style="color:#C0C0CE;">Email address</label>
          <input
            id="login-email"
            v-model="email"
            type="email"
            required
            autocomplete="email"
            placeholder="you@example.com"
            class="w-full px-4 py-3 text-sm text-white rounded-xl transition-colors"
            style="background:#141420; border:1px solid rgba(255,255,255,0.08); outline:none;"
            :style="{ boxShadow: 'none' }"
            @focus="$event.target.style.borderColor='rgba(123,97,255,0.5)'"
            @blur="$event.target.style.borderColor='rgba(255,255,255,0.08)'"
          />
        </div>

        <!-- Password -->
        <div class="mb-6">
          <label for="login-password" class="block text-sm font-medium mb-2" style="color:#C0C0CE;">Password</label>
          <div class="relative">
            <input
              id="login-password"
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              required
              autocomplete="current-password"
              placeholder="••••••••"
              class="w-full px-4 py-3 pr-11 text-sm text-white rounded-xl transition-colors"
              style="background:#141420; border:1px solid rgba(255,255,255,0.08); outline:none;"
              @focus="$event.target.style.borderColor='rgba(123,97,255,0.5)'"
              @blur="$event.target.style.borderColor='rgba(255,255,255,0.08)'"
            />
            <button
              type="button"
              @click="showPassword = !showPassword"
              class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded transition-colors"
              style="color:#4A4A5E;"
              :aria-label="showPassword ? 'Hide password' : 'Show password'"
              :aria-pressed="showPassword"
            >
              <EyeOff v-if="showPassword" class="w-4 h-4" aria-hidden="true" />
              <Eye v-else class="w-4 h-4" aria-hidden="true" />
            </button>
          </div>
        </div>

        <!-- Submit -->
        <button
          type="submit"
          :disabled="loading"
          class="w-full py-3 rounded-xl text-sm font-semibold text-white transition-opacity disabled:opacity-60"
          style="background:linear-gradient(135deg,#7B61FF,#5C45CC);"
          :aria-busy="loading"
        >
          <span class="flex items-center justify-center gap-2">
            <svg v-if="loading" class="w-4 h-4 spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </span>
        </button>
      </form>

      <!-- Register link -->
      <p class="text-center text-sm mt-6" style="color:#4A4A5E;">
        New to Nova Bank?
        <RouterLink to="/register" class="font-medium ml-1 transition-colors" style="color:#7B61FF;" @mouseenter="$event.target.style.color='#9B81FF'" @mouseleave="$event.target.style.color='#7B61FF'">
          Create account
        </RouterLink>
      </p>
    </div>
  </div>
</template>
