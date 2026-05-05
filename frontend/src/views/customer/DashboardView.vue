<script setup>
import { ref, computed, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { Eye, EyeOff, Send, Search, AlignLeft, ArrowRight } from 'lucide-vue-next'
import CustomerLayout from '../../components/CustomerLayout.vue'
import ActivityRow from '../../components/ActivityRow.vue'
import VCard from '../../components/ui/VCard.vue'
import CopyChip from '../../components/ui/CopyChip.vue'
import { getMyAccounts } from '../../services/accounts'
import { getTransactions } from '../../services/transactions'

const accounts = ref([])
const transactions = ref([])
const loading = ref(true)
const masked = ref(false)

const myIbans = computed(() => accounts.value.map(a => a.iban))

const ownerName = computed(() => {
  const u = accounts.value[0]?.user
  if (!u) return 'there'
  return u.firstName || 'there'
})

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}

const totalBalance = computed(() =>
  accounts.value.reduce((sum, a) => sum + (parseFloat(a.balance) || 0), 0)
)
const totalDisplay = computed(() => masked.value ? '€ ••••••' : eur(totalBalance.value))

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return 'Good morning'
  if (h < 18) return 'Good afternoon'
  return 'Good evening'
})
const today = computed(() =>
  new Date().toLocaleDateString('en-GB', { weekday: 'long', day: 'numeric', month: 'long' })
)

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data

    // Load recent transactions combining all accounts
    const txResults = await Promise.all(
      data.map(a => getTransactions({ iban: a.iban, size: 10, sort: 'timestamp,desc' }).catch(() => ({ data: { content: [] } })))
    )
    const allTx = txResults.flatMap(r => r.data.content ?? [])
    // Deduplicate by id
    const seen = new Set()
    const unique = allTx.filter(tx => {
      if (seen.has(tx.id)) return false
      seen.add(tx.id)
      return true
    })
    unique.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
    transactions.value = unique.slice(0, 5)
  } catch {
    // errors handled per section below
  } finally {
    loading.value = false
  }
})

const quickActions = [
  { icon: Send,      label: 'Move money',      to: '/customer/transfer?mode=own',   desc: 'Between your accounts' },
  { icon: Send,      label: 'Send to someone', to: '/customer/transfer?mode=other', desc: 'External transfer' },
  { icon: Search,    label: 'Find IBAN',        to: '/customer/find',                desc: 'Look up a customer' },
  { icon: AlignLeft, label: 'Statement',        to: '/customer/transactions',         desc: 'Full history' },
]
</script>

<template>
  <CustomerLayout>
    <!-- Greeting -->
    <div class="fade-up mb-8">
      <p class="text-sm mb-1" :style="{ color: 'var(--ink-3)' }">{{ today }}</p>
      <h1 class="font-display" style="font-size: 36px; line-height: 1.1; font-weight: 400;" :style="{ color: 'var(--ink)' }">
        {{ greeting }}, {{ ownerName }}.
      </h1>
    </div>

    <!-- Loading skeletons -->
    <div v-if="loading" class="space-y-4">
      <div class="skeleton h-32 rounded-2xl" />
      <div class="grid grid-cols-2 gap-4">
        <div class="skeleton h-44 rounded-2xl" />
        <div class="skeleton h-44 rounded-2xl" />
      </div>
    </div>

    <template v-else>
      <!-- Balance hero -->
      <div
        class="fade-up delay-1 rounded-2xl p-8 mb-6"
        :style="{ background: 'var(--surface)', border: '1px solid var(--line)' }"
      >
        <div class="flex items-center gap-3 mb-2">
          <p class="text-sm font-medium" :style="{ color: 'var(--ink-3)' }">Total balance</p>
          <button
            class="w-7 h-7 rounded-lg flex items-center justify-center"
            :style="{ background: 'var(--surface-2)', color: 'var(--ink-3)' }"
            @click="masked = !masked"
            :aria-label="masked ? 'Show balance' : 'Hide balance'"
          >
            <EyeOff v-if="!masked" class="w-3.5 h-3.5" />
            <Eye v-else class="w-3.5 h-3.5" />
          </button>
        </div>
        <span
          class="font-display tabnum"
          style="font-size: 52px; line-height: 1; font-weight: 400;"
          :style="{ color: 'var(--ink)' }"
        >
          {{ totalDisplay }}
        </span>
        <p class="text-sm mt-3" :style="{ color: 'var(--ink-3)' }">
          Across {{ accounts.length }} account{{ accounts.length !== 1 ? 's' : '' }}
        </p>
      </div>

      <!-- Account Cards -->
      <div v-if="accounts.length" class="fade-up delay-2 grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <RouterLink
          v-for="(account, i) in accounts"
          :key="account.iban"
          :to="`/customer/account/${account.iban}`"
          class="no-underline"
        >
          <div
            class="rounded-2xl p-6 h-44 flex flex-col justify-between lift cursor-pointer"
            :class="i === 0 ? 'card-vault' : 'card-savings'"
          >
            <div>
              <p class="text-xs font-medium uppercase tracking-[.1em] opacity-70">{{ account.accountType }}</p>
            </div>
            <div>
              <div class="text-2xl font-display tabnum mb-2" style="font-weight: 400;">
                {{ masked ? '€ ••••' : eur(account.balance) }}
              </div>
              <CopyChip :value="account.iban" :light="i === 0" />
            </div>
          </div>
        </RouterLink>
      </div>

      <!-- No accounts state -->
      <div
        v-else
        class="fade-up delay-2 rounded-2xl border py-12 text-center mb-6"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No accounts found. Your accounts will appear here once approved.</p>
      </div>

      <!-- Quick Actions -->
      <div class="fade-up delay-3 grid grid-cols-2 sm:grid-cols-4 gap-3 mb-6">
        <RouterLink
          v-for="action in quickActions"
          :key="action.label"
          :to="action.to"
          class="qa-tile no-underline rounded-2xl p-4 flex flex-col gap-2 lift"
        >
          <div
            class="w-8 h-8 rounded-lg flex items-center justify-center"
            :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
          >
            <component :is="action.icon" class="w-4 h-4" />
          </div>
          <div>
            <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ action.label }}</p>
            <p class="text-xs" :style="{ color: 'var(--ink-3)' }">{{ action.desc }}</p>
          </div>
        </RouterLink>
      </div>

      <!-- Recent Activity -->
      <div class="fade-up delay-4">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold" :style="{ color: 'var(--ink)' }">Recent activity</h2>
          <RouterLink
            to="/customer/transactions"
            class="flex items-center gap-1 text-sm font-medium no-underline"
            :style="{ color: 'var(--accent)' }"
          >View all <ArrowRight class="w-4 h-4" /></RouterLink>
        </div>

        <VCard>
          <div v-if="transactions.length" class="p-2">
            <ActivityRow
              v-for="tx in transactions"
              :key="tx.id"
              :tx="tx"
              :my-ibans="myIbans"
              :masked="masked"
            />
          </div>
          <div v-else class="py-10 text-center">
            <AlignLeft class="w-8 h-8 mx-auto mb-2" :style="{ color: 'var(--ink-3)' }" />
            <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No transactions yet</p>
          </div>
        </VCard>
      </div>
    </template>
  </CustomerLayout>
</template>
