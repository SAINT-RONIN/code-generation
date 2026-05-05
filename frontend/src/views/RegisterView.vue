<script setup>
import { ref, computed } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { register } from '../services/auth'
import { Eye, EyeOff, ArrowLeft } from 'lucide-vue-next'
import IconLogo from '../components/icons/IconLogo.vue'
import VField from '../components/ui/VField.vue'
import VTextInput from '../components/ui/VTextInput.vue'
import VBtn from '../components/ui/VBtn.vue'

const router = useRouter()
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)
const agreed = ref(false)

const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  bsn: '',
  phoneNumber: '',
  password: '',
  confirmPassword: '',

})

const errors = ref({})

const passwordStrength = computed(() => {
  const p = form.value.password
  if (!p) return 0
  if (p.length >= 12 && /[A-Z]/.test(p) && /[0-9]/.test(p)) return 4
  if (p.length >= 8 && /[0-9]/.test(p)) return 3
  if (p.length >= 8) return 2
  return 1
})

function validate() {
  const e = {}
  if (!form.value.firstName.trim()) e.firstName = 'Required'
  if (!form.value.lastName.trim()) e.lastName = 'Required'
  if (!form.value.email.includes('@')) e.email = 'Valid email required'
  if (!/^\d{9}$/.test(form.value.bsn)) e.bsn = 'BSN must be 9 digits'
  if (!form.value.phoneNumber.trim()) e.phoneNumber = 'Required'
  if (form.value.password.length < 8) e.password = 'Minimum 8 characters'
  if (form.value.password !== form.value.confirmPassword) e.confirmPassword = 'Passwords do not match'
  if (!agreed.value) e.agreed = 'You must accept the terms'
  errors.value = e
  return Object.keys(e).length === 0
}

async function handleRegister() {
  if (!validate()) return
  error.value = ''
  loading.value = true
  try {
    await register({
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      email: form.value.email,
      bsn: form.value.bsn,
      phoneNumber: form.value.phoneNumber,
      password: form.value.password,
    })
    router.push('/pending-approval')
  } catch (e) {
    error.value = e.response?.data?.error || 'Registration failed. Please try again.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-6" :style="{ background: 'var(--bg)' }">
    <div
      class="w-full max-w-md rounded-2xl border p-8"
      :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
    >
      <!-- Back + Logo -->
      <div class="flex items-center justify-between mb-8">
        <div class="flex items-center gap-2.5">
          <IconLogo class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
          <span class="font-display text-[19px]" :style="{ color: 'var(--ink)' }">Impreza Bank</span>
        </div>
        <RouterLink
          to="/login"
          class="inline-flex items-center gap-2 text-sm font-medium no-underline px-3 py-2 rounded-lg"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)', border: '1px solid var(--line-2)' }"
        >
          <ArrowLeft class="w-4 h-4" /> Back
        </RouterLink>
      </div>

      <h1 class="font-display mb-1" style="font-size: 36px; line-height: 1.05; font-weight: 400;" :style="{ color: 'var(--ink)' }">
        Let's get you set up.
      </h1>
      <p class="text-sm mb-6" :style="{ color: 'var(--ink-3)' }">Open your Impreza Bank account in minutes.</p>

      <!-- Global Error -->
      <div
        v-if="error"
        class="mb-4 px-4 py-3 rounded-xl text-sm"
        :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
      >
        {{ error }}
      </div>

      <form @submit.prevent="handleRegister" class="space-y-4">
        <div class="grid grid-cols-2 gap-3">
          <VField label="First name" id="reg-first" :error="errors.firstName">
            <VTextInput id="reg-first" v-model="form.firstName" placeholder="Leandro" :error="!!errors.firstName" />
          </VField>
          <VField label="Last name" id="reg-last" :error="errors.lastName">
            <VTextInput id="reg-last" v-model="form.lastName" placeholder="Soares" :error="!!errors.lastName" />
          </VField>
        </div>

        <VField label="Email" id="reg-email" :error="errors.email">
          <VTextInput id="reg-email" v-model="form.email" type="email" placeholder="you@example.com" :error="!!errors.email" />
        </VField>

        <VField label="BSN" id="reg-bsn" :error="errors.bsn" hint="9-digit Dutch citizen number">
          <VTextInput id="reg-bsn" v-model="form.bsn" placeholder="123456789" :error="!!errors.bsn" />
        </VField>

        <VField label="Phone" id="reg-phone" :error="errors.phoneNumber">
          <VTextInput id="reg-phone" v-model="form.phoneNumber" type="tel" placeholder="+31 6 1234 5678" :error="!!errors.phoneNumber" />
        </VField>

        <VField label="Password" id="reg-password" :error="errors.password">
          <div class="relative">
            <VTextInput
              id="reg-password"
              v-model="form.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="Min. 8 characters"
              :error="!!errors.password"
            />
            <button
              type="button"
              class="absolute right-3 top-1/2 -translate-y-1/2"
              :style="{ color: 'var(--ink-3)' }"
              @click="showPassword = !showPassword"
            >
              <EyeOff v-if="showPassword" class="w-4 h-4" />
              <Eye v-else class="w-4 h-4" />
            </button>
          </div>
          <!-- Strength bars -->
          <div v-if="form.password" class="flex gap-1 mt-2">
            <div
              v-for="i in 4"
              :key="i"
              class="h-1 flex-1 rounded-full transition-all"
              :style="{ background: i <= passwordStrength ? 'var(--accent)' : 'var(--line-2)' }"
            />
          </div>
        </VField>

        <VField label="Confirm password" id="reg-confirm" :error="errors.confirmPassword">
          <VTextInput
            id="reg-confirm"
            v-model="form.confirmPassword"
            type="password"
            placeholder="Repeat password"
            :error="!!errors.confirmPassword"
          />
        </VField>

        <!-- Terms -->
        <div>
          <label class="flex items-start gap-3 cursor-pointer">
            <input
              type="checkbox"
              v-model="agreed"
              class="mt-0.5 w-4 h-4 rounded flex-shrink-0"
              :style="{ accentColor: 'var(--accent)' }"
            />
            <span class="text-sm" :style="{ color: 'var(--ink-2)' }">
              I agree to the <span class="font-medium" :style="{ color: 'var(--accent)' }">Terms of Service</span> and <span class="font-medium" :style="{ color: 'var(--accent)' }">Privacy Policy</span>
            </span>
          </label>
          <p v-if="errors.agreed" class="text-xs mt-1" :style="{ color: 'var(--debit)' }">{{ errors.agreed }}</p>
        </div>

        <VBtn type="submit" variant="primary" size="lg" class="w-full mt-2" :disabled="loading">
          {{ loading ? 'Creating account…' : 'Create account' }}
        </VBtn>
      </form>

      <p class="text-sm mt-6 text-center" :style="{ color: 'var(--ink-3)' }">
        Already have an account?
        <RouterLink to="/login" class="font-medium" :style="{ color: 'var(--accent)' }">Sign in</RouterLink>
      </p>
    </div>
  </div>
</template>
