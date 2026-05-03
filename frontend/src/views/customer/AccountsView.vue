<script setup>
import { ref, onMounted } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { getMyAccounts } from '../../services/accounts'

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
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Your accounts</h1>
      <p class="text-sm mt-1" style="color:#6B6B7E;">Balances, limits, and account details.</p>
    </div>

    <div v-if="loading" class="grid gap-4 md:grid-cols-2" aria-busy="true" aria-label="Loading accounts">
      <div v-for="i in 2" :key="i" class="skeleton h-52 rounded-2xl"></div>
    </div>

    <div v-else class="grid gap-4 md:grid-cols-2">
      <article
        v-for="account in accounts"
        :key="account.id"
        class="rounded-2xl p-6"
        style="background:#0E0E16; border:1px solid rgba(255,255,255,0.07);"
        :aria-label="`${account.accountType.toLowerCase()} account`"
      >
        <!-- Header -->
        <div class="flex items-start justify-between mb-6">
          <div>
            <p class="text-xs font-semibold uppercase tracking-widest mb-1" style="color:#4A4A5E;">
              {{ account.accountType.toLowerCase() }}
            </p>
            <p class="font-mono text-xs" style="color:#6B6B7E;">{{ account.iban }}</p>
          </div>
          <span
            class="text-[11px] font-semibold px-2.5 py-1 rounded-full"
            :style="account.active
              ? 'background:rgba(0,217,163,0.1); color:#00D9A3; border:1px solid rgba(0,217,163,0.18);'
              : 'background:rgba(255,94,91,0.1); color:#FF5E5B; border:1px solid rgba(255,94,91,0.18);'"
            :aria-label="`Account status: ${account.active ? 'active' : 'closed'}`"
          >
            {{ account.active ? 'Active' : 'Closed' }}
          </span>
        </div>

        <!-- Balance -->
        <div class="mb-6">
          <p class="text-xs font-medium mb-1" style="color:#4A4A5E;">Available balance</p>
          <p class="text-3xl font-bold tabular-nums text-white">{{ eur(account.balance) }}</p>
        </div>

        <!-- Limits -->
        <div class="grid grid-cols-2 gap-4 pt-5" style="border-top:1px solid rgba(255,255,255,0.06);">
          <div>
            <p class="text-[11px] font-medium mb-1" style="color:#4A4A5E;">Daily limit</p>
            <p class="text-sm font-semibold text-white tabular-nums">{{ eur(account.dailyLimit) }}</p>
          </div>
          <div>
            <p class="text-[11px] font-medium mb-1" style="color:#4A4A5E;">Min. balance</p>
            <p class="text-sm font-semibold text-white tabular-nums">{{ eur(account.absoluteLimit) }}</p>
          </div>
        </div>
      </article>
    </div>
  </CustomerLayout>
</template>
