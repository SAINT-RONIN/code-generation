<script setup>
import { ref, onMounted, computed } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Search, ChevronRight, History, Wallet } from 'lucide-vue-next'

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
  { icon: ArrowLeftRight, label: 'Transfer',  path: '/customer/transfer',      accent: '#7B61FF' },
  { icon: History,        label: 'History',   path: '/customer/transactions',  accent: null },
  { icon: Search,         label: 'Find IBAN', path: '/customer/find',          accent: null },
  { icon: Wallet,         label: 'ATM',       path: '/atm',                    accent: null },
]
</script>

<template>
  <CustomerLayout>

    <!-- Skeleton -->
    <div v-if="loading" class="space-y-8" aria-busy="true" aria-label="Loading dashboard">
      <div class="skeleton h-20 w-56 rounded-2xl"></div>
      <div class="flex gap-4">
        <div v-for="i in 2" :key="i" class="skeleton h-44 w-64 rounded-2xl flex-shrink-0"></div>
      </div>
      <div class="skeleton h-40 rounded-2xl"></div>
    </div>

    <template v-else>

      <!-- Balance hero -->
      <section class="mb-10 fade-up" aria-label="Account summary">
        <p class="text-xs font-semibold uppercase tracking-widest mb-3" style="color:#4A4A5E;">Total balance</p>
        <p class="text-5xl md:text-6xl font-bold tabular-nums tracking-tight text-white leading-none mb-4">
          {{ eur(totalBalance) }}
        </p>
        <div
          class="inline-flex items-center gap-2 px-3 py-1.5 rounded-full text-xs font-semibold"
          style="background:rgba(0,217,163,0.1); color:#00D9A3; border:1px solid rgba(0,217,163,0.18);"
        >
          <div class="w-1.5 h-1.5 rounded-full bg-[#00D9A3]" aria-hidden="true"></div>
          {{ accounts.length }} active account{{ accounts.length !== 1 ? 's' : '' }}
        </div>
      </section>

      <!-- Account cards -->
      <section class="mb-10 fade-up-1" aria-label="Your accounts">
        <div class="flex gap-4 overflow-x-auto hide-scrollbar -mx-5 px-5 md:mx-0 md:px-0 pb-2">
          <article
            v-for="account in accounts"
            :key="account.id"
            class="flex-none w-64 h-44 rounded-2xl p-5 flex flex-col justify-between relative overflow-hidden hover:-translate-y-1 transition-transform duration-200"
            :style="account.accountType === 'CHECKING'
              ? 'background:linear-gradient(145deg,#1F1550 0%,#130E38 100%);'
              : 'background:linear-gradient(145deg,#0B2018 0%,#061410 100%);'"
            :aria-label="`${account.accountType.toLowerCase()} account, balance ${eur(account.balance)}`"
          >
            <div class="flex items-center justify-between">
              <span
                class="text-[10px] font-bold uppercase tracking-widest"
                :style="account.accountType === 'CHECKING' ? 'color:rgba(123,97,255,0.7)' : 'color:rgba(0,217,163,0.7)'"
              >{{ account.accountType }}</span>
              <div class="flex gap-1" aria-hidden="true">
                <div class="w-4 h-3 rounded-sm" :style="account.accountType === 'CHECKING' ? 'background:rgba(123,97,255,0.5)' : 'background:rgba(0,217,163,0.5)'"></div>
                <div class="w-4 h-3 rounded-sm" :style="account.accountType === 'CHECKING' ? 'background:rgba(123,97,255,0.25)' : 'background:rgba(0,217,163,0.25)'"></div>
              </div>
            </div>

            <div>
              <p class="text-2xl font-bold tabular-nums text-white leading-tight">{{ eur(account.balance) }}</p>
            </div>

            <div>
              <p class="font-mono text-[11px] tracking-widest" style="color:rgba(255,255,255,0.3);">
                {{ account.iban.slice(0,4) }} {{ account.iban.slice(4,8) }} ···· {{ account.iban.slice(-4) }}
              </p>
            </div>
          </article>
        </div>
      </section>

      <!-- Quick actions -->
      <section class="mb-10 fade-up-2" aria-label="Quick actions">
        <div class="grid grid-cols-4 gap-3">
          <RouterLink
            v-for="action in quickActions"
            :key="action.path"
            :to="action.path"
            class="flex flex-col items-center gap-2.5 group"
            :aria-label="action.label"
          >
            <div
              class="w-14 h-14 rounded-2xl flex items-center justify-center transition-transform duration-150 group-hover:scale-105"
              :style="action.accent
                ? `background:${action.accent};`
                : 'background:rgba(255,255,255,0.06);'"
            >
              <component :is="action.icon" class="w-5 h-5 text-white" aria-hidden="true" />
            </div>
            <span class="text-[11px] font-medium text-center transition-colors" style="color:#6B6B7E;">{{ action.label }}</span>
          </RouterLink>
        </div>
      </section>

      <!-- Recent transactions -->
      <section class="fade-up-3" aria-label="Recent activity">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-sm font-semibold text-white">Recent activity</h2>
          <RouterLink
            to="/customer/transactions"
            class="text-xs font-medium flex items-center gap-0.5 transition-colors"
            style="color:#7B61FF;"
            aria-label="View all transactions"
          >
            View all <ChevronRight class="w-3.5 h-3.5" aria-hidden="true" />
          </RouterLink>
        </div>

        <div class="rounded-2xl overflow-hidden" style="background:#0E0E16; border:1px solid rgba(255,255,255,0.06);">
          <ul v-if="recentTransactions.length > 0" role="list" aria-label="Recent transactions">
            <li
              v-for="tx in recentTransactions"
              :key="tx.id"
              class="flex items-center gap-3.5 px-4 py-3.5 transition-colors"
              style="border-bottom:1px solid rgba(255,255,255,0.04);"
              :style="{ borderBottom: '1px solid rgba(255,255,255,0.04)' }"
            >
              <div
                class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
                :style="tx.fromIban === checkingAccount?.iban
                  ? 'background:rgba(255,94,91,0.1); color:#FF5E5B;'
                  : 'background:rgba(0,217,163,0.1); color:#00D9A3;'"
                aria-hidden="true"
              >
                <ArrowUpRight v-if="tx.fromIban === checkingAccount?.iban" class="w-4 h-4" />
                <ArrowDownLeft v-else class="w-4 h-4" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium text-white truncate">{{ tx.description || tx.type }}</p>
                <p class="text-[11px] mt-0.5 capitalize" style="color:#4A4A5E;">{{ tx.type.toLowerCase() }} · {{ shortDate(tx.timestamp) }}</p>
              </div>
              <span
                class="text-sm font-semibold tabular-nums flex-shrink-0"
                :style="tx.fromIban === checkingAccount?.iban ? 'color:#6B6B7E' : 'color:#00D9A3'"
                :aria-label="`${tx.fromIban === checkingAccount?.iban ? 'Sent' : 'Received'} ${eur(tx.amount)}`"
              >
                {{ tx.fromIban === checkingAccount?.iban ? '−' : '+' }}{{ eur(tx.amount) }}
              </span>
            </li>
          </ul>
          <div v-else class="flex flex-col items-center justify-center py-12" style="color:#4A4A5E;">
            <History class="w-7 h-7 mb-2 opacity-40" aria-hidden="true" />
            <p class="text-sm">No transactions yet</p>
          </div>
        </div>
      </section>

    </template>
  </CustomerLayout>
</template>
