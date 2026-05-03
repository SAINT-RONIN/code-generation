<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../services/auth'
import { Eye, EyeOff, Check } from 'lucide-vue-next'
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
  if (p.length > 10 && /[A-Z]/.test(p) && /[0-9]/.test(p)) return { level: 3, label: 'Strong', color: '#00D9A3' }
  if (p.length > 6) return { level: 2, label: 'Fair', color: '#F59E0B' }
  return { level: 1, label: 'Weak', color: '#FF5E5B' }
})

const steps = [{ num: 1, label: 'Personal' }, { num: 2, label: 'Identity' }, { num: 3, label: 'Security' }]

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

const inputClass = "w-full px-4 py-3 text-sm text-white rounded-xl transition-colors"
const inputStyle = "background:#141420; border:1px solid rgba(255,255,255,0.08); outline:none;"

function onFocus(e) { e.target.style.borderColor = 'rgba(123,97,255,0.5)' }
function onBlur(e)  { e.target.style.borderColor = 'rgba(255,255,255,0.08)' }
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
      <div class="text-center mb-8">
        <div
          class="inline-flex items-center justify-center w-12 h-12 rounded-2xl mb-5"
          style="background:linear-gradient(135deg,#7B61FF,#5C45CC);"
          aria-hidden="true"
        >
          <span class="text-white font-bold text-lg">N</span>
        </div>
        <h1 class="text-2xl font-bold text-white tracking-tight">Create your account</h1>
        <p class="text-sm mt-1" style="color:#6B6B7E;">Takes less than 2 minutes</p>
      </div>

      <!-- Step indicator -->
      <div class="flex items-center gap-2 mb-8" role="progressbar" :aria-valuenow="step" aria-valuemin="1" aria-valuemax="3" :aria-label="`Step ${step} of 3: ${steps[step-1].label}`">
        <template v-for="(s, i) in steps" :key="s.num">
          <div class="flex items-center gap-2 flex-1">
            <div
              class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0 transition-all"
              :style="step > s.num
                ? 'background:#7B61FF; color:#fff;'
                : step === s.num
                  ? 'background:rgba(123,97,255,0.15); color:#7B61FF; border:1.5px solid rgba(123,97,255,0.4);'
                  : 'background:rgba(255,255,255,0.05); color:#4A4A5E;'"
              :aria-label="`Step ${s.num}: ${s.label}${step > s.num ? ' (completed)' : step === s.num ? ' (current)' : ''}`"
            >
              <Check v-if="step > s.num" class="w-3.5 h-3.5" aria-hidden="true" />
              <span v-else aria-hidden="true">{{ s.num }}</span>
            </div>
            <span class="text-xs font-medium hidden sm:block" :style="step === s.num ? 'color:#fff' : 'color:#4A4A5E'">{{ s.label }}</span>
          </div>
          <div v-if="i < steps.length - 1" class="h-px flex-1 transition-all" :style="step > s.num ? 'background:rgba(123,97,255,0.4)' : 'background:rgba(255,255,255,0.07)'"></div>
        </template>
      </div>

      <!-- Card -->
      <div class="rounded-2xl p-6" style="background:#0E0E16; border:1px solid rgba(255,255,255,0.07);">

        <!-- Error -->
        <div v-if="error" role="alert" class="flex items-center gap-2.5 text-sm px-4 py-3 rounded-xl mb-5" style="background:rgba(255,94,91,0.1); border:1px solid rgba(255,94,91,0.2); color:#FF5E5B;">
          <div class="w-1.5 h-1.5 rounded-full flex-shrink-0" style="background:#FF5E5B;" aria-hidden="true"></div>
          {{ error }}
        </div>

        <form @submit.prevent="handleNext" novalidate>

          <!-- Step 1: Personal -->
          <fieldset v-if="step === 1" class="space-y-4 border-none p-0 m-0">
            <legend class="text-base font-semibold text-white mb-4">Personal details</legend>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label for="reg-first" class="block text-xs font-medium mb-1.5" style="color:#8B8B9E;">First name</label>
                <input id="reg-first" v-model="form.firstName" required type="text" placeholder="John" :class="inputClass" :style="inputStyle" @focus="onFocus" @blur="onBlur" autocomplete="given-name" />
              </div>
              <div>
                <label for="reg-last" class="block text-xs font-medium mb-1.5" style="color:#8B8B9E;">Last name</label>
                <input id="reg-last" v-model="form.lastName" required type="text" placeholder="Doe" :class="inputClass" :style="inputStyle" @focus="onFocus" @blur="onBlur" autocomplete="family-name" />
              </div>
            </div>
            <div>
              <label for="reg-email" class="block text-xs font-medium mb-1.5" style="color:#8B8B9E;">Email address</label>
              <input id="reg-email" v-model="form.email" required type="email" placeholder="you@example.com" :class="inputClass" :style="inputStyle" @focus="onFocus" @blur="onBlur" autocomplete="email" />
            </div>
          </fieldset>

          <!-- Step 2: Identity -->
          <fieldset v-if="step === 2" class="space-y-4 border-none p-0 m-0">
            <legend class="text-base font-semibold text-white mb-4">Identity verification</legend>
            <div>
              <label for="reg-phone" class="block text-xs font-medium mb-1.5" style="color:#8B8B9E;">Phone number</label>
              <input id="reg-phone" v-model="form.phoneNumber" required type="tel" placeholder="+31 6 1234 5678" :class="inputClass" :style="inputStyle" @focus="onFocus" @blur="onBlur" autocomplete="tel" />
            </div>
            <div>
              <div class="flex justify-between items-center mb-1.5">
                <label for="reg-bsn" class="text-xs font-medium" style="color:#8B8B9E;">BSN number</label>
                <span class="text-[10px]" style="color:#4A4A5E;">Dutch citizen ID</span>
              </div>
              <input id="reg-bsn" v-model="form.bsn" required type="text" placeholder="123 456 789" :class="inputClass" :style="inputStyle" @focus="onFocus" @blur="onBlur" />
            </div>
          </fieldset>

          <!-- Step 3: Security -->
          <fieldset v-if="step === 3" class="space-y-4 border-none p-0 m-0">
            <legend class="text-base font-semibold text-white mb-4">Set your password</legend>
            <div>
              <label for="reg-password" class="block text-xs font-medium mb-1.5" style="color:#8B8B9E;">Password</label>
              <div class="relative">
                <input
                  id="reg-password"
                  v-model="form.password"
                  required
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="Min. 8 characters"
                  :class="inputClass"
                  class="pr-11"
                  :style="inputStyle"
                  @focus="onFocus"
                  @blur="onBlur"
                  autocomplete="new-password"
                  :aria-describedby="form.password ? 'password-strength' : undefined"
                />
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded"
                  style="color:#4A4A5E;"
                  :aria-label="showPassword ? 'Hide password' : 'Show password'"
                  :aria-pressed="showPassword"
                >
                  <EyeOff v-if="showPassword" class="w-4 h-4" aria-hidden="true" />
                  <Eye v-else class="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
              <div v-if="form.password" id="password-strength" class="mt-2.5 space-y-1.5" aria-live="polite">
                <div class="flex gap-1" aria-hidden="true">
                  <div v-for="i in 3" :key="i" class="h-1 flex-1 rounded-full transition-all" :style="passwordStrength.level >= i ? `background:${passwordStrength.color}` : 'background:rgba(255,255,255,0.07)'"></div>
                </div>
                <p class="text-xs font-medium" :style="`color:${passwordStrength.color}`">
                  {{ passwordStrength.label }} password
                </p>
              </div>
            </div>
          </fieldset>

          <!-- Actions -->
          <div class="flex gap-3 mt-6">
            <button
              v-if="step > 1"
              type="button"
              @click="step--"
              class="px-5 py-2.5 rounded-xl text-sm font-medium transition-colors"
              style="background:rgba(255,255,255,0.05); color:#8B8B9E; border:1px solid rgba(255,255,255,0.07);"
            >
              Back
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 py-2.5 rounded-xl text-sm font-semibold text-white transition-opacity disabled:opacity-60 flex items-center justify-center gap-2"
              style="background:linear-gradient(135deg,#7B61FF,#5C45CC);"
              :aria-busy="loading"
            >
              <svg v-if="loading" class="w-4 h-4 spin" viewBox="0 0 24 24" fill="none" aria-hidden="true">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
              </svg>
              {{ step === 3 ? (loading ? 'Creating account…' : 'Create account') : 'Continue' }}
            </button>
          </div>
        </form>
      </div>

      <!-- Login link -->
      <p class="text-center text-sm mt-5" style="color:#4A4A5E;">
        Already have an account?
        <RouterLink to="/login" class="font-medium ml-1" style="color:#7B61FF;">Sign in</RouterLink>
      </p>
    </div>
  </div>
</template>
