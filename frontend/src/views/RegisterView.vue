<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../services/auth'
import { Mail, Lock, Phone, CreditCard, ChevronRight, User, Eye, EyeOff, Check } from 'lucide-vue-next'
import { useBubbles } from '../composables/useBubbles'

const router = useRouter()
const { blobs } = useBubbles()
const step = ref(1)
const error = ref('')
const loading = ref(false)
const showPassword = ref(false)
const form = ref({ firstName: '', lastName: '', email: '', phoneNumber: '', bsn: '', password: '' })

const passwordStrength = computed(() => {
  const p = form.value.password
  if (!p) return { level: 0, label: '', color: '' }
  if (p.length > 10 && /[A-Z]/.test(p) && /[0-9]/.test(p)) return { level: 3, label: 'Strong', color: 'bg-[#00D9A3]' }
  if (p.length > 6) return { level: 2, label: 'Fair', color: 'bg-yellow-500' }
  return { level: 1, label: 'Weak', color: 'bg-[#FF5E5B]' }
})

const steps = [
  { num: 1, label: 'Personal' },
  { num: 2, label: 'Identity' },
  { num: 3, label: 'Security' },
]

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

    <div class="relative z-10 w-full max-w-md">
      <div class="text-center mb-8">
        <div class="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] mb-5 shadow-2xl shadow-[#7B61FF]/30">
          <span class="text-white font-bold text-2xl tracking-tighter">N</span>
        </div>
        <h1 class="text-3xl font-bold tracking-tight text-white">Nova Bank</h1>
        <p class="text-sm text-gray-500 mt-1.5">Create your account in minutes.</p>
      </div>

      <div class="bg-[#14141A]/80 backdrop-blur-xl rounded-3xl border border-white/[0.06] p-8 shadow-2xl">
        <!-- Step indicators -->
        <div class="flex items-center gap-2 mb-8">
          <template v-for="(s, i) in steps" :key="s.num">
            <div class="flex items-center gap-2 flex-1">
              <div
                class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold transition-all flex-shrink-0"
                :class="step > s.num ? 'bg-[#7B61FF] text-white' : step === s.num ? 'bg-[#7B61FF]/20 text-[#7B61FF] ring-2 ring-[#7B61FF]/40' : 'bg-white/5 text-gray-600'"
              >
                <Check v-if="step > s.num" class="w-3.5 h-3.5" />
                <span v-else>{{ s.num }}</span>
              </div>
              <span class="text-xs font-medium hidden sm:block" :class="step === s.num ? 'text-white' : 'text-gray-600'">{{ s.label }}</span>
            </div>
            <div v-if="i < steps.length - 1" class="h-px flex-1 transition-all" :class="step > s.num ? 'bg-[#7B61FF]/50' : 'bg-white/10'"></div>
          </template>
        </div>

        <form @submit.prevent="handleNext" class="space-y-5">
          <div v-if="error" class="flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3.5 rounded-xl">
            <div class="w-1.5 h-1.5 rounded-full bg-[#FF5E5B] flex-shrink-0"></div>
            {{ error }}
          </div>

          <!-- Step 1: Personal -->
          <div v-if="step === 1" class="space-y-4">
            <h2 class="text-lg font-semibold text-white">Personal Details</h2>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="block text-xs font-medium text-gray-400 mb-1.5">First Name</label>
                <div class="relative">
                  <User class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-600" />
                  <input v-model="form.firstName" required type="text" placeholder="John" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-9 pr-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
                </div>
              </div>
              <div>
                <label class="block text-xs font-medium text-gray-400 mb-1.5">Last Name</label>
                <input v-model="form.lastName" required type="text" placeholder="Doe" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 px-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-400 mb-1.5">Email Address</label>
              <div class="relative">
                <Mail class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-600" />
                <input v-model="form.email" required type="email" placeholder="you@example.com" autocomplete="email" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-9 pr-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
              </div>
            </div>
          </div>

          <!-- Step 2: Identity -->
          <div v-if="step === 2" class="space-y-4">
            <h2 class="text-lg font-semibold text-white">Identity Verification</h2>
            <div>
              <label class="block text-xs font-medium text-gray-400 mb-1.5">Phone Number</label>
              <div class="relative">
                <Phone class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-600" />
                <input v-model="form.phoneNumber" required type="tel" placeholder="+31 6 1234 5678" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-9 pr-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
              </div>
            </div>
            <div>
              <label class="block text-xs font-medium text-gray-400 mb-1.5 flex justify-between">
                <span>BSN Number</span>
                <span class="text-gray-600 font-normal">Dutch citizen ID</span>
              </label>
              <div class="relative">
                <CreditCard class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-600" />
                <input v-model="form.bsn" required type="text" placeholder="123 456 789" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-9 pr-3 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
              </div>
            </div>
          </div>

          <!-- Step 3: Security -->
          <div v-if="step === 3" class="space-y-4">
            <h2 class="text-lg font-semibold text-white">Set Your Password</h2>
            <div>
              <label class="block text-xs font-medium text-gray-400 mb-1.5">Password</label>
              <div class="relative">
                <Lock class="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-600" />
                <input v-model="form.password" required :type="showPassword ? 'text' : 'password'" placeholder="Min. 8 characters" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-3 pl-9 pr-10 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/40 transition-all placeholder:text-gray-700" />
                <button type="button" @click="showPassword = !showPassword" class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-600 hover:text-gray-400 transition-colors">
                  <EyeOff v-if="showPassword" class="w-4 h-4" /><Eye v-else class="w-4 h-4" />
                </button>
              </div>
              <div v-if="form.password" class="mt-2.5 space-y-1.5">
                <div class="flex gap-1.5">
                  <div v-for="i in 3" :key="i" class="h-1 flex-1 rounded-full transition-all" :class="passwordStrength.level >= i ? passwordStrength.color : 'bg-white/10'"></div>
                </div>
                <p class="text-xs" :class="passwordStrength.level === 3 ? 'text-[#00D9A3]' : passwordStrength.level === 2 ? 'text-yellow-500' : 'text-[#FF5E5B]'">{{ passwordStrength.label }}</p>
              </div>
            </div>
          </div>

          <div class="flex gap-3 pt-1">
            <button v-if="step > 1" type="button" @click="step--" class="px-5 py-3 rounded-xl border border-white/10 text-sm text-gray-400 font-medium hover:bg-white/5 hover:text-white transition-all">
              Back
            </button>
            <button type="submit" :disabled="loading" class="flex-1 bg-gradient-to-r from-[#7B61FF] to-[#6050D0] text-white rounded-xl py-3 text-sm font-semibold shadow-lg shadow-[#7B61FF]/20 hover:shadow-[#7B61FF]/35 disabled:opacity-60 transition-all flex items-center justify-center gap-2">
              <svg v-if="loading" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
              {{ step === 3 ? (loading ? 'Creating account...' : 'Create Account') : 'Continue' }}
              <ChevronRight v-if="step !== 3 && !loading" class="w-4 h-4" />
            </button>
          </div>
        </form>

        <div class="mt-5 text-center">
          <RouterLink to="/login" class="text-sm text-gray-500 hover:text-gray-300 transition-colors">
            Already have an account? <span class="text-[#7B61FF]">Sign in</span>
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>
