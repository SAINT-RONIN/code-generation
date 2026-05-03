<script setup>
import { ref, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'
import { CreditCard } from 'lucide-vue-next'

const accounts = ref([])

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
})
</script>

<template>
  <CustomerLayout>
    <div class="flex items-center justify-between mb-8">
      <h1 class="text-3xl font-bold text-white">Accounts</h1>
    </div>

    <div class="grid gap-6 md:grid-cols-2">
      <div
        v-for="account in accounts"
        :key="account.id"
        class="bg-[#14141A] border border-white/5 rounded-2xl p-6 relative overflow-hidden hover:border-white/10 transition-colors"
      >
        <div class="flex justify-between items-start mb-8">
          <div class="flex items-center gap-3">
            <div
              class="w-12 h-12 rounded-xl flex items-center justify-center"
              :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/10 text-[#7B61FF]' : 'bg-[#00D9A3]/10 text-[#00D9A3]'"
            >
              <CreditCard class="w-6 h-6" />
            </div>
            <div>
              <h3 class="font-medium text-lg text-white capitalize">{{ account.accountType.toLowerCase() }} Account</h3>
              <p class="text-sm text-gray-400 font-mono">{{ account.iban }}</p>
            </div>
          </div>
        </div>

        <div class="space-y-4">
          <div>
            <p class="text-sm text-gray-400 mb-1">Available Balance</p>
            <h4 class="text-3xl font-bold tabular-nums text-white">{{ formatEur(account.balance) }}</h4>
          </div>

          <div class="grid grid-cols-2 gap-4 pt-4 border-t border-white/5">
            <div>
              <p class="text-xs text-gray-500 mb-1">Daily Limit</p>
              <p class="font-medium text-white">{{ formatEur(account.dailyLimit) }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 mb-1">Absolute Limit</p>
              <p class="font-medium text-white">{{ formatEur(account.absoluteLimit) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
