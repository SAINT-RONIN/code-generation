<script setup>
import { ref, computed, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { transfer } from '../../services/transactions'
import { ArrowRight, CheckCircle2, AlertCircle, ArrowDown } from 'lucide-vue-next'

const accounts = ref([])
const step = ref(1)
const fromIban = ref('')
const toIban = ref('')
const amount = ref('')
const description = ref('')
const error = ref('')
const loading = ref(false)

const selectedAccount = computed(() => accounts.value.find(a => a.iban === fromIban.value))

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  const checking = data.find(a => a.accountType === 'CHECKING')
  if (checking) fromIban.value = checking.iban
})

function reviewTransfer() {
  error.value = ''
  if (!fromIban.value) return (error.value = 'Select a source account')
  if (!toIban.value || toIban.value.length < 10) return (error.value = 'Enter a valid IBAN')
  if (!amount.value || parseFloat(amount.value) <= 0) return (error.value = 'Enter a valid amount')
  step.value = 2
}

async function confirmTransfer() {
  error.value = ''
  loading.value = true
  try {
    await transfer({ fromIban: fromIban.value, toIban: toIban.value, amount: parseFloat(amount.value), description: description.value || 'Transfer' })
    step.value = 3
  } catch (e) {
    error.value = e.response?.data?.error || 'Transfer failed.'
    step.value = 1
  } finally {
    loading.value = false
  }
}

function reset() {
  step.value = 1
  toIban.value = ''
  amount.value = ''
  description.value = ''
  error.value = ''
}
</script>

<template>
  <CustomerLayout>
    <div class="max-w-lg mx-auto">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-white">Make a Transfer</h1>
        <p class="text-sm text-gray-500 mt-1">Send money to any account instantly.</p>
      </div>

      <!-- Step indicators -->
      <div class="flex items-center gap-2 mb-8">
        <div v-for="i in 2" :key="i" class="flex items-center gap-2">
          <div class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold transition-all"
            :class="step > i ? 'bg-[#7B61FF] text-white' : step === i ? 'bg-[#7B61FF]/20 text-[#7B61FF] ring-2 ring-[#7B61FF]/30' : 'bg-white/5 text-gray-600'">
            <CheckCircle2 v-if="step > i" class="w-3.5 h-3.5" />
            <span v-else>{{ i }}</span>
          </div>
          <div v-if="i < 2" class="w-16 h-px" :class="step > i ? 'bg-[#7B61FF]/50' : 'bg-white/10'"></div>
        </div>
      </div>

      <!-- Step 1: Form -->
      <div v-if="step === 1" class="space-y-5">
        <div v-if="error" class="flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 text-[#FF5E5B] text-sm p-3.5 rounded-xl">
          <AlertCircle class="w-4 h-4 flex-shrink-0" /><span>{{ error }}</span>
        </div>

        <!-- From account selector -->
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-3">From</p>
          <div class="grid gap-2">
            <button
              v-for="account in accounts"
              :key="account.iban"
              @click="fromIban = account.iban"
              type="button"
              class="flex items-center gap-3.5 p-4 rounded-xl border text-left transition-all"
              :class="fromIban === account.iban
                ? 'border-[#7B61FF]/50 bg-[#7B61FF]/8 ring-1 ring-[#7B61FF]/20'
                : 'border-white/5 bg-[#14141A] hover:bg-white/[0.03]'"
            >
              <div class="w-9 h-9 rounded-lg flex items-center justify-center flex-shrink-0"
                :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/15 text-[#7B61FF]' : 'bg-[#00D9A3]/15 text-[#00D9A3]'">
                <div class="w-3 h-3 rounded-full" :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]' : 'bg-[#00D9A3]'"></div>
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-semibold text-white capitalize">{{ account.accountType.toLowerCase() }}</p>
                <p class="text-xs text-gray-600 font-mono truncate">{{ account.iban }}</p>
              </div>
              <div class="text-right flex-shrink-0">
                <p class="text-sm font-bold tabular-nums text-white">{{ eur(account.balance) }}</p>
                <p class="text-[10px] text-gray-600">available</p>
              </div>
            </button>
          </div>
        </div>

        <!-- Arrow separator -->
        <div class="flex items-center gap-3">
          <div class="h-px flex-1 bg-white/[0.05]"></div>
          <div class="w-7 h-7 rounded-full bg-[#1C1C24] border border-white/10 flex items-center justify-center">
            <ArrowDown class="w-3.5 h-3.5 text-gray-500" />
          </div>
          <div class="h-px flex-1 bg-white/[0.05]"></div>
        </div>

        <!-- To IBAN -->
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-3">To</p>
          <input
            v-model="toIban"
            type="text"
            placeholder="NL00 INHO 0000 0000 00"
            class="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white text-sm font-mono tracking-wider focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/30 focus:border-[#7B61FF]/40 placeholder:text-gray-700 transition-all uppercase"
            @input="toIban = toIban.toUpperCase()"
          />
        </div>

        <!-- Amount -->
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-3">Amount</p>
          <div class="relative bg-[#14141A] border border-white/5 rounded-xl focus-within:ring-2 focus-within:ring-[#7B61FF]/30 focus-within:border-[#7B61FF]/40 transition-all">
            <span class="absolute left-4 top-1/2 -translate-y-1/2 text-2xl font-bold text-gray-600">€</span>
            <input
              v-model="amount"
              type="number"
              placeholder="0.00"
              step="0.01"
              min="0.01"
              class="w-full bg-transparent py-4 pl-10 pr-4 text-white text-2xl font-bold tabular-nums focus:outline-none placeholder:text-gray-700"
            />
          </div>
          <p v-if="selectedAccount" class="text-xs text-gray-600 mt-1.5 ml-1">
            Available: <span class="text-gray-400">{{ eur(selectedAccount.balance) }}</span>
          </p>
        </div>

        <!-- Description -->
        <div>
          <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-3">Description <span class="text-gray-700 font-normal normal-case">(optional)</span></p>
          <input
            v-model="description"
            type="text"
            placeholder="What's this for?"
            class="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/30 focus:border-[#7B61FF]/40 placeholder:text-gray-700 transition-all"
          />
        </div>

        <button
          @click="reviewTransfer"
          class="w-full bg-gradient-to-r from-[#7B61FF] to-[#6050D0] text-white py-4 rounded-xl font-semibold shadow-lg shadow-[#7B61FF]/20 hover:shadow-[#7B61FF]/35 hover:from-[#8B71FF] transition-all flex items-center justify-center gap-2"
        >
          Review Transfer <ArrowRight class="w-4 h-4" />
        </button>
      </div>

      <!-- Step 2: Review -->
      <div v-if="step === 2" class="space-y-5">
        <div class="bg-[#14141A] border border-white/5 rounded-2xl overflow-hidden">
          <div class="px-5 py-4 border-b border-white/[0.05]">
            <p class="text-sm font-semibold text-white">Review your transfer</p>
          </div>
          <div class="p-5 space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-sm text-gray-500">Amount</span>
              <span class="text-2xl font-bold tabular-nums text-white">{{ eur(amount) }}</span>
            </div>
            <div class="h-px bg-white/[0.05]"></div>
            <div class="flex justify-between items-start">
              <span class="text-sm text-gray-500">From</span>
              <div class="text-right">
                <p class="text-sm font-medium text-white capitalize">{{ selectedAccount?.accountType.toLowerCase() }}</p>
                <p class="text-xs text-gray-600 font-mono">{{ fromIban }}</p>
              </div>
            </div>
            <div class="flex justify-between items-start">
              <span class="text-sm text-gray-500">To</span>
              <p class="text-sm font-medium font-mono text-white">{{ toIban }}</p>
            </div>
            <div v-if="description" class="flex justify-between">
              <span class="text-sm text-gray-500">Note</span>
              <span class="text-sm text-white">{{ description }}</span>
            </div>
          </div>
        </div>

        <div class="flex gap-3">
          <button @click="step = 1" class="flex-1 py-3.5 rounded-xl border border-white/10 text-sm font-medium text-gray-400 hover:bg-white/[0.04] hover:text-white transition-all">Back</button>
          <button
            @click="confirmTransfer"
            :disabled="loading"
            class="flex-[2] bg-gradient-to-r from-[#7B61FF] to-[#6050D0] text-white py-3.5 rounded-xl font-semibold shadow-lg shadow-[#7B61FF]/20 hover:shadow-[#7B61FF]/35 disabled:opacity-60 transition-all flex items-center justify-center gap-2"
          >
            <svg v-if="loading" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
            {{ loading ? 'Processing...' : 'Confirm Transfer' }}
          </button>
        </div>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 3" class="text-center py-10">
        <div class="w-20 h-20 bg-[#00D9A3]/10 border border-[#00D9A3]/20 rounded-full flex items-center justify-center mx-auto mb-5">
          <CheckCircle2 class="w-10 h-10 text-[#00D9A3]" />
        </div>
        <h2 class="text-2xl font-bold text-white mb-2">Transfer Complete</h2>
        <p class="text-gray-500 text-sm mb-8">
          <span class="text-white font-semibold">{{ eur(amount) }}</span> sent to <span class="font-mono text-gray-400">{{ toIban }}</span>
        </p>
        <button @click="reset" class="bg-[#1C1C24] hover:bg-[#252530] text-white px-8 py-3 rounded-xl font-medium text-sm transition-colors">
          Make another transfer
        </button>
      </div>
    </div>
  </CustomerLayout>
</template>
