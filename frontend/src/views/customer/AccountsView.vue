<script setup>
import { ref, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { CreditCard, TrendingUp, Shield } from 'lucide-vue-next'

const accounts = ref([])
const loading = ref(true)

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <CustomerLayout>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Your Accounts</h1>
      <p class="text-sm text-gray-500 mt-1">Overview of all your accounts and limits.</p>
    </div>

    <div v-if="loading" class="grid gap-5 md:grid-cols-2">
      <div v-for="i in 2" :key="i" class="skeleton h-56 rounded-2xl"></div>
    </div>

    <div v-else class="grid gap-5 md:grid-cols-2">
      <div
        v-for="account in accounts"
        :key="account.id"
        class="relative overflow-hidden rounded-2xl border border-white/[0.06] bg-[#0D0D14] p-6 hover:border-white/10 transition-all duration-200 group"
      >
        <!-- Top accent bar -->
        <div class="absolute top-0 left-0 right-0 h-px" :class="account.accountType === 'CHECKING' ? 'bg-gradient-to-r from-transparent via-[#7B61FF]/50 to-transparent' : 'bg-gradient-to-r from-transparent via-[#00D9A3]/50 to-transparent'"></div>

        <div class="flex items-start justify-between mb-6">
          <div class="flex items-center gap-3.5">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center shadow-lg"
              :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/15 text-[#7B61FF] shadow-[#7B61FF]/10' : 'bg-[#00D9A3]/15 text-[#00D9A3] shadow-[#00D9A3]/10'">
              <CreditCard class="w-5 h-5" />
            </div>
            <div>
              <p class="font-semibold text-white capitalize">{{ account.accountType.toLowerCase() }} Account</p>
              <p class="text-xs text-gray-600 font-mono mt-0.5">{{ account.iban }}</p>
            </div>
          </div>
          <span class="text-xs font-medium px-2.5 py-1 rounded-full"
            :class="account.active ? 'bg-[#00D9A3]/10 text-[#00D9A3]' : 'bg-[#FF5E5B]/10 text-[#FF5E5B]'">
            {{ account.active ? 'Active' : 'Closed' }}
          </span>
        </div>

        <div class="mb-6">
          <p class="text-xs text-gray-600 mb-1">Available Balance</p>
          <p class="text-3xl font-bold tabular-nums text-white">{{ eur(account.balance) }}</p>
        </div>

        <div class="grid grid-cols-2 gap-3 pt-4 border-t border-white/[0.05]">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-lg bg-white/[0.04] flex items-center justify-center flex-shrink-0">
              <TrendingUp class="w-3.5 h-3.5 text-gray-500" />
            </div>
            <div>
              <p class="text-[10px] text-gray-600 leading-none mb-0.5">Daily Limit</p>
              <p class="text-sm font-semibold text-white">{{ eur(account.dailyLimit) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-lg bg-white/[0.04] flex items-center justify-center flex-shrink-0">
              <Shield class="w-3.5 h-3.5 text-gray-500" />
            </div>
            <div>
              <p class="text-[10px] text-gray-600 leading-none mb-0.5">Min Balance</p>
              <p class="text-sm font-semibold text-white">{{ eur(account.absoluteLimit) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
