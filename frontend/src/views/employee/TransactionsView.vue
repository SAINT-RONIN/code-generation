<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getAll } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, FileText, X, SlidersHorizontal } from 'lucide-vue-next'

const transactions = ref([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(true)
const showFilters = ref(false)
const filters = ref({ from: '', to: '', amountGt: '', amountLt: '', iban: '' })

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }
function fmtDate(ts) { return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }) }
function fmtTime(ts) { return new Date(ts).toLocaleTimeString('en-GB', { hour: '2-digit', minute: '2-digit' }) }

function txIcon(tx) {
  if (tx.type === 'DEPOSIT') return { component: ArrowDownLeft, color: 'bg-[#00D9A3]/10 text-[#00D9A3]' }
  if (tx.type === 'WITHDRAWAL') return { component: ArrowUpRight, color: 'bg-[#FF5E5B]/10 text-[#FF5E5B]' }
  return { component: ArrowLeftRight, color: 'bg-[#7B61FF]/10 text-[#7B61FF]' }
}

async function loadTransactions() {
  loading.value = true
  try {
    const params = { page: page.value, size: 25, ...Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v)) }
    const { data } = await getAll(params)
    transactions.value = data.content || []
    totalPages.value = data.totalPages || 0
  } finally {
    loading.value = false
  }
}

async function applyFilters() { page.value = 0; await loadTransactions() }
function clearFilters() { filters.value = { from: '', to: '', amountGt: '', amountLt: '', iban: '' }; applyFilters() }

onMounted(loadTransactions)
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8 flex items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-white">System Ledger</h1>
        <p class="text-sm text-gray-500 mt-1">Global view of all platform transactions.</p>
      </div>
      <button
        @click="showFilters = !showFilters"
        class="flex items-center gap-1.5 px-3 py-1.5 rounded-xl border text-xs font-medium transition-all flex-shrink-0"
        :class="showFilters ? 'border-[#FF5E5B]/40 bg-[#FF5E5B]/10 text-[#FF5E5B]' : 'border-white/10 text-gray-500 hover:text-white hover:border-white/20'"
      >
        <SlidersHorizontal class="w-3.5 h-3.5" /> Filters
      </button>
    </div>

    <!-- Filters panel -->
    <div v-if="showFilters" class="bg-[#0D0D14] border border-white/[0.06] rounded-2xl p-4 mb-4">
      <div class="grid grid-cols-2 md:grid-cols-5 gap-3 mb-3">
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">From date</label>
          <input v-model="filters.from" type="date" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">To date</label>
          <input v-model="filters.to" type="date" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">Min amount</label>
          <input v-model="filters.amountGt" type="number" placeholder="€0" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/30" />
        </div>
        <div>
          <label class="block text-[10px] text-gray-600 mb-1.5 uppercase tracking-wider">IBAN</label>
          <input v-model="filters.iban" type="text" placeholder="NL00..." class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white font-mono uppercase focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/30" @input="filters.iban = filters.iban.toUpperCase()" />
        </div>
        <div class="flex flex-col justify-end">
          <button @click="applyFilters" class="w-full bg-[#FF5E5B] hover:bg-[#E54542] text-white rounded-lg py-2 text-xs font-semibold transition-colors">Apply</button>
        </div>
      </div>
      <button @click="clearFilters" class="flex items-center gap-1 text-xs text-gray-600 hover:text-gray-400 transition-colors">
        <X class="w-3 h-3" /> Clear filters
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 8" :key="i" class="skeleton h-16 rounded-xl"></div>
    </div>

    <!-- Table -->
    <div v-else class="bg-[#0D0D14] rounded-2xl border border-white/[0.05] overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-white/[0.05] text-[11px] font-semibold text-gray-600 uppercase tracking-wider">
              <th class="px-4 py-3">Type</th>
              <th class="px-4 py-3">Description</th>
              <th class="px-4 py-3">From</th>
              <th class="px-4 py-3">To</th>
              <th class="px-4 py-3">Amount</th>
              <th class="px-4 py-3">Date</th>
              <th class="px-4 py-3">By</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/[0.03]">
            <tr v-for="tx in transactions" :key="tx.id" class="hover:bg-white/[0.02] transition-colors">
              <td class="px-4 py-3.5">
                <div class="w-8 h-8 rounded-lg flex items-center justify-center" :class="txIcon(tx).color">
                  <component :is="txIcon(tx).component" class="w-3.5 h-3.5" />
                </div>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-sm text-white font-medium">{{ tx.description || '—' }}</p>
                <span class="inline-block text-[10px] px-1.5 py-0.5 rounded-md bg-white/[0.04] text-gray-500 capitalize mt-0.5">{{ tx.type.toLowerCase() }}</span>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-xs text-gray-500 font-mono">{{ tx.fromIban || 'ATM' }}</p>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-xs text-gray-500 font-mono">{{ tx.toIban || 'ATM' }}</p>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-sm font-semibold tabular-nums text-white">{{ eur(tx.amount) }}</p>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-xs text-gray-400">{{ fmtDate(tx.timestamp) }}</p>
                <p class="text-[10px] text-gray-600">{{ fmtTime(tx.timestamp) }}</p>
              </td>
              <td class="px-4 py-3.5">
                <p class="text-[11px] text-gray-600 truncate max-w-[120px]">{{ tx.performedBy }}</p>
              </td>
            </tr>
          </tbody>
        </table>

        <div v-if="transactions.length === 0" class="py-14 flex flex-col items-center text-gray-600">
          <FileText class="w-8 h-8 mb-3 opacity-30" />
          <p class="text-sm">No transactions found.</p>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-5">
      <button :disabled="page === 0" @click="page--; loadTransactions()" class="px-3 py-1.5 rounded-lg bg-[#0D0D14] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">← Prev</button>
      <span class="text-xs text-gray-600 px-2">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadTransactions()" class="px-3 py-1.5 rounded-lg bg-[#0D0D14] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">Next →</button>
    </div>
  </EmployeeLayout>
</template>
