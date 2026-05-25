<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Filter, X, ChevronLeft, ChevronRight, AlignLeft } from 'lucide-vue-next'
import CustomerLayout from '../../components/CustomerLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import ActivityRow from '../../components/ActivityRow.vue'
import VModal from '../../components/ui/VModal.vue'
import VBtn from '../../components/ui/VBtn.vue'
import { getMyAccounts } from '../../services/accounts'
import { getTransactions } from '../../services/transactions'
import { eur } from '../../utils/format'

const accounts = ref([])
const myIbans = computed(() => accounts.value.map(a => a.iban))

const transactions = ref([])
const totalPages = ref(0)
const totalElements = ref(0)
const page = ref(0)
const pageSize = 10
const loading = ref(true)
const error = ref('')
const showFilters = ref(false)
const selectedTx = ref(null)
const selectedIban = ref('')

const filters = ref({ amountMin: '', amountMax: '' })

async function loadTransactions() {
  loading.value = true
  error.value = ''
  try {
    const iban = selectedIban.value || myIbans.value[0]
    if (!iban) { transactions.value = []; loading.value = false; return }

    const params = { iban, page: page.value, size: pageSize, sort: 'timestamp,desc' }
    if (filters.value.amountMin) params.amountMin = filters.value.amountMin
    if (filters.value.amountMax) params.amountMax = filters.value.amountMax

    const { data } = await getTransactions(params)
    transactions.value = data.content ?? []
    totalPages.value = data.totalPages ?? 0
    totalElements.value = data.totalElements ?? 0
  } catch {
    error.value = 'Could not load transactions.'
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  selectedIban.value = data[0]?.iban ?? ''
  await loadTransactions()
})

watch(page, loadTransactions)

function applyFilters() { page.value = 0; loadTransactions() }
function clearFilters() { filters.value = { amountMin: '', amountMax: '' }; page.value = 0; loadTransactions() }

function fmtTime(ts) {
  if (!ts) return '—'
  return new Date(ts).toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <CustomerLayout>
    <VPageHeader eyebrow="Banking" title="Transactions">
      <template #right>
        <VBtn variant="secondary" size="sm" @click="showFilters = !showFilters">
          <Filter class="w-4 h-4" />
          {{ showFilters ? 'Hide' : 'Filters' }}
        </VBtn>
      </template>
    </VPageHeader>

    <!-- Account selector + filter bar -->
    <div class="flex flex-wrap gap-3 mb-4">
      <button
        v-for="acc in accounts"
        :key="acc.iban"
        class="h-8 px-3 text-xs font-medium rounded-lg transition-colors"
        :style="selectedIban === acc.iban
          ? { background: 'var(--accent)', color: 'var(--accent-ink)' }
          : { background: 'var(--surface-2)', color: 'var(--ink-2)' }"
        @click="selectedIban = acc.iban; page = 0; loadTransactions()"
      >{{ acc.accountType }} · {{ acc.iban.slice(-6) }}</button>
    </div>

    <div v-if="showFilters" class="mb-4 rounded-2xl border p-4" :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }">
      <div class="grid grid-cols-2 gap-3 mb-3">
        <div>
          <label class="block text-xs font-medium mb-1" :style="{ color: 'var(--ink-2)' }">Min amount (€)</label>
          <input v-model="filters.amountMin" type="number" placeholder="0"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
        <div>
          <label class="block text-xs font-medium mb-1" :style="{ color: 'var(--ink-2)' }">Max amount (€)</label>
          <input v-model="filters.amountMax" type="number" placeholder="∞"
            class="w-full h-9 px-3 text-sm rounded-lg border"
            :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
        </div>
      </div>
      <div class="flex gap-2">
        <VBtn variant="primary" size="sm" @click="applyFilters">Apply</VBtn>
        <VBtn variant="ghost" size="sm" @click="clearFilters"><X class="w-3.5 h-3.5" /> Clear</VBtn>
      </div>
    </div>

    <div v-if="error" class="mb-4 px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">
      {{ error }}
    </div>

    <!-- Skeleton -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 6" :key="i" class="skeleton h-14 rounded-xl" />
    </div>

    <template v-else>
      <div
        v-if="!transactions.length"
        class="rounded-2xl border py-16 text-center"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <AlignLeft class="w-8 h-8 mx-auto mb-2" :style="{ color: 'var(--ink-3)' }" />
        <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">No transactions yet</p>
        <p class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">Transactions for this account will appear here.</p>
      </div>

      <VCard v-else>
        <div class="p-2">
          <ActivityRow
            v-for="tx in transactions"
            :key="tx.id"
            :tx="tx"
            :my-ibans="myIbans"
            @click="selectedTx = tx"
          />
        </div>
      </VCard>

      <div v-if="totalPages > 1" class="flex items-center justify-between mt-4">
        <span class="text-xs" :style="{ color: 'var(--ink-3)' }">{{ totalElements }} total</span>
        <div class="flex items-center gap-2">
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center lift"
            :style="{ background: 'var(--surface)', border: '1px solid var(--line-2)', color: page === 0 ? 'var(--ink-3)' : 'var(--ink)' }"
            :disabled="page === 0" @click="page--"
          ><ChevronLeft class="w-4 h-4" /></button>
          <span class="text-xs tabnum" :style="{ color: 'var(--ink-2)' }">{{ page + 1 }} / {{ totalPages }}</span>
          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center lift"
            :style="{ background: 'var(--surface)', border: '1px solid var(--line-2)', color: page >= totalPages - 1 ? 'var(--ink-3)' : 'var(--ink)' }"
            :disabled="page >= totalPages - 1" @click="page++"
          ><ChevronRight class="w-4 h-4" /></button>
        </div>
      </div>
    </template>

    <!-- Detail modal -->
    <VModal :open="!!selectedTx" @close="selectedTx = null">
      <div v-if="selectedTx" class="p-6">
        <div class="flex items-center justify-between mb-5">
          <h2 class="text-lg font-semibold" :style="{ color: 'var(--ink)' }">Transaction detail</h2>
          <button @click="selectedTx = null" :style="{ color: 'var(--ink-3)' }"><X class="w-5 h-5" /></button>
        </div>
        <div
          class="text-3xl font-display tabnum mb-1"
          style="font-weight: 400;"
          :style="{ color: myIbans.includes(selectedTx.toIban) ? 'var(--credit)' : 'var(--debit)' }"
        >
          {{ myIbans.includes(selectedTx.toIban) ? '+ ' : '− ' }}{{ eur(selectedTx.amount) }}
        </div>
        <p class="text-sm mb-5" :style="{ color: 'var(--ink-3)' }">{{ fmtTime(selectedTx.timestamp) }}</p>
        <div class="space-y-3 text-sm">
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">From</span>
            <span class="font-mono text-xs" :style="{ color: 'var(--ink)' }">{{ selectedTx.fromIban }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">To</span>
            <span class="font-mono text-xs" :style="{ color: 'var(--ink)' }">{{ selectedTx.toIban }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">Description</span>
            <span :style="{ color: 'var(--ink)' }">{{ selectedTx.description || '—' }}</span>
          </div>
          <div class="flex justify-between gap-4">
            <span :style="{ color: 'var(--ink-3)' }">Type</span>
            <span :style="{ color: 'var(--ink)' }">{{ (selectedTx.transactionType ?? '').toLowerCase().replace(/_/g, ' ') }}</span>
          </div>
        </div>
      </div>
    </VModal>
  </CustomerLayout>
</template>
