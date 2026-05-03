<script setup>
import { ref, computed, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { transfer } from '../../services/transactions'
import { ArrowRight, CheckCircle2, AlertCircle } from 'lucide-vue-next'

const accounts = ref([])
const step = ref(1)
const fromIban = ref('')
const toIban = ref('')
const amount = ref('')
const description = ref('')
const error = ref('')

const selectedAccount = computed(() => accounts.value.find(a => a.iban === fromIban.value))

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  if (data.length > 0) fromIban.value = data[0].iban
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
  try {
    await transfer({ fromIban: fromIban.value, toIban: toIban.value, amount: parseFloat(amount.value), description: description.value || 'Transfer' })
    step.value = 3
  } catch (e) {
    error.value = e.response?.data?.error || 'Transfer failed.'
    step.value = 1
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
    <div class="max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold tracking-tight mb-8 text-white">Make a Transfer</h1>

      <div v-if="step === 1" class="space-y-8">
        <div v-if="error" class="p-4 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl text-[#FF5E5B] flex items-center gap-3">
          <AlertCircle class="w-5 h-5 flex-shrink-0" />
          <p class="font-medium">{{ error }}</p>
        </div>

        <section>
          <h2 class="text-lg font-bold text-white mb-4">From Account</h2>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <button
              v-for="account in accounts"
              :key="account.iban"
              @click="fromIban = account.iban"
              class="p-4 rounded-xl border text-left transition-all"
              :class="fromIban === account.iban ? 'border-[#7B61FF] bg-[#7B61FF]/10' : 'border-white/5 bg-[#14141A] hover:bg-[#1C1C24]'"
            >
              <p class="text-sm text-gray-400 capitalize mb-1">{{ account.accountType.toLowerCase() }}</p>
              <p class="text-xl font-bold tabular-nums text-white mb-1">{{ formatEur(account.balance) }}</p>
              <p class="text-xs font-mono text-gray-500">{{ account.iban }}</p>
            </button>
          </div>
        </section>

        <section class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">To IBAN</label>
            <input
              v-model="toIban"
              type="text"
              placeholder="NL00 INHO 0000 0000 00"
              class="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none font-mono text-lg transition-all uppercase"
              @input="toIban = toIban.toUpperCase()"
            />
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">Amount</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-2xl text-gray-500">€</span>
              <input
                v-model="amount"
                type="number"
                placeholder="0.00"
                step="0.01"
                min="0.01"
                class="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 pl-10 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none text-2xl font-bold tabular-nums transition-all"
              />
            </div>
          </div>

          <div>
            <label class="block text-sm font-medium text-gray-300 mb-2">Description (Optional)</label>
            <input
              v-model="description"
              type="text"
              placeholder="What's this for?"
              class="w-full bg-[#14141A] border border-white/5 rounded-xl p-4 text-white focus:ring-2 focus:ring-[#7B61FF]/50 outline-none transition-all"
            />
          </div>
        </section>

        <button
          @click="reviewTransfer"
          class="w-full bg-[#7B61FF] hover:bg-[#6A52E5] text-white p-4 rounded-xl font-bold text-lg transition-colors flex items-center justify-center gap-2 shadow-lg shadow-[#7B61FF]/20"
        >
          Review Transfer <ArrowRight class="w-5 h-5" />
        </button>
      </div>

      <div v-if="step === 2" class="space-y-6">
        <div class="bg-[#14141A] border border-white/5 rounded-2xl p-6">
          <h2 class="text-xl font-bold text-white mb-6 pb-4 border-b border-white/5">Review Details</h2>
          <div class="space-y-4">
            <div class="flex justify-between items-center">
              <span class="text-gray-400">Amount</span>
              <span class="text-2xl font-bold tabular-nums text-white">{{ formatEur(amount) }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-400">From</span>
              <span class="font-medium font-mono text-white">{{ fromIban }}</span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-400">To</span>
              <span class="font-medium font-mono text-white">{{ toIban }}</span>
            </div>
            <div v-if="description" class="flex justify-between items-center">
              <span class="text-gray-400">Description</span>
              <span class="font-medium text-white">{{ description }}</span>
            </div>
          </div>
        </div>

        <div class="flex gap-4">
          <button @click="step = 1" class="flex-1 bg-[#1C1C24] hover:bg-[#252530] text-white p-4 rounded-xl font-bold transition-colors">Back</button>
          <button @click="confirmTransfer" class="flex-[2] bg-[#7B61FF] hover:bg-[#6A52E5] text-white p-4 rounded-xl font-bold transition-colors shadow-lg shadow-[#7B61FF]/20">Confirm Transfer</button>
        </div>
      </div>

      <div v-if="step === 3" class="text-center py-12">
        <div class="w-24 h-24 bg-[#00D9A3]/10 text-[#00D9A3] rounded-full flex items-center justify-center mx-auto mb-6">
          <CheckCircle2 class="w-12 h-12" />
        </div>
        <h2 class="text-3xl font-bold text-white mb-2">Transfer Successful</h2>
        <p class="text-gray-400 mb-8">{{ formatEur(amount) }} sent to {{ toIban }}</p>
        <button @click="reset" class="bg-[#1C1C24] hover:bg-[#252530] text-white px-8 py-3 rounded-xl font-bold transition-colors">
          Make another transfer
        </button>
      </div>
    </div>
  </CustomerLayout>
</template>
