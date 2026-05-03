<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../services/auth'
import { User as UserIcon, Mail, Phone, CreditCard, Lock, ChevronRight, Eye, EyeOff } from 'lucide-vue-next'

const router = useRouter()
const step = ref(1)
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)
const form = ref({ firstName: '', lastName: '', email: '', phoneNumber: '', bsn: '', password: '' })

const passwordStrength = computed(() => {
  const p = form.value.password
  if (!p) return { width: '0%', color: '' }
  if (p.length > 10 && /[A-Z]/.test(p) && /[0-9]/.test(p)) return { width: '100%', color: 'bg-[#00D9A3]' }
  if (p.length > 6) return { width: '50%', color: 'bg-yellow-500' }
  return { width: '25%', color: 'bg-[#FF5E5B]' }
})

async function handleNext() {
  error.value = ''
  if (step.value < 3) { step.value++; return }
  loading.value = true
  try {
    await register(form.value)
    router.push('/pending-approval')
  } catch (e) {
    error.value = e.response?.data?.error || 'Registration failed. Please try again.'
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

      <!-- Progress bars -->
      <div class="flex gap-2 mb-8" role="progressbar" :aria-valuenow="step" aria-valuemin="1" aria-valuemax="3" :aria-label="`Step ${step} of 3`">
        <div v-for="i in 3" :key="i" class="h-1.5 flex-1 rounded-full transition-all" :class="step >= i ? 'bg-[#7B61FF]' : 'bg-white/10'"></div>
      </div>

      <!-- Error -->
      <div v-if="error" role="alert" class="bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3 rounded-lg mb-5">
        {{ error }}
      </div>

      <form @submit.prevent="handleNext" novalidate class="space-y-6">

        <!-- Step 1: Personal Details -->
        <fieldset v-if="step === 1" class="space-y-4 border-none p-0 m-0">
          <legend class="text-xl font-bold text-white mb-4">Personal Details</legend>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label for="reg-first" class="block text-sm font-medium text-gray-300 mb-1.5">First Name</label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                  <UserIcon class="h-5 w-5 text-gray-500" />
                </div>
                <input id="reg-first" v-model="form.firstName" required type="text" placeholder="John" autocomplete="given-name"
                  class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600" />
              </div>
            </div>
            <div>
              <label for="reg-last" class="block text-sm font-medium text-gray-300 mb-1.5">Last Name</label>
              <input id="reg-last" v-model="form.lastName" required type="text" placeholder="Doe" autocomplete="family-name"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 px-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600" />
            </div>
          </div>
          <div>
            <label for="reg-email" class="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <Mail class="h-5 w-5 text-gray-500" />
              </div>
              <input id="reg-email" v-model="form.email" required type="email" placeholder="you@example.com" autocomplete="email"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600" />
            </div>
          </div>
        </fieldset>

        <!-- Step 2: Identity -->
        <fieldset v-if="step === 2" class="space-y-4 border-none p-0 m-0">
          <legend class="text-xl font-bold text-white mb-4">Identity</legend>
          <div>
            <label for="reg-phone" class="block text-sm font-medium text-gray-300 mb-1.5">Phone Number</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <Phone class="h-5 w-5 text-gray-500" />
              </div>
              <input id="reg-phone" v-model="form.phoneNumber" required type="tel" placeholder="+31 6 1234 5678" autocomplete="tel"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600" />
            </div>
          </div>
          <div>
            <label for="reg-bsn" class="block text-sm font-medium text-gray-300 mb-1.5 flex items-center justify-between">
              <span>BSN Number</span>
              <span class="text-xs text-gray-500" title="Burgerservicenummer for Dutch identification">What is this?</span>
            </label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <CreditCard class="h-5 w-5 text-gray-500" />
              </div>
              <input id="reg-bsn" v-model="form.bsn" required type="text" placeholder="123456789"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600" />
            </div>
          </div>
        </fieldset>

        <!-- Step 3: Security -->
        <fieldset v-if="step === 3" class="space-y-4 border-none p-0 m-0">
          <legend class="text-xl font-bold text-white mb-4">Security</legend>
          <div>
            <label for="reg-password" class="block text-sm font-medium text-gray-300 mb-1.5">Create Password</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none" aria-hidden="true">
                <Lock class="h-5 w-5 text-gray-500" />
              </div>
              <input
                id="reg-password"
                v-model="form.password"
                required
                :type="showPassword ? 'text' : 'password'"
                placeholder="Min. 8 characters"
                autocomplete="new-password"
                class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-11 text-white focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/50 transition-all placeholder:text-gray-600"
                :aria-describedby="form.password ? 'password-strength' : undefined"
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
            <div v-if="form.password" id="password-strength" class="mt-2 h-1.5 w-full bg-white/10 rounded-full overflow-hidden" aria-live="polite">
              <div class="h-full rounded-full transition-all" :class="passwordStrength.color" :style="{ width: passwordStrength.width }"></div>
            </div>
          </div>
        </fieldset>

        <!-- Actions -->
        <div class="flex gap-3 pt-4">
          <button
            v-if="step > 1"
            type="button"
            @click="step--"
            class="px-6 py-3 rounded-xl border border-white/10 text-white font-medium hover:bg-white/5 transition-colors"
          >
            Back
          </button>
          <button
            type="submit"
            :disabled="loading"
            class="flex-1 bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2 disabled:opacity-60"
            :aria-busy="loading"
          >
            <svg v-if="loading" class="w-4 h-4 spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
            {{ step === 3 ? (loading ? 'Creating account…' : 'Complete Registration') : 'Continue' }}
            <ChevronRight v-if="step !== 3 && !loading" class="w-4 h-4" aria-hidden="true" />
          </button>
        </div>

        <div class="text-center pt-2">
          <RouterLink to="/login" class="text-sm text-gray-400 hover:text-white transition-colors">
            Already have an account? Sign in
          </RouterLink>
        </div>
      </form>
    </div>
  </div>
</template>
