<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '../services/auth'
import { Mail, Lock, Phone, CreditCard, ChevronRight } from 'lucide-vue-next'

const router = useRouter()
const step = ref(1)
const error = ref('')
const form = ref({
  firstName: '',
  lastName: '',
  email: '',
  phoneNumber: '',
  bsn: '',
  password: '',
})

async function handleNext() {
  error.value = ''
  if (step.value < 3) {
    step.value++
    return
  }
  try {
    await register(form.value)
    router.push('/pending-approval')
  } catch (e) {
    error.value = e.response?.data?.error || 'Registration failed.'
  }
}
</script>

<template>
  <div class="min-h-screen w-full bg-[#0A0A0F] flex flex-col items-center justify-center p-4 relative overflow-hidden">
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden z-0 pointer-events-none">
      <div class="absolute -top-[20%] -left-[10%] w-[50%] h-[50%] bg-[#7B61FF] rounded-full blur-[120px] opacity-20 animate-pulse"></div>
    </div>

    <div class="z-10 w-full max-w-md bg-[#14141A] rounded-2xl shadow-2xl border border-white/5 p-8">
      <div class="text-center mb-6">
        <div class="inline-flex items-center justify-center w-12 h-12 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] mb-4 shadow-lg shadow-[#7B61FF]/20">
          <span class="text-white font-bold text-xl tracking-tighter">NL</span>
        </div>
        <h1 class="text-2xl font-bold tracking-tight text-white">Nova Bank</h1>
      </div>

      <div class="flex gap-2 mb-8">
        <div
          v-for="i in 3"
          :key="i"
          class="h-1.5 flex-1 rounded-full transition-all"
          :class="step >= i ? 'bg-[#7B61FF]' : 'bg-white/10'"
        />
      </div>

      <form @submit.prevent="handleNext" class="space-y-6">
        <div v-if="error" class="bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3 rounded-lg">
          {{ error }}
        </div>

        <div v-if="step === 1" class="space-y-4">
          <h2 class="text-xl font-bold">Personal Details</h2>
          <div class="grid grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1.5">First Name</label>
              <input v-model="form.firstName" required type="text" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 px-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-300 mb-1.5">Last Name</label>
              <input v-model="form.lastName" required type="text" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 px-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">Email</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Mail class="h-5 w-5 text-gray-500" />
              </div>
              <input v-model="form.email" required type="email" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
          </div>
        </div>

        <div v-if="step === 2" class="space-y-4">
          <h2 class="text-xl font-bold">Identity</h2>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">Phone Number</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Phone class="h-5 w-5 text-gray-500" />
              </div>
              <input v-model="form.phoneNumber" required type="tel" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">BSN Number</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <CreditCard class="h-5 w-5 text-gray-500" />
              </div>
              <input v-model="form.bsn" required type="text" placeholder="123456789" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
          </div>
        </div>

        <div v-if="step === 3" class="space-y-4">
          <h2 class="text-xl font-bold">Security</h2>
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-1.5">Create Password</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Lock class="h-5 w-5 text-gray-500" />
              </div>
              <input v-model="form.password" required type="password" class="w-full bg-[#1C1C24] border border-white/5 rounded-xl py-3 pl-10 pr-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all" />
            </div>
            <div v-if="form.password" class="mt-2 h-1.5 w-full bg-white/10 rounded-full overflow-hidden">
              <div
                class="h-full transition-all"
                :class="form.password.length > 8 ? 'bg-[#00D9A3] w-full' : form.password.length > 4 ? 'bg-yellow-500 w-1/2' : 'bg-[#FF5E5B] w-1/4'"
              />
            </div>
          </div>
        </div>

        <div class="flex gap-3 pt-4">
          <button v-if="step > 1" type="button" @click="step--" class="px-6 py-3 rounded-xl border border-white/10 text-white font-medium hover:bg-white/5 transition-colors">
            Back
          </button>
          <button type="submit" class="flex-1 bg-gradient-to-r from-[#7B61FF] to-[#5C45CC] text-white rounded-xl py-3 font-medium shadow-lg shadow-[#7B61FF]/25 hover:shadow-[#7B61FF]/40 transition-all flex items-center justify-center gap-2">
            {{ step === 3 ? 'Complete Registration' : 'Continue' }}
            <ChevronRight v-if="step !== 3" class="w-4 h-4" />
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
