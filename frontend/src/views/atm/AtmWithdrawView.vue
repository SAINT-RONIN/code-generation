<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { atmWithdraw } from '../../services/transactions'
import { ArrowLeft, CreditCard, ChevronRight } from 'lucide-vue-next'

const router = useRouter()
const step = ref(1)
const iban = ref('')
const withdrawnAmount = ref(null)
const error = ref('')

async function handleNext() {
  error.value = ''
  if (!iban.value || iban.value.length < 10) {
    error.value = 'Enter a valid IBAN'
    return
  }
  step.value = 2
}

async function handleWithdraw(selectedAmount) {
  error.value = ''
  try {
    await atmWithdraw({ iban: iban.value, amount: selectedAmount })
    withdrawnAmount.value = selectedAmount
    step.value = 3
    setTimeout(() => router.push('/atm'), 5000)
  } catch (e) {
    error.value = e.response?.data?.error || 'Insufficient funds or limit exceeded.'
  }
}
</script>

<template>
  <div class="min-h-screen bg-black text-white p-8 font-sans flex flex-col items-center justify-center relative">
    <button
      @click="router.push('/atm')"
      class="absolute top-8 left-8 flex items-center gap-2 bg-white/10 hover:bg-white/20 px-6 py-4 rounded-xl text-xl font-bold uppercase tracking-wider transition-colors border-2 border-white/20"
    >
      <ArrowLeft class="w-6 h-6" />
      Cancel
    </button>

    <div class="w-full max-w-4xl mt-12">
      <div v-if="step === 1" class="text-center">
        <h2 class="text-5xl font-bold mb-12 uppercase tracking-wider">Insert Card / Enter IBAN</h2>

        <p v-if="error" class="text-red-500 text-2xl font-bold mb-8 uppercase bg-red-500/10 py-4 rounded-xl">{{ error }}</p>

        <div class="max-w-xl mx-auto flex gap-4">
          <input
            v-model="iban"
            type="text"
            placeholder="NL00INHO0000000000"
            class="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-3xl font-mono text-center uppercase focus:outline-none focus:border-blue-500 transition-colors"
            @input="iban = iban.toUpperCase()"
          />
          <button
            @click="handleNext"
            class="bg-blue-600 hover:bg-blue-500 px-8 rounded-2xl border-4 border-blue-500 flex items-center justify-center transition-colors"
          >
            <ChevronRight class="w-12 h-12" />
          </button>
        </div>

        <div class="mt-16 flex flex-col items-center opacity-50">
          <CreditCard class="w-32 h-32 mb-4" />
          <p class="text-xl uppercase tracking-widest">Card Slot Simulator</p>
        </div>
      </div>

      <div v-if="step === 2">
        <h2 class="text-5xl font-bold mb-12 text-center uppercase tracking-wider">Select Amount</h2>

        <p v-if="error" class="text-red-500 text-2xl font-bold mb-8 text-center uppercase bg-red-500/10 py-4 rounded-xl mx-auto max-w-xl">{{ error }}</p>

        <div class="grid grid-cols-2 gap-6 max-w-3xl mx-auto">
          <button
            v-for="amt in [20, 50, 100, 200, 500]"
            :key="amt"
            @click="handleWithdraw(amt)"
            class="bg-blue-600 hover:bg-blue-500 py-10 rounded-2xl border-4 border-blue-500 text-4xl font-bold tabular-nums shadow-2xl transform hover:scale-105 active:scale-95 transition-all"
          >
            € {{ amt }}
          </button>
          <div class="bg-gray-800 py-10 rounded-2xl border-4 border-gray-600 text-3xl font-bold uppercase tracking-wider text-gray-500 flex items-center justify-center">
            Other
          </div>
        </div>
      </div>

      <div v-if="step === 3" class="text-center">
        <div class="w-48 h-48 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-12 animate-pulse">
          <span class="text-6xl font-bold">€</span>
        </div>
        <h2 class="text-5xl font-bold mb-6 uppercase tracking-wider text-blue-400">Please take your cash</h2>
        <p class="text-3xl text-gray-400 font-mono">Amount: € {{ withdrawnAmount }}</p>
        <p class="text-xl text-gray-500 mt-12 uppercase tracking-widest">Returning to menu...</p>
      </div>
    </div>
  </div>
</template>
