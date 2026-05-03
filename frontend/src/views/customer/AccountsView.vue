<script setup>
import { ref, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { CreditCard } from 'lucide-vue-next'

const accounts = ref([])
const loading = ref(true)

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }

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
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-3xl font-bold text-white">Accounts</h1>
    </div>

    <div v-if="loading" class="grid gap-6 md:grid-cols-2" aria-busy="true" aria-label="Loading accounts">
      <div v-for="i in 2" :key="i" class="skeleton h-56 rounded-2xl"></div>
    </div>

    <div v-else class="grid gap-6 md:grid-cols-2">
      <article
        v-for="account in accounts"
        :key="account.id"
        class="bg-[#14141A] border border-white/5 rounded-2xl p-6 relative overflow-hidden hover:border-white/10 transition-colors"
        :aria-label="`${account.accountType.toLowerCase()} account`"
      >
        <div class="flex justify-between items-start mb-8">
          <div class="flex items-center gap-3">
            <div
              class="w-12 h-12 rounded-xl flex items-center justify-center"
              :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/10 text-[#7B61FF]' : 'bg-[#00D9A3]/10 text-[#00D9A3]'"
            >
              <CreditCard class="w-6 h-6" aria-hidden="true" />
            </div>
            <div>
              <h3 class="font-medium capitalize text-lg text-white">{{ account.accountType.toLowerCase() }} Account</h3>
              <p class="text-sm text-gray-400 font-mono">{{ account.iban }}</p>
            </div>
          </div>
          <span
            class="text-[11px] font-semibold px-2.5 py-1 rounded-full"
            :class="account.active
              ? 'bg-[#00D9A3]/10 text-[#00D9A3]'
              : 'bg-[#FF5E5B]/10 text-[#FF5E5B]'"
            :aria-label="`Account status: ${account.active ? 'active' : 'closed'}`"
          >
            {{ account.active ? 'Active' : 'Closed' }}
          </span>
        </div>

        <div class="space-y-4">
          <div>
            <p class="text-sm text-gray-400 mb-1">Available Balance</p>
            <h4 class="text-3xl font-bold tabular-nums text-white">{{ eur(account.balance) }}</h4>
          </div>

          <div class="grid grid-cols-2 gap-4 pt-4 border-t border-white/5">
            <div>
              <p class="text-xs text-gray-500 mb-1">Daily Limit</p>
              <p class="font-medium text-white">{{ eur(account.dailyLimit) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">Min. Balance</p>
              <p class="font-medium text-white">{{ eur(account.absoluteLimit) }}</p>
            </div>
          </div>
        </div>
      </article>
    </div>
  </CustomerLayout>
</template>
