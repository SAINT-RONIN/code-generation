<script setup>
import { ref, onMounted, computed } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Search, ChevronRight, History, MapPin } from 'lucide-vue-next'

const accounts = ref([])
const recentTransactions = ref([])
const loading = ref(true)

const totalBalance = computed(() => accounts.value.reduce((s, a) => s + parseFloat(a.balance), 0))
const checkingAccount = computed(() => accounts.value.find(a => a.accountType === 'CHECKING'))

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }
function shortDate(ts) { return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) }

function sparkline() {
  return Array.from({ length: 12 }, () => Math.floor(Math.random() * 80) + 20)
}

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data
    if (checkingAccount.value) {
      const txRes = await getHistory(checkingAccount.value.iban, { size: 6, page: 0 })
      recentTransactions.value = txRes.data.content || []
    }
  } finally {
    loading.value = false
  }
})

const quickActions = [
  { icon: ArrowLeftRight, label: 'Transfer',  path: '/customer/transfer',     color: 'bg-[#7B61FF]' },
  { icon: History,        label: 'History',   path: '/customer/transactions', color: 'bg-[#2A2A35]' },
  { icon: Search,         label: 'Find IBAN', path: '/customer/find',         color: 'bg-[#2A2A35]' },
  { icon: MapPin,         label: 'ATM',       path: '/atm',                   color: 'bg-[#2A2A35]' },
]
</script>

<template>
  <CustomerLayout>

    <!-- Skeleton -->
    <div v-if="loading" class="space-y-8" aria-busy="true" aria-label="Loading dashboard">
      <div class="skeleton h-20 w-64 rounded-2xl"></div>
      <div class="flex gap-4">
        <div v-for="i in 2" :key="i" class="skeleton h-48 w-72 rounded-2xl flex-shrink-0"></div>
      </div>
      <div class="skeleton h-40 rounded-2xl"></div>
    </div>

    <template v-else>

      <!-- Balance hero -->
      <section class="mb-10 mt-4 md:mt-0 text-center md:text-left fade-up" aria-label="Account summary">
        <p class="text-gray-400 font-medium mb-2">Total Balance</p>
        <h1 class="text-5xl md:text-7xl font-bold tracking-tighter tabular-nums mb-3 text-white">
          {{ eur(totalBalance) }}
        </h1>
        <div
          class="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#00D9A3]/10 text-[#00D9A3] text-sm font-medium"
        >
          <ArrowUpRight class="w-4 h-4" aria-hidden="true" />
          <span>{{ accounts.length }} active account{{ accounts.length !== 1 ? 's' : '' }}</span>
        </div>
      </section>

      <!-- Account Cards -->
      <section class="mb-10 fade-up-1" aria-label="Your accounts">
        <div class="flex overflow-x-auto gap-4 pb-6 hide-scrollbar -mx-4 px-4 md:mx-0 md:px-0">
          <article
            v-for="account in accounts"
            :key="account.id"
            class="flex-none w-72 md:w-80 h-48 rounded-2xl p-5 flex flex-col justify-between relative overflow-hidden group hover:-translate-y-1 transition-transform duration-200 cursor-pointer shadow-xl"
            :class="account.accountType === 'CHECKING'
              ? 'bg-gradient-to-br from-[#2D1B69] to-[#1A1040]'
              : 'bg-gradient-to-br from-[#2A2A35] to-[#14141A]'"
            :aria-label="`${account.accountType.toLowerCase()} account, balance ${eur(account.balance)}`"
          >
            <div class="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none" aria-hidden="true" />

            <div>
              <p class="text-white/70 text-sm font-medium mb-1 capitalize">{{ account.accountType.toLowerCase() }} Account</p>
              <h3 class="text-2xl font-bold tabular-nums text-white">{{ eur(account.balance) }}</h3>
            </div>

            <div>
              <p class="font-mono text-sm tracking-widest text-white/50 mb-1">
                {{ account.iban.slice(0,4) }} {{ account.iban.slice(4,8) }} ···· {{ account.iban.slice(-4) }}
              </p>
              <div class="w-full h-8 flex items-end gap-0.5 opacity-40 group-hover:opacity-80 transition-opacity" aria-hidden="true">
                <div
                  v-for="(h, i) in sparkline()"
                  :key="i"
                  class="flex-1 bg-white/30 rounded-t-sm"
                  :style="`height:${h}%`"
                ></div>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- Quick Actions -->
      <section class="mb-10 fade-up-2" aria-label="Quick actions">
        <div class="grid grid-cols-4 gap-3 md:gap-6">
          <RouterLink
            v-for="action in quickActions"
            :key="action.path"
            :to="action.path"
            class="flex flex-col items-center gap-2 group"
            :aria-label="action.label"
          >
            <div
              class="w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform duration-150 group-hover:scale-105 shadow-lg"
              :class="action.color"
            >
              <component :is="action.icon" class="w-5 h-5 md:w-6 md:h-6 text-white" aria-hidden="true" />
            </div>
            <span class="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors text-center">{{ action.label }}</span>
          </RouterLink>
        </div>
      </section>

      <!-- Recent Transactions -->
      <section class="fade-up-3" aria-label="Recent activity">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-lg font-bold text-white">Recent Activity</h2>
          <RouterLink
            to="/customer/transactions"
            class="text-sm font-medium text-[#7B61FF] hover:text-[#907aff] flex items-center gap-1 transition-colors"
            aria-label="View all transactions"
          >
            View All <ChevronRight class="w-4 h-4" aria-hidden="true" />
          </RouterLink>
        </div>

        <div class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
          <ul v-if="recentTransactions.length > 0" class="divide-y divide-white/5" role="list" aria-label="Recent transactions">
            <li
              v-for="tx in recentTransactions"
              :key="tx.id"
              class="flex items-center gap-4 p-4 hover:bg-white/[0.02] transition-colors cursor-pointer"
            >
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
                :class="tx.fromIban === checkingAccount?.iban
                  ? 'text-[#FF5E5B] bg-[#FF5E5B]/10'
                  : 'text-[#00D9A3] bg-[#00D9A3]/10'"
                aria-hidden="true"
              >
                <ArrowUpRight v-if="tx.fromIban === checkingAccount?.iban" class="w-5 h-5" />
                <ArrowDownLeft v-else class="w-5 h-5" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-medium text-white truncate">{{ tx.description || tx.type }}</p>
                <p class="text-xs text-gray-500 capitalize">{{ tx.type.toLowerCase() }} · {{ shortDate(tx.timestamp) }}</p>
              </div>
              <div
                class="font-bold tabular-nums whitespace-nowrap"
                :class="tx.fromIban === checkingAccount?.iban ? 'text-white' : 'text-[#00D9A3]'"
                :aria-label="`${tx.fromIban === checkingAccount?.iban ? 'Sent' : 'Received'} ${eur(tx.amount)}`"
              >
                {{ tx.fromIban === checkingAccount?.iban ? '−' : '+' }}{{ eur(tx.amount) }}
              </div>
            </li>
          </ul>
          <div v-else class="p-8 text-center text-gray-500 flex flex-col items-center gap-3">
            <History class="w-8 h-8 opacity-20" aria-hidden="true" />
            <p>No recent transactions</p>
          </div>
        </div>
      </section>

    </template>
  </CustomerLayout>
</template>
