<script setup>
import { ref, onMounted, computed } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Search, ChevronRight, History, Wallet, TrendingUp } from 'lucide-vue-next'

const accounts = ref([])
const recentTransactions = ref([])
const loading = ref(true)

const totalBalance = computed(() => accounts.value.reduce((s, a) => s + parseFloat(a.balance), 0))
const checkingAccount = computed(() => accounts.value.find(a => a.accountType === 'CHECKING'))

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}
function shortDate(ts) {
  return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' })
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
  { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer', accent: 'bg-[#7B61FF] shadow-[#7B61FF]/30' },
  { icon: History, label: 'History', path: '/customer/transactions', accent: 'bg-[#1C1C2E]' },
  { icon: Search, label: 'Find IBAN', path: '/customer/find', accent: 'bg-[#1C1C2E]' },
  { icon: Wallet, label: 'ATM', path: '/atm', accent: 'bg-[#1C1C2E]' },
]
</script>

<template>
  <CustomerLayout>
    <!-- Skeleton -->
    <div v-if="loading" class="space-y-6 mt-2">
      <div class="skeleton h-24 w-64 rounded-2xl"></div>
      <div class="flex gap-4"><div v-for="i in 2" :key="i" class="skeleton h-44 w-72 rounded-2xl flex-shrink-0"></div></div>
      <div class="skeleton h-40 rounded-2xl"></div>
    </div>

    <template v-else>
      <!-- Hero balance -->
      <div class="mt-2 mb-8">
        <p class="text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">Total Balance</p>
        <h1 class="text-5xl md:text-6xl font-bold tracking-tight tabular-nums text-white leading-none mb-3">
          {{ eur(totalBalance) }}
        </h1>
        <div class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-[#00D9A3]/10 border border-[#00D9A3]/20 text-[#00D9A3] text-xs font-semibold">
          <TrendingUp class="w-3.5 h-3.5" />
          {{ accounts.length }} active account{{ accounts.length !== 1 ? 's' : '' }}
        </div>
      </div>

      <!-- Account cards -->
      <div class="flex gap-4 mb-8 overflow-x-auto hide-scrollbar -mx-5 px-5 md:mx-0 md:px-0 pb-2">
        <div
          v-for="account in accounts"
          :key="account.id"
          class="flex-none w-72 h-44 rounded-2xl p-5 flex flex-col justify-between relative overflow-hidden cursor-pointer hover:-translate-y-1 transition-all duration-300 shadow-2xl select-none"
          :class="account.accountType === 'CHECKING'
            ? 'bg-gradient-to-br from-[#3D2B8A] via-[#2A1B69] to-[#14103A] shadow-[#3D2B8A]/30'
            : 'bg-gradient-to-br from-[#1E2A20] via-[#16201A] to-[#0C1410] shadow-black/50'"
        >
          <!-- Decorative circles -->
          <div class="absolute -top-8 -right-8 w-32 h-32 rounded-full opacity-20" :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]' : 'bg-[#00D9A3]'"></div>
          <div class="absolute -bottom-6 -left-6 w-24 h-24 rounded-full opacity-10" :class="account.accountType === 'CHECKING' ? 'bg-[#5C45CC]' : 'bg-[#00A880]'"></div>

          <div class="relative">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-semibold uppercase tracking-wider opacity-60 text-white">{{ account.accountType.toLowerCase() }}</span>
              <div class="w-6 h-6 rounded-full opacity-40 flex items-center justify-center" :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]' : 'bg-[#00D9A3]'">
                <div class="w-2.5 h-2.5 rounded-full bg-white"></div>
              </div>
            </div>
            <p class="text-2xl font-bold tabular-nums text-white leading-tight">{{ eur(account.balance) }}</p>
          </div>

          <div class="relative space-y-1">
            <p class="font-mono text-xs tracking-widest text-white/40 leading-none">
              {{ account.iban.slice(0, 4) }} {{ account.iban.slice(4, 8) }} ···· {{ account.iban.slice(-4) }}
            </p>
            <div class="flex justify-between text-[10px] text-white/30 font-medium">
              <span>Daily limit: {{ eur(account.dailyLimit) }}</span>
              <span>Min: {{ eur(account.absoluteLimit) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick actions -->
      <div class="grid grid-cols-4 gap-3 mb-8">
        <RouterLink
          v-for="action in quickActions"
          :key="action.path"
          :to="action.path"
          class="flex flex-col items-center gap-2 group"
        >
          <div
            class="w-13 h-13 md:w-14 md:h-14 rounded-2xl flex items-center justify-center transition-all duration-200 group-hover:scale-110 group-hover:shadow-lg shadow-md"
            :class="action.accent"
          >
            <component :is="action.icon" class="w-5 h-5 text-white" />
          </div>
          <span class="text-[11px] font-medium text-gray-500 group-hover:text-white transition-colors">{{ action.label }}</span>
        </RouterLink>
      </div>

      <!-- Recent transactions -->
      <div>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-semibold text-white">Recent Activity</h3>
          <RouterLink to="/customer/transactions" class="text-xs font-medium text-[#7B61FF] hover:text-[#9B81FF] flex items-center gap-0.5 transition-colors">
            View all <ChevronRight class="w-3.5 h-3.5" />
          </RouterLink>
        </div>

        <div class="bg-[#0D0D14] rounded-2xl border border-white/[0.05] overflow-hidden">
          <div v-if="recentTransactions.length > 0" class="divide-y divide-white/[0.04]">
            <div
              v-for="tx in recentTransactions"
              :key="tx.id"
              class="flex items-center gap-3.5 px-4 py-3.5 hover:bg-white/[0.02] transition-colors cursor-pointer"
            >
              <div
                class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                :class="tx.fromIban === checkingAccount?.iban ? 'bg-[#FF5E5B]/10 text-[#FF5E5B]' : 'bg-[#00D9A3]/10 text-[#00D9A3]'"
              >
                <ArrowUpRight v-if="tx.fromIban === checkingAccount?.iban" class="w-4 h-4" />
                <ArrowDownLeft v-else class="w-4 h-4" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-white truncate">{{ tx.description || tx.type }}</p>
                <p class="text-[11px] text-gray-600 mt-0.5">{{ tx.type }} · {{ shortDate(tx.timestamp) }}</p>
              </div>
              <span
                class="text-sm font-semibold tabular-nums"
                :class="tx.fromIban === checkingAccount?.iban ? 'text-gray-300' : 'text-[#00D9A3]'"
              >
                {{ tx.fromIban === checkingAccount?.iban ? '−' : '+' }}{{ eur(tx.amount) }}
              </span>
            </div>
          </div>
          <div v-else class="flex flex-col items-center justify-center py-12 text-gray-600">
            <History class="w-7 h-7 mb-2 opacity-30" />
            <p class="text-sm">No transactions yet</p>
          </div>
        </div>
      </div>
    </template>
  </CustomerLayout>
</template>
