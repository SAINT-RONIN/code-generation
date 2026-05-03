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

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }
function shortDate(ts) { return new Date(ts).toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) }

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
  { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer', bg: 'linear-gradient(135deg, #7B61FF, #5C45CC)', shadow: 'rgba(123,97,255,0.4)' },
  { icon: History, label: 'History', path: '/customer/transactions', bg: 'rgba(255,255,255,0.06)', shadow: 'transparent' },
  { icon: Search, label: 'Find IBAN', path: '/customer/find', bg: 'rgba(255,255,255,0.06)', shadow: 'transparent' },
  { icon: Wallet, label: 'ATM', path: '/atm', bg: 'rgba(255,255,255,0.06)', shadow: 'transparent' },
]
</script>

<template>
  <CustomerLayout>
    <!-- Skeleton -->
    <div v-if="loading" class="space-y-6 mt-2">
      <div class="skeleton h-28 w-72 rounded-2xl"></div>
      <div class="flex gap-4">
        <div v-for="i in 2" :key="i" class="skeleton h-48 w-72 rounded-2xl flex-shrink-0"></div>
      </div>
      <div class="skeleton h-44 rounded-2xl"></div>
    </div>

    <template v-else>
      <!-- Hero balance -->
      <div class="mt-2 mb-10 animate-fade-up">
        <p class="text-xs font-semibold text-gray-600 uppercase tracking-widest mb-3">Total Balance</p>
        <h1 class="text-5xl md:text-6xl font-bold tracking-tight tabular-nums leading-none mb-4 gradient-text">
          {{ eur(totalBalance) }}
        </h1>
        <div class="flex items-center gap-3">
          <div class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold" style="background: rgba(0,217,163,0.1); border: 1px solid rgba(0,217,163,0.2); color: #00D9A3;">
            <TrendingUp class="w-3.5 h-3.5" />
            {{ accounts.length }} active account{{ accounts.length !== 1 ? 's' : '' }}
          </div>
          <div class="w-1.5 h-1.5 rounded-full bg-[#00D9A3] animate-pulse"></div>
        </div>
      </div>

      <!-- Account cards -->
      <div class="flex gap-4 mb-10 overflow-x-auto hide-scrollbar -mx-5 px-5 md:mx-0 md:px-0 pb-2 animate-fade-up-1">
        <div
          v-for="account in accounts"
          :key="account.id"
          class="flex-none w-72 h-48 rounded-2xl p-5 flex flex-col justify-between relative overflow-hidden cursor-pointer hover:-translate-y-1.5 transition-all duration-300 card-shine select-none"
          :style="account.accountType === 'CHECKING'
            ? 'background: linear-gradient(135deg, #2D1F6B 0%, #1C1347 50%, #0F0B2E 100%); box-shadow: 0 20px 60px rgba(123,97,255,0.25), 0 0 0 1px rgba(123,97,255,0.2);'
            : 'background: linear-gradient(135deg, #0F2A20 0%, #0A1D16 50%, #060F0C 100%); box-shadow: 0 20px 60px rgba(0,217,163,0.15), 0 0 0 1px rgba(0,217,163,0.15);'"
        >
          <!-- Glow orb -->
          <div class="absolute -top-12 -right-12 w-40 h-40 rounded-full blur-2xl opacity-30"
            :style="account.accountType === 'CHECKING' ? 'background: #7B61FF' : 'background: #00D9A3'"></div>
          <div class="absolute -bottom-8 -left-8 w-28 h-28 rounded-full blur-2xl opacity-20"
            :style="account.accountType === 'CHECKING' ? 'background: #5C45CC' : 'background: #00A880'"></div>

          <!-- Card chip -->
          <div class="relative flex items-center justify-between">
            <div class="flex gap-0.5">
              <div class="w-5 h-4 rounded-sm opacity-60" :style="account.accountType === 'CHECKING' ? 'background: #7B61FF' : 'background: #00D9A3'"></div>
              <div class="w-5 h-4 rounded-sm opacity-30" :style="account.accountType === 'CHECKING' ? 'background: #a89bff' : 'background: #80ffe8'"></div>
            </div>
            <span class="text-[10px] font-bold uppercase tracking-widest text-white/40">{{ account.accountType }}</span>
          </div>

          <!-- Balance -->
          <div class="relative">
            <p class="text-2xl font-bold tabular-nums text-white leading-tight">{{ eur(account.balance) }}</p>
          </div>

          <!-- IBAN + limits -->
          <div class="relative space-y-1.5">
            <p class="font-mono text-xs tracking-widest text-white/35">
              {{ account.iban.slice(0, 4) }} {{ account.iban.slice(4, 8) }} ···· {{ account.iban.slice(-4) }}
            </p>
            <div class="flex justify-between text-[10px] text-white/25 font-medium">
              <span>Daily {{ eur(account.dailyLimit) }}</span>
              <span>Min {{ eur(account.absoluteLimit) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick actions -->
      <div class="grid grid-cols-4 gap-3 mb-10 animate-fade-up-2">
        <RouterLink
          v-for="action in quickActions"
          :key="action.path"
          :to="action.path"
          class="flex flex-col items-center gap-2.5 group"
        >
          <div
            class="w-14 h-14 rounded-2xl flex items-center justify-center transition-all duration-200 group-hover:scale-110"
            :style="`background: ${action.bg}; box-shadow: 0 8px 24px ${action.shadow};`"
          >
            <component :is="action.icon" class="w-5 h-5 text-white" />
          </div>
          <span class="text-[11px] font-medium text-gray-500 group-hover:text-white transition-colors">{{ action.label }}</span>
        </RouterLink>
      </div>

      <!-- Recent transactions -->
      <div class="animate-fade-up-3">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-base font-semibold text-white">Recent Activity</h3>
          <RouterLink to="/customer/transactions" class="text-xs font-medium flex items-center gap-0.5 transition-colors" style="color: #7B61FF;">
            View all <ChevronRight class="w-3.5 h-3.5" />
          </RouterLink>
        </div>

        <div class="rounded-2xl overflow-hidden card-shine" style="background: #0D0D14; border: 1px solid rgba(255,255,255,0.05);">
          <div v-if="recentTransactions.length > 0" class="divide-y" style="border-color: rgba(255,255,255,0.04);">
            <div
              v-for="tx in recentTransactions"
              :key="tx.id"
              class="flex items-center gap-3.5 px-4 py-3.5 transition-colors cursor-pointer"
              style="transition: background 0.15s;"
              @mouseenter="$event.currentTarget.style.background='rgba(255,255,255,0.02)'"
              @mouseleave="$event.currentTarget.style.background=''"
            >
              <div
                class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                :style="tx.fromIban === checkingAccount?.iban
                  ? 'background: rgba(255,94,91,0.1); color: #FF5E5B;'
                  : 'background: rgba(0,217,163,0.1); color: #00D9A3;'"
              >
                <ArrowUpRight v-if="tx.fromIban === checkingAccount?.iban" class="w-4 h-4" />
                <ArrowDownLeft v-else class="w-4 h-4" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-white truncate">{{ tx.description || tx.type }}</p>
                <p class="text-[11px] text-gray-600 mt-0.5 capitalize">{{ tx.type.toLowerCase() }} · {{ shortDate(tx.timestamp) }}</p>
              </div>
              <span
                class="text-sm font-semibold tabular-nums"
                :style="tx.fromIban === checkingAccount?.iban ? 'color: #9ca3af;' : 'color: #00D9A3;'"
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
