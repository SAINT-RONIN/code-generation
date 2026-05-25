<script setup>
import { ref } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import AtmShell from '../../components/AtmShell.vue'
import { login } from '../../services/auth'
import { ArrowLeft } from 'lucide-vue-next'
import { setAuth, ROLES } from '../../composables/useAuth'
import { extractError } from '../../utils/error'

const router = useRouter()
const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleLogin() {
  if (!email.value || !password.value) return
  error.value = ''
  loading.value = true
  try {
    const { data } = await login(email.value, password.value)
    if (data.role !== ROLES.CUSTOMER) {
      error.value = 'ATM access is for customers only.'
      return
    }
    setAuth(data.token, data.role)
    router.push('/atm/menu')
  } catch (e) {
    error.value = extractError(e, 'Incorrect email or password.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmShell>
    <div class="flex flex-col items-center justify-center h-full px-10 py-8">
      <div class="w-full max-w-xs">
        <RouterLink
          to="/login"
          class="inline-flex items-center gap-2 text-sm font-medium mb-8 no-underline px-3 py-2 rounded-lg"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)', border: '1px solid var(--line-2)' }"
        >
          <ArrowLeft class="w-4 h-4" /> Back to main login
        </RouterLink>

        <h1 class="font-display text-4xl mb-1" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
          Welcome.
        </h1>
        <p class="text-sm mb-8" :style="{ color: 'var(--ink-3)' }">
          Sign in to use this ATM.
        </p>

        <div
          v-if="error"
          class="mb-5 px-4 py-3 rounded-xl text-sm"
          :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
        >{{ error }}</div>

        <div class="mb-4">
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-3)' }">Email</label>
          <input
            v-model="email"
            type="email"
            placeholder="you@example.com"
            autocomplete="email"
            class="w-full h-11 px-3 text-sm rounded-xl border"
            :style="{ background: 'var(--surface-2)', color: 'var(--ink)', borderColor: 'var(--line-2)' }"
            @keyup.enter="handleLogin"
          />
        </div>

        <div class="mb-6">
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-3)' }">Password</label>
          <input
            v-model="password"
            type="password"
            placeholder="••••••••"
            autocomplete="current-password"
            class="w-full h-11 px-3 text-sm rounded-xl border"
            :style="{ background: 'var(--surface-2)', color: 'var(--ink)', borderColor: 'var(--line-2)' }"
            @keyup.enter="handleLogin"
          />
        </div>

        <button
          class="w-full h-12 rounded-xl text-sm font-semibold lift"
          :style="{ background: 'var(--accent)', color: 'var(--accent-ink)', opacity: loading ? 0.7 : 1 }"
          :disabled="loading"
          @click="handleLogin"
        >
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </div>
    </div>
  </AtmShell>
</template>
