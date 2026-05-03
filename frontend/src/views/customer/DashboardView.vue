<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { getHistory } from '../../services/transactions'
import { ArrowUpRight, ArrowDownLeft, ArrowLeftRight, Search, ChevronRight, History } from 'lucide-vue-next'

const router = useRouter()
const accounts = ref([])
const recentTransactions = ref([])

const totalBalance = computed(() =>
  accounts.value.reduce((sum, a) => sum + parseFloat(a.balance), 0)
)

const checkingAccount = computed(() => accounts.value.find(a => a.accountType === 'CHECKING'))

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

function formatDate(ts) {
  return new Date(ts).toLocaleDateString('nl-NL')
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  if (checkingAccount.value) {
    const txRes = await getHistory(checkingAccount.value.iban, { size: 5 })
    recentTransactions.value = txRes.data.content || []
  }
})
</script>

<template>
  <CustomerLayout>
    <div class="mb-10 text-center md:text-left mt-4 md:mt-0">
      <p class="text-gray-400 font-medium mb-2">Total Balance</p>
      <h1 class="text-5xl md:text-7xl font-bold tracking-tighter tabular-nums mb-3 text-white">
        {{ formatEur(totalBalance) }}
      </h1>
    </div>

    <div class="flex overflow-x-auto gap-4 pb-6 -mx-4 px-4 md:mx-0 md:px-0 mb-10">
      <div
        v-for="account in accounts"
        :key="account.id"
        class="flex-none w-72 md:w-80 h-48 rounded-2xl p-5 flex flex-col justify-between relative overflow-hidden hover:-translate-y-1 transition-transform cursor-pointer shadow-xl"
        :class="account.accountType === 'CHECKING' ? 'bg-gradient-to-br from-[#2D1B69] to-[#1A1040]' : 'bg-gradient-to-br from-[#2A2A35] to-[#14141A]'"
      >
        <div class="absolute top-0 right-0 w-32 h-32 bg-white/5 rounded-full blur-2xl -mr-10 -mt-10 pointer-events-none" />
        <div>
          <p class="text-white/70 text-sm font-medium mb-1 capitalize">{{ account.accountType.toLowerCase() }} Account</p>
          <h3 class="text-2xl font-bold tabular-nums text-white">{{ formatEur(account.balance) }}</h3>
        </div>
        <p class="font-mono text-sm tracking-widest text-white/50">{{ account.iban }}</p>
      </div>
    </div>

    <div class="grid grid-cols-4 gap-3 md:gap-6 mb-10">
      <RouterLink to="/customer/transfer" class="flex flex-col items-center gap-2 group">
        <div class="w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-105 shadow-lg bg-[#7B61FF]">
          <ArrowLeftRight class="w-6 h-6 text-white" />
        </div>
        <span class="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors">Transfer</span>
      </RouterLink>
      <RouterLink to="/customer/transactions" class="flex flex-col items-center gap-2 group">
        <div class="w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-105 shadow-lg bg-[#2A2A35]">
          <History class="w-6 h-6 text-white" />
        </div>
        <span class="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors">History</span>
      </RouterLink>
      <RouterLink to="/customer/find" class="flex flex-col items-center gap-2 group">
        <div class="w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-105 shadow-lg bg-[#2A2A35]">
          <Search class="w-6 h-6 text-white" />
        </div>
        <span class="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors">Find IBAN</span>
      </RouterLink>
      <RouterLink to="/atm" class="flex flex-col items-center gap-2 group">
        <div class="w-14 h-14 md:w-16 md:h-16 rounded-2xl flex items-center justify-center transition-transform group-hover:scale-105 shadow-lg bg-[#2A2A35]">
          <ArrowUpRight class="w-6 h-6 text-white" />
        </div>
        <span class="text-xs md:text-sm font-medium text-gray-400 group-hover:text-white transition-colors">ATM</span>
      </RouterLink>
    </div>

    <div>
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-bold text-white">Recent Activity</h3>
        <RouterLink to="/customer/transactions" class="text-sm font-medium text-[#7B61FF] hover:text-[#907aff] flex items-center gap-1">
          View All <ChevronRight class="w-4 h-4" />
        </RouterLink>
      </div>

      <div class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
        <div v-if="recentTransactions.length > 0" class="divide-y divide-white/5">
          <div
            v-for="tx in recentTransactions"
            :key="tx.id"
            class="flex items-center gap-4 p-4 hover:bg-white/[0.02] transition-colors"
          >
            <div
              class="w-10 h-10 rounded-full flex items-center justify-center flex-shrink-0"
              :class="tx.fromIban === checkingAccount?.iban ? 'text-[#FF5E5B] bg-[#FF5E5B]/10' : 'text-[#00D9A3] bg-[#00D9A3]/10'"
            >
              <ArrowUpRight v-if="tx.fromIban === checkingAccount?.iban" class="w-5 h-5" />
              <ArrowDownLeft v-else class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <p class="font-medium text-white truncate">{{ tx.description || tx.type }}</p>
              <p class="text-xs text-gray-500 capitalize">{{ tx.type }} · {{ formatDate(tx.timestamp) }}</p>
            </div>
            <div
              class="font-bold tabular-nums whitespace-nowrap"
              :class="tx.fromIban === checkingAccount?.iban ? 'text-white' : 'text-[#00D9A3]'"
            >
              {{ tx.fromIban === checkingAccount?.iban ? '-' : '+' }}{{ formatEur(tx.amount) }}
            </div>
          </div>
        </div>
        <div v-else class="p-8 text-center text-gray-500 flex flex-col items-center gap-3">
          <History class="w-8 h-8 opacity-20" />
          <p>No recent transactions</p>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
