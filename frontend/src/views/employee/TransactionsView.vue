<script setup>
import { ref, watch, onMounted } from 'vue'
import { Filter, X, ChevronLeft, ChevronRight, AlignLeft } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VBtn from '../../components/ui/VBtn.vue'
import VPill from '../../components/ui/VPill.vue'
import VModal from '../../components/ui/VModal.vue'
import { getTransactions } from '../../services/transactions'
import { eur } from '../../utils/format'

const transactions = ref([])
const totalElements = ref(0)
const totalPages = ref(0)
const page = ref(0)
const pageSize = 15
const loading = ref(true)
const error = ref('')

const showFilters = ref(false)
const filters = ref({ from: '', to: '', amountMin: '', amountMax: '', iban: '', transactionType: '' })
const selectedTx = ref(null)

async function load() {
  loading.value = true
  error.value = ''
  try {
    const params = {
      page: page.value,
      size: pageSize,
      sort: 'timestamp,desc',
    }
    if (filters.value.from) params.from = filters.value.from
    if (filters.value.to) params.to = filters.value.to
    if (filters.value.amountMin) params.amountMin = filters.value.amountMin
    if (filters.value.amountMax) params.amountMax = filters.value.amountMax
    if (filters.value.iban) params.iban = filters.value.iban
    if (filters.value.transactionType) params.transactionType = filters.value.transactionType
    const { data } = await getTransactions(params)
    transactions.value = data.content ?? []
    totalElements.value = data.totalElements ?? 0
    totalPages.value = data.totalPages ?? 0
  } catch {
    error.value = 'Could not load transactions.'
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(page, load)

function applyFilters() {
  page.value = 0
  load()
}

function clearFilters() {
  filters.value = { from: '', to: '', amountMin: '', amountMax: '', iban: '', transactionType: '' }
  page.value = 0
  load()
}

function fmtTime(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function txLabel(type) {
  if (!type) return '—'
  return type.toLowerCase().replace(/_/g, ' ')
}
</script>

<template>
  <EmployeeLayout>
    <VPageHeader eyebrow="Staff portal" title="All transactions">
      <template #right>
        <VBtn variant="secondary" size="sm" @click="showFilters = !showFilters">
          <Filter class="w-4 h-4" />
          {{ showFilters ? 'Hide filters' : 'Filters' }}
        </VBtn>
      </template>
    </VPageHeader>

    <!-- Filter bar -->
    <div v-if="showFilters" class="mb-6 rounded-2xl border p-5" :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }">
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4 mb-4">
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">From date</label>
          <input v-model="filters.from" type="date"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">To date</label>
          <input v-model="filters.to" type="date"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">Min amount (€)</label>
          <input v-model="filters.amountMin" type="number" placeholder="0"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">Max amount (€)</label>
          <input v-model="filters.amountMax" type="number" placeholder="∞"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">IBAN contains</label>
          <input v-model="filters.iban" type="text" placeholder="NL12…"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">Type</label>
          <select v-model="filters.transactionType"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }">
            <option value="">All types</option>
            <option value="TRANSFER">Transfer</option>
            <option value="DEPOSIT">Deposit</option>
            <option value="WITHDRAWAL">Withdrawal</option>
          </select>
        </div>
      </div>
      <div class="flex gap-2">
        <VBtn variant="primary" size="sm" @click="applyFilters">Apply</VBtn>
        <VBtn variant="ghost" size="sm" @click="clearFilters">
          <X class="w-3.5 h-3.5" /> Clear
        </VBtn>
      </div>
    </div>

    <!-- Error -->
    <div
      v-if="error"
      class="mb-6 px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
    >{{ error }}</div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 8" :key="i" class="skeleton h-14 rounded-xl" />
    </div>

    <template v-else>
      <!-- Empty state -->
      <div
        v-if="!transactions.length"
        class="rounded-2xl border py-20 text-center"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <AlignLeft class="w-10 h-10 mx-auto mb-3" :style="{ color: 'var(--ink-3)' }" />
        <p class="text-base font-medium" :style="{ color: 'var(--ink)' }">No transactions found</p>
        <p class="text-sm mt-1" :style="{ color: 'var(--ink-3)' }">Transactions will appear here once customers start transacting.</p>
      </div>

      <!-- Table -->
      <VCard v-else>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="border-b" :style="{ borderColor: 'var(--line)' }">
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider" :style="{ color: 'var(--ink-3)' }">From</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider" :style="{ color: 'var(--ink-3)' }">To</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider hidden md:table-cell" :style="{ color: 'var(--ink-3)' }">Type</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider hidden md:table-cell" :style="{ color: 'var(--ink-3)' }">Date</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider text-right" :style="{ color: 'var(--ink-3)' }">Amount</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="tx in transactions"
                :key="tx.id"
                class="row cursor-pointer border-b"
                :style="{ borderColor: 'var(--line)' }"
                @click="selectedTx = tx"
              >
                <td class="px-5 py-3.5">
                  <span class="font-mono text-xs" :style="{ color: 'var(--ink-2)' }">{{ tx.fromIban }}</span>
                </td>
                <td class="px-5 py-3.5">
                  <span class="font-mono text-xs" :style="{ color: 'var(--ink-2)' }">{{ tx.toIban }}</span>
                </td>
                <td class="px-5 py-3.5 hidden md:table-cell">
                  <VPill tone="neutral">{{ txLabel(tx.transactionType) }}</VPill>
                </td>
                <td class="px-5 py-3.5 text-sm hidden md:table-cell" :style="{ color: 'var(--ink-2)' }">{{ fmtTime(tx.timestamp) }}</td>
                <td class="px-5 py-3.5 text-right text-sm font-medium tabnum" :style="{ color: 'var(--credit)' }">
                  {{ eur(tx.amount) }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </VCard>

      <!-- Pagination -->
      <div v-if="totalPages > 1" class="flex items-center justify-between mt-4">
        <span class="text-xs" :style="{ color: 'var(--ink-3)' }">
          Page {{ page + 1 }} of {{ totalPages }} · {{ totalElements }} transactions
        </span>
        <div class="flex items-center gap-2">
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center lift"
            :style="{ background: 'var(--surface)', border: '1px solid var(--line-2)', color: page === 0 ? 'var(--ink-3)' : 'var(--ink)' }"
            :disabled="page === 0"
            @click="page--"
          ><ChevronLeft class="w-4 h-4" /></button>
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center lift"
            :style="{ background: 'var(--surface)', border: '1px solid var(--line-2)', color: page >= totalPages - 1 ? 'var(--ink-3)' : 'var(--ink)' }"
            :disabled="page >= totalPages - 1"
            @click="page++"
          ><ChevronRight class="w-4 h-4" /></button>
        </div>
      </div>
    </template>

    <!-- Transaction detail modal -->
    <VModal :open="!!selectedTx" @close="selectedTx = null">
      <div v-if="selectedTx" class="p-6">
        <div class="flex items-center justify-between mb-5">
          <h2 class="text-lg font-semibold" :style="{ color: 'var(--ink)' }">Transaction detail</h2>
          <button @click="selectedTx = null" :style="{ color: 'var(--ink-3)' }"><X class="w-5 h-5" /></button>
        </div>
        <div class="text-3xl font-display tabnum mb-1" style="font-weight: 400;" :style="{ color: 'var(--credit)' }">
          {{ eur(selectedTx.amount) }}
        </div>
        <p class="text-sm mb-6" :style="{ color: 'var(--ink-3)' }">{{ fmtTime(selectedTx.timestamp) }}</p>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">From</span>
            <span class="font-mono text-xs text-right" :style="{ color: 'var(--ink)' }">{{ selectedTx.fromIban }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">To</span>
            <span class="font-mono text-xs text-right" :style="{ color: 'var(--ink)' }">{{ selectedTx.toIban }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">Type</span>
            <VPill tone="neutral">{{ txLabel(selectedTx.transactionType) }}</VPill>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">Description</span>
            <span :style="{ color: 'var(--ink)' }">{{ selectedTx.description || '—' }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">Performed by</span>
            <span :style="{ color: 'var(--ink)' }">{{ selectedTx.performedBy || '—' }}</span>
          </div>
        </div>
      </div>
    </VModal>
  </EmployeeLayout>
</template>
