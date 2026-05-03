<script setup>
import { ref, onMounted, computed } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, Filter } from 'lucide-vue-next'

const accounts = ref([])
const transactions = ref([])
const selectedIban = ref('')
const filters = ref({ from: '', to: '', amountGt: '', amountLt: '' })
const page = ref(0)
const totalPages = ref(0)

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}
function formatDate(ts) {
  return new Date(ts).toLocaleString('nl-NL')
}

async function loadTransactions() {
  if (!selectedIban.value) return
  const params = { page: page.value, size: 20, ...Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v)) }
  const { data } = await getHistory(selectedIban.value, params)
  transactions.value = data.content || []
  totalPages.value = data.totalPages || 0
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  if (data.length > 0) {
    selectedIban.value = data.find(a => a.accountType === 'CHECKING')?.iban || data[0].iban
    await loadTransactions()
  }
})

async function applyFilters() {
  page.value = 0
  await loadTransactions()
}
</script>

<template>
  <CustomerLayout>
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4">
      <h1 class="text-3xl font-bold text-white">Transactions</h1>

      <select
        v-model="selectedIban"
        @change="applyFilters"
        class="bg-[#14141A] border border-white/10 rounded-xl py-2 px-4 text-sm text-white focus:outline-none focus:border-[#7B61FF]"
      >
        <option v-for="a in accounts" :key="a.iban" :value="a.iban">
          {{ a.accountType.toLowerCase() }} · {{ a.iban }}
        </option>
      </select>
    </div>

    <div class="bg-[#14141A] border border-white/5 rounded-2xl p-4 mb-6 grid grid-cols-2 md:grid-cols-4 gap-3">
      <div>
        <label class="block text-xs text-gray-500 mb-1">From date</label>
        <input v-model="filters.from" type="date" class="w-full bg-[#1C1C24] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
      </div>
      <div>
        <label class="block text-xs text-gray-500 mb-1">To date</label>
        <input v-model="filters.to" type="date" class="w-full bg-[#1C1C24] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
      </div>
      <div>
        <label class="block text-xs text-gray-500 mb-1">Amount &gt;</label>
        <input v-model="filters.amountGt" type="number" placeholder="0.00" class="w-full bg-[#1C1C24] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
      </div>
      <div>
        <label class="block text-xs text-gray-500 mb-1">Amount &lt;</label>
        <input v-model="filters.amountLt" type="number" placeholder="0.00" class="w-full bg-[#1C1C24] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
      </div>
      <button @click="applyFilters" class="md:col-span-4 bg-[#7B61FF] hover:bg-[#6A52E5] text-white rounded-lg py-2 text-sm font-medium flex items-center justify-center gap-2 transition-colors">
        <Filter class="w-4 h-4" /> Apply Filters
      </button>
    </div>

    <div class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
      <div v-if="transactions.length > 0" class="divide-y divide-white/5">
        <div
          v-for="tx in transactions"
          :key="tx.id"
          class="flex items-center gap-4 p-4 hover:bg-white/[0.02] transition-colors"
        >
          <div
            class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0"
            :class="tx.fromIban === selectedIban ? 'text-[#FF5E5B] bg-[#FF5E5B]/10' : 'text-[#00D9A3] bg-[#00D9A3]/10'"
          >
            <ArrowUpRight v-if="tx.fromIban === selectedIban" class="w-5 h-5" />
            <ArrowDownLeft v-else class="w-5 h-5" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-medium text-white truncate text-lg">{{ tx.description || tx.type }}</p>
            <p class="text-sm text-gray-500 capitalize">{{ tx.type }} · {{ formatDate(tx.timestamp) }}</p>
          </div>
          <div
            class="font-bold tabular-nums whitespace-nowrap text-lg"
            :class="tx.fromIban === selectedIban ? 'text-white' : 'text-[#00D9A3]'"
          >
            {{ tx.fromIban === selectedIban ? '-' : '+' }}{{ formatEur(tx.amount) }}
          </div>
        </div>
      </div>
      <div v-else class="p-12 text-center text-gray-500">
        <p>No transactions found.</p>
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-6">
      <button :disabled="page === 0" @click="page--; loadTransactions()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Prev</button>
      <span class="px-4 py-2 text-sm text-gray-400">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadTransactions()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Next</button>
    </div>
  </CustomerLayout>
</template>
