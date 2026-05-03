<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { atmWithdraw } from '../../services/transactions'
import { ArrowLeft, ArrowRight, CheckCircle2, AlertCircle, Banknote, Cpu } from 'lucide-vue-next'

const router = useRouter()
const step = ref(1)
const iban = ref('')
const withdrawnAmount = ref(null)
const error = ref('')
const loading = ref(false)

const PRESET_AMOUNTS = [20, 50, 100, 200, 500, 1000]

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }

function handleNext() {
  error.value = ''
  if (!iban.value || iban.value.length < 10) { error.value = 'Please enter a valid IBAN.'; return }
  step.value = 2
}

async function handleWithdraw(selectedAmount) {
  error.value = ''
  loading.value = true
  try {
    await atmWithdraw({ iban: iban.value, amount: selectedAmount })
    withdrawnAmount.value = selectedAmount
    step.value = 3
    setTimeout(() => router.push('/atm'), 5000)
  } catch (e) {
    error.value = e.response?.data?.error || 'Insufficient funds or limit exceeded.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#0A0A0F] text-white flex flex-col items-center justify-center relative overflow-hidden">
    <!-- Background blobs -->
    <div class="absolute top-0 right-1/4 w-96 h-96 bg-[#7B61FF]/5 rounded-full blur-3xl pointer-events-none"></div>
    <div class="absolute bottom-0 left-1/4 w-72 h-72 bg-[#FF5E5B]/5 rounded-full blur-3xl pointer-events-none"></div>

    <!-- Top bar -->
    <div class="absolute top-0 left-0 right-0 flex items-center justify-between px-8 py-5 border-b border-white/[0.04]">
      <div class="flex items-center gap-3">
        <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center">
          <Cpu class="w-4.5 h-4.5 text-white" />
        </div>
        <div>
          <p class="text-sm font-bold text-white tracking-tight">Nova ATM</p>
          <p class="text-[10px] text-gray-600">Terminal #402</p>
        </div>
      </div>
      <button
        @click="router.push('/atm')"
        class="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/[0.04] hover:bg-white/[0.07] text-gray-500 hover:text-white border border-white/[0.06] text-xs font-semibold transition-all"
      >
        <ArrowLeft class="w-3.5 h-3.5" />
        Cancel
      </button>
    </div>

    <!-- Step indicator -->
    <div class="absolute top-20 left-0 right-0 flex justify-center gap-2 py-4">
      <div v-for="s in 3" :key="s" class="h-1 w-12 rounded-full transition-all" :class="s <= step ? 'bg-[#7B61FF]' : 'bg-white/[0.08]'"></div>
    </div>

    <!-- Main content -->
    <div class="w-full max-w-lg px-6 mt-16">

      <!-- Step 1: IBAN -->
      <div v-if="step === 1">
        <div class="text-center mb-8">
          <div class="w-14 h-14 rounded-2xl bg-[#7B61FF]/10 flex items-center justify-center mx-auto mb-4">
            <Banknote class="w-7 h-7 text-[#7B61FF]" />
          </div>
          <h1 class="text-2xl font-bold text-white">Cash Withdrawal</h1>
          <p class="text-sm text-gray-500 mt-1">Enter your checking account IBAN.</p>
        </div>

        <div v-if="error" class="flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl px-4 py-3 mb-4">
          <AlertCircle class="w-4 h-4 text-[#FF5E5B] flex-shrink-0" />
          <p class="text-sm text-[#FF5E5B]">{{ error }}</p>
        </div>

        <div class="bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-5">
          <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Account IBAN</label>
          <input
            v-model="iban"
            type="text"
            placeholder="NL00INHO0000000000"
            class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl px-4 py-3 text-sm text-white font-mono uppercase focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/40 placeholder:text-gray-700 transition-all mb-4"
            @input="iban = iban.toUpperCase()"
            @keydown.enter="handleNext"
          />
          <button
            @click="handleNext"
            class="w-full bg-[#7B61FF] hover:bg-[#6A52E5] text-white font-semibold text-sm py-3 rounded-xl flex items-center justify-center gap-2 transition-colors shadow-lg shadow-[#7B61FF]/20"
          >
            Continue <ArrowRight class="w-4 h-4" />
          </button>
        </div>
      </div>

      <!-- Step 2: Amount selection -->
      <div v-if="step === 2">
        <div class="text-center mb-8">
          <h1 class="text-2xl font-bold text-white">Select Amount</h1>
          <p class="text-sm text-gray-500 mt-1">Choose how much cash to withdraw.</p>
        </div>

        <div v-if="error" class="flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl px-4 py-3 mb-4">
          <AlertCircle class="w-4 h-4 text-[#FF5E5B] flex-shrink-0" />
          <p class="text-sm text-[#FF5E5B]">{{ error }}</p>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <button
            v-for="amt in PRESET_AMOUNTS"
            :key="amt"
            @click="handleWithdraw(amt)"
            :disabled="loading"
            class="group relative bg-[#0D0D14] border border-white/[0.05] rounded-2xl py-6 flex flex-col items-center justify-center gap-1 hover:border-[#7B61FF]/30 hover:bg-[#7B61FF]/5 disabled:opacity-50 transition-all overflow-hidden"
          >
            <span class="text-xl font-bold text-white tabular-nums group-hover:text-[#7B61FF] transition-colors">€{{ amt }}</span>
            <span class="text-[10px] text-gray-600">withdraw</span>
          </button>
        </div>

        <button @click="step = 1" class="w-full mt-4 text-xs text-gray-600 hover:text-gray-400 transition-colors py-2">
          ← Back
        </button>

        <div v-if="loading" class="flex items-center justify-center gap-2 mt-4 text-xs text-gray-500">
          <svg class="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
          Processing...
        </div>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 3" class="text-center">
        <div class="w-20 h-20 rounded-full bg-[#7B61FF]/10 border border-[#7B61FF]/20 flex items-center justify-center mx-auto mb-6">
          <CheckCircle2 class="w-10 h-10 text-[#7B61FF]" />
        </div>
        <h1 class="text-2xl font-bold text-white mb-2">Please Take Your Cash</h1>
        <p class="text-sm text-gray-500 mb-6">Withdrawal complete.</p>
        <div class="bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-5 inline-block">
          <p class="text-3xl font-bold text-[#7B61FF] tabular-nums">{{ eur(withdrawnAmount) }}</p>
          <p class="text-xs text-gray-600 mt-1">from {{ iban.slice(0, 8) }}···</p>
        </div>
        <p class="text-xs text-gray-700 mt-6">Returning to menu in a moment...</p>
      </div>

    </div>
  </div>
</template>
