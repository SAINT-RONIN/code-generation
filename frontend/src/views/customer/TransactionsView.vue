<script setup>
import { ref, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, SlidersHorizontal, X } from 'lucide-vue-next'

const accounts = ref([])
const transactions = ref([])
const selectedIban = ref('')
const showFilters = ref(false)
const filters = ref({ from: '', to: '', amountGt: '', amountLt: '' })
const page = ref(0)
const totalPages = ref(0)
const loading = ref(false)

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }
function fmtDate(ts) { return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) }
function fmtTime(ts) { return new Date(ts).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) }

async function loadTransactions() {
  if (!selectedIban.value) return
  loading.value = true
  try {
    const params = { page: page.value, size: 20, ...Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v)) }
    const { data } = await getHistory(selectedIban.value, params)
    transactions.value = data.content || []
    totalPages.value = data.totalPages || 0
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  const checking = data.find(a => a.accountType === 'CHECKING')
  selectedIban.value = checking?.iban || data[0]?.iban || ''
  await loadTransactions()
})

async function applyFilters() { page.value = 0; await loadTransactions() }
function clearFilters() { filters.value = { from: '', to: '', amountGt: '', amountLt: '' }; applyFilters() }
</script>

<template>
  <CustomerLayout>
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-white">Transaction History</h1>
      <p class="text-sm text-gray-500 mt-1">All your account activity.</p>
    </div>

    <!-- Account tabs + filter toggle -->
    <div class="flex items-center justify-between gap-3 mb-4">
      <div class="flex gap-1.5 bg-[#14141A] border border-white/5 rounded-xl p-1">
        <button
          v-for="a in accounts"
          :key="a.iban"
          @click="selectedIban = a.iban; page = 0; loadTransactions()"
          class="px-3 py-1.5 rounded-lg text-xs font-medium transition-all capitalize"
          :class="selectedIban === a.iban ? 'bg-[#7B61FF]/15 text-[#7B61FF]' : 'text-gray-500 hover:text-gray-300'"
        >
          {{ a.accountType.toLowerCase() }}
        </button>
      </div>
      <button @click="showFilters = !showFilters" class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-medium transition-all"
        :class="showFilters ? 'border-[#7B61FF]/40 bg-[#7B61FF]/10 text-[#7B61FF]' : 'border-white/10 text-gray-500 hover:text-white hover:border-white/20'">
        <SlidersHorizontal class="w-3.5 h-3.5" /> Filters
      </button>
    </div>

    <!-- Filters panel -->
    <div v-if="showFilters" class="bg-[#14141A] border border-white/5 rounded-2xl p-4 mb-4">
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-3">
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">From date</label>
          <input v-model="filters.from" type="date" class="w-full bg-[#1C1C24] border border-white/5 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">To date</label>
          <input v-model="filters.to" type="date" class="w-full bg-[#1C1C24] border border-white/5 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">Min amount</label>
          <input v-model="filters.amountGt" type="number" placeholder="€0" class="w-full bg-[#1C1C24] border border-white/5 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">Max amount</label>
          <input v-model="filters.amountLt" type="number" placeholder="€∞" class="w-full bg-[#1C1C24] border border-white/5 rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
        </div>
      </div>
      <div class="flex gap-2">
        <button @click="applyFilters" class="flex-1 bg-[#7B61FF] hover:bg-[#6A52E5] text-white rounded-lg py-2 text-xs font-semibold transition-colors">Apply</button>
        <button @click="clearFilters" class="px-4 py-2 rounded-lg border border-white/10 text-gray-500 hover:text-white text-xs transition-all flex items-center gap-1.5"><X class="w-3.5 h-3.5" /> Clear</button>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 6" :key="i" class="skeleton h-16 rounded-xl"></div>
    </div>

    <!-- Transactions list -->
    <div v-else class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
      <div v-if="transactions.length > 0" class="divide-y divide-white/[0.04]">
        <div v-for="tx in transactions" :key="tx.id" class="flex items-center gap-3.5 px-4 py-3.5 hover:bg-white/[0.02] transition-colors">
          <div class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
            :class="tx.fromIban === selectedIban ? 'bg-[#FF5E5B]/10 text-[#FF5E5B]' : 'bg-[#00D9A3]/10 text-[#00D9A3]'">
            <ArrowUpRight v-if="tx.fromIban === selectedIban" class="w-4 h-4" />
            <ArrowDownLeft v-else class="w-4 h-4" />
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-white truncate">{{ tx.description || tx.type }}</p>
            <p class="text-[11px] text-gray-600 mt-0.5">
              <span class="capitalize px-1.5 py-0.5 rounded-md bg-white/[0.04] mr-1.5">{{ tx.type.toLowerCase() }}</span>
              {{ fmtDate(tx.timestamp) }} at {{ fmtTime(tx.timestamp) }}
            </p>
          </div>
          <div class="text-right flex-shrink-0">
            <p class="text-sm font-semibold tabular-nums" :class="tx.fromIban === selectedIban ? 'text-gray-300' : 'text-[#00D9A3]'">
              {{ tx.fromIban === selectedIban ? '−' : '+' }}{{ eur(tx.amount) }}
            </p>
          </div>
        </div>
      </div>
      <div v-else class="flex flex-col items-center justify-center py-14 text-gray-600">
        <div class="w-10 h-10 rounded-full bg-white/[0.03] flex items-center justify-center mb-3">
          <ArrowLeftRight class="w-5 h-5 opacity-30" />
        </div>
        <p class="text-sm">No transactions found</p>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-5">
      <button :disabled="page === 0" @click="page--; loadTransactions()" class="px-3 py-1.5 rounded-lg bg-[#14141A] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">← Prev</button>
      <span class="text-xs text-gray-600 px-2">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadTransactions()" class="px-3 py-1.5 rounded-lg bg-[#14141A] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">Next →</button>
    </div>
  </CustomerLayout>
</template>
