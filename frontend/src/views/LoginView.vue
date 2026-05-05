<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { login } from '../services/auth'
import { Eye, EyeOff, Shield, CreditCard } from 'lucide-vue-next'
import IconLogo from '../components/icons/IconLogo.vue'
import VField from '../components/ui/VField.vue'
import VTextInput from '../components/ui/VTextInput.vue'
import VBtn from '../components/ui/VBtn.vue'

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
  } catch (e) {
    const msg = e?.response?.data?.error
    error.value = msg || 'Incorrect email or password.'
  } finally {
    loading.value = false
  }
}

function fillDemo(role) {
  if (role === 'customer') {
    email.value = 'customer@test.com'
    password.value = 'customer123'
  } else {
    email.value = 'employee@bank.com'
    password.value = 'employee123'
  }
}
</script>

<template>
  <div class="min-h-screen flex" :style="{ background: 'var(--bg)' }">
    <!-- Left: Form -->
    <div class="flex-1 flex flex-col items-center justify-center px-8 py-12">
      <div class="w-full max-w-sm">
        <!-- Logo -->
        <div class="flex items-center gap-2.5 mb-10">
          <IconLogo class="w-8 h-8" :style="{ color: 'var(--accent)' }" />
          <span class="font-display text-[22px]" :style="{ color: 'var(--ink)' }">Impreza Bank</span>
        </div>

        <h1 class="font-display mb-2" style="font-size: 44px; line-height: 1.05; font-weight: 400;" :style="{ color: 'var(--ink)' }">
          Welcome back.
        </h1>
        <p class="text-sm mb-8" :style="{ color: 'var(--ink-3)' }">Sign in to your account.</p>

        <!-- Error -->
        <div
          v-if="error"
          class="mb-4 px-4 py-3 rounded-xl text-sm"
          :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
        >
          {{ error }}
        </div>

        <form @submit.prevent="handleLogin" class="space-y-4">
          <VField label="Email" id="login-email">
            <VTextInput
              id="login-email"
              v-model="email"
              type="email"
              placeholder="you@example.com"
              autocomplete="email"
            />
          </VField>

          <VField label="Password" id="login-password">
            <div class="relative">
              <VTextInput
                id="login-password"
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="••••••••"
                autocomplete="current-password"
              />
              <button
                type="button"
                class="absolute right-3 top-1/2 -translate-y-1/2"
                :style="{ color: 'var(--ink-3)' }"
                @click="showPassword = !showPassword"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
              >
                <EyeOff v-if="showPassword" class="w-4 h-4" />
                <Eye v-else class="w-4 h-4" />
              </button>
            </div>
          </VField>

          <VBtn type="submit" variant="primary" size="lg" class="w-full mt-2" :disabled="loading">
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </VBtn>
        </form>

        <!-- ATM button -->
        <RouterLink to="/atm/login" class="no-underline">
          <button
            type="button"
            class="w-full mt-3 h-11 rounded-xl text-sm font-medium flex items-center justify-center gap-2 lift border transition-colors"
            :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)', color: 'var(--ink-2)' }"
          >
            <CreditCard class="w-4 h-4" />
            Use ATM
          </button>
        </RouterLink>

        <!-- Demo shortcuts -->
        <div class="mt-6 pt-6 border-t" :style="{ borderColor: 'var(--line)' }">
          <p class="text-xs mb-2" :style="{ color: 'var(--ink-3)' }">Demo shortcuts:</p>
          <div class="flex gap-2">
            <button
              type="button"
              class="text-xs px-3 py-1.5 rounded-lg font-medium lift"
              :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)' }"
              @click="fillDemo('customer')"
            >Customer</button>
            <button
              type="button"
              class="text-xs px-3 py-1.5 rounded-lg font-medium lift"
              :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)' }"
              @click="fillDemo('employee')"
            >Employee</button>
          </div>
        </div>

        <p class="text-sm mt-6" :style="{ color: 'var(--ink-3)' }">
          No account?
          <RouterLink to="/register" class="font-medium" :style="{ color: 'var(--accent)' }">
            Open one here
          </RouterLink>
        </p>
      </div>
    </div>

    <!-- Right: Accent panel (hidden on mobile) -->
    <div
      class="hidden lg:flex w-[480px] flex-col items-center justify-center p-16"
      :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
    >
      <Shield class="w-16 h-16 mb-6 opacity-80" />
      <h2 class="font-display text-4xl mb-4 text-center" style="font-weight: 300; line-height: 1.1;">
        Money in good hands.
      </h2>
      <p class="text-center text-sm opacity-70 leading-relaxed max-w-xs">
        Impreza Bank keeps your finances secure, transparent and always within reach.
      </p>
    </div>
  </div>
</template>
