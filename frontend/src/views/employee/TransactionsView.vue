<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getAll } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, FileText } from 'lucide-vue-next'

const transactions = ref([])
const page = ref(0)
const totalPages = ref(0)
const filters = ref({ from: '', to: '', amountGt: '', amountLt: '', iban: '' })

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}
function formatDate(ts) {
  return new Date(ts).toLocaleString('nl-NL')
}

function iconFor(tx) {
  if (tx.type === 'DEPOSIT') return { component: ArrowDownLeft, cls: 'text-[#00D9A3] bg-[#00D9A3]/10' }
  if (tx.type === 'WITHDRAWAL') return { component: ArrowUpRight, cls: 'text-[#FF5E5B] bg-[#FF5E5B]/10' }
  return { component: ArrowLeftRight, cls: 'text-[#7B61FF] bg-[#7B61FF]/10' }
}

async function loadTransactions() {
  const params = { page: page.value, size: 25, ...Object.fromEntries(Object.entries(filters.value).filter(([, v]) => v)) }
  const { data } = await getAll(params)
  transactions.value = data.content || []
  totalPages.value = data.totalPages || 0
}

onMounted(loadTransactions)

async function applyFilters() {
  page.value = 0
  await loadTransactions()
}
</script>

<template>
  <EmployeeLayout>
    <div class="flex flex-col md:flex-row md:items-center justify-between mb-6 gap-4">
      <div>
        <h1 class="text-3xl font-bold tracking-tight text-white mb-2">System Ledger</h1>
        <p class="text-gray-400">Global view of all platform transactions.</p>
      </div>
    </div>

    <div class="bg-[#14141A] border border-white/5 rounded-2xl p-4 mb-6 grid grid-cols-2 md:grid-cols-5 gap-3">
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
        <label class="block text-xs text-gray-500 mb-1">IBAN</label>
        <input v-model="filters.iban" type="text" placeholder="NL00..." class="w-full bg-[#1C1C24] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none uppercase" @input="filters.iban = filters.iban.toUpperCase()" />
      </div>
      <button @click="applyFilters" class="bg-[#FF5E5B] hover:bg-[#E54542] text-white rounded-lg py-2 text-sm font-medium transition-colors self-end">
        Apply
      </button>
    </div>

    <div class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
      <div v-if="transactions.length > 0" class="divide-y divide-white/5">
        <div
          v-for="tx in transactions"
          :key="tx.id"
          class="flex flex-col md:flex-row md:items-center gap-4 p-5 hover:bg-white/[0.02] transition-colors"
        >
          <div class="flex items-center gap-4 flex-1">
            <div class="w-12 h-12 rounded-full flex items-center justify-center flex-shrink-0" :class="iconFor(tx).cls">
              <component :is="iconFor(tx).component" class="w-5 h-5" />
            </div>
            <div>
              <p class="font-bold text-white text-lg mb-1">{{ tx.description || tx.type }}</p>
              <p class="text-sm text-gray-400 capitalize flex items-center gap-2">
                {{ tx.type.toLowerCase() }} · {{ formatDate(tx.timestamp) }}
                <span class="bg-white/10 px-2 py-0.5 rounded text-xs">{{ tx.performedBy }}</span>
              </p>
            </div>
          </div>
          <div class="flex items-center justify-between md:flex-col md:items-end gap-1 ml-16 md:ml-0">
            <span class="font-bold text-xl tabular-nums text-white">{{ formatEur(tx.amount) }}</span>
            <span class="text-xs text-gray-500 font-mono">{{ tx.fromIban || 'ATM' }} → {{ tx.toIban || 'ATM' }}</span>
          </div>
        </div>
      </div>
      <div v-else class="p-12 text-center text-gray-500 flex flex-col items-center">
        <FileText class="w-10 h-10 mb-4 opacity-20" />
        <p>No transactions found.</p>
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-6">
      <button :disabled="page === 0" @click="page--; loadTransactions()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Prev</button>
      <span class="px-4 py-2 text-sm text-gray-400">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadTransactions()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Next</button>
    </div>
  </EmployeeLayout>
</template>
