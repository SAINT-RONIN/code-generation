<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { atmDeposit } from '../../services/transactions'
import { ArrowLeft, ChevronRight, Inbox } from 'lucide-vue-next'

const router = useRouter()
const step = ref(1)
const iban = ref('')
const amount = ref('')
const error = ref('')

async function handleNext() {
  error.value = ''
  if (!iban.value || iban.value.length < 10) {
    error.value = 'Enter a valid IBAN'
    return
  }
  step.value = 2
}

async function handleDeposit() {
  error.value = ''
  const num = parseFloat(amount.value)
  if (isNaN(num) || num <= 0) {
    error.value = 'Enter a valid amount'
    return
  }
  try {
    await atmDeposit({ iban: iban.value, amount: num })
    step.value = 3
    setTimeout(() => router.push('/atm'), 5000)
  } catch (e) {
    error.value = e.response?.data?.error || 'Deposit failed.'
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
            class="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-3xl font-mono text-center uppercase focus:outline-none focus:border-green-500 transition-colors"
            @input="iban = iban.toUpperCase()"
          />
          <button
            @click="handleNext"
            class="bg-green-600 hover:bg-green-500 px-8 rounded-2xl border-4 border-green-500 flex items-center justify-center transition-colors"
          >
            <ChevronRight class="w-12 h-12" />
          </button>
        </div>
      </div>

      <div v-if="step === 2" class="text-center">
        <h2 class="text-5xl font-bold mb-12 uppercase tracking-wider">Insert Cash</h2>

        <p v-if="error" class="text-red-500 text-2xl font-bold mb-8 uppercase bg-red-500/10 py-4 rounded-xl max-w-xl mx-auto">{{ error }}</p>

        <div class="max-w-xl mx-auto flex gap-4 mb-16">
          <span class="bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-4xl font-bold flex items-center">€</span>
          <input
            v-model="amount"
            type="number"
            placeholder="0"
            class="w-full bg-gray-900 border-4 border-white/20 rounded-2xl p-6 text-4xl font-bold text-center focus:outline-none focus:border-green-500 transition-colors"
          />
        </div>

        <button
          @click="handleDeposit"
          class="bg-green-600 hover:bg-green-500 py-6 px-16 rounded-2xl border-4 border-green-500 text-3xl font-bold uppercase tracking-wider shadow-2xl transform hover:scale-105 active:scale-95 transition-all mx-auto"
        >
          Deposit
        </button>

        <div class="mt-16 flex flex-col items-center opacity-50">
          <Inbox class="w-32 h-32 mb-4" />
          <p class="text-xl uppercase tracking-widest">Cash Slot Simulator</p>
        </div>
      </div>

      <div v-if="step === 3" class="text-center">
        <div class="w-48 h-48 bg-green-600 rounded-full flex items-center justify-center mx-auto mb-12">
          <Inbox class="w-24 h-24 text-white" />
        </div>
        <h2 class="text-5xl font-bold mb-6 uppercase tracking-wider text-green-400">Deposit Accepted</h2>
        <p class="text-3xl text-gray-400 font-mono">Amount: €{{ amount }}</p>
        <p class="text-xl text-gray-500 mt-12 uppercase tracking-widest">Returning to menu...</p>
      </div>
    </div>
  </div>
</template>
