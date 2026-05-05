<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { ArrowLeft, AlignLeft } from 'lucide-vue-next'
import CustomerLayout from '../../components/CustomerLayout.vue'
import ActivityRow from '../../components/ActivityRow.vue'
import VCard from '../../components/ui/VCard.vue'
import CopyChip from '../../components/ui/CopyChip.vue'
import { getMyAccounts } from '../../services/accounts'
import { getTransactions } from '../../services/transactions'

const route = useRoute()
const accountIban = computed(() => route.params.iban)

const account = ref(null)
const allIbans = ref([])
const transactions = ref([])
const loading = ref(true)
const error = ref('')

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    allIbans.value = data.map(a => a.iban)
    account.value = data.find(a => a.iban === accountIban.value) ?? data[0] ?? null

    if (account.value) {
      const txRes = await getTransactions({ iban: account.value.iban, size: 10, page: 0, sort: 'timestamp,desc' })
      transactions.value = txRes.data.content ?? []
    }
  } catch {
    error.value = 'Could not load account details.'
  } finally {
    loading.value = false
  }
})

const isChecking = computed(() => account.value?.accountType === 'CHECKING')
const ownerName = computed(() => {
  const u = account.value?.user
  if (!u) return ''
  return `${u.firstName} ${u.lastName}`
})
const ownerEmail = computed(() => account.value?.user?.email ?? '')
</script>

<template>
  <CustomerLayout>
    <RouterLink
      to="/customer"
      class="inline-flex items-center gap-2 text-sm font-medium mb-6 no-underline"
      :style="{ color: 'var(--ink-2)' }"
    >
      <ArrowLeft class="w-4 h-4" />
      Back to overview
    </RouterLink>

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <div class="skeleton h-48 rounded-2xl" />
      <div class="grid grid-cols-3 gap-4">
        <div v-for="i in 3" :key="i" class="skeleton h-28 rounded-2xl" />
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">
      {{ error }}
    </div>

    <template v-else-if="account">
      <!-- Account card -->
      <div
        class="rounded-2xl p-8 mb-6"
        :class="isChecking ? 'card-vault' : 'card-savings'"
      >
        <p class="text-xs font-medium uppercase tracking-[.1em] opacity-70 mb-2">{{ account.accountType }}</p>
        <div class="text-4xl font-display tabnum mb-6" style="font-weight: 300;">
          {{ eur(account.balance) }}
        </div>
        <CopyChip :value="account.iban" :light="isChecking" />
      </div>

      <!-- Info cards -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <VCard>
          <div class="p-5">
            <p class="text-xs font-medium uppercase tracking-[.1em] mb-1" :style="{ color: 'var(--ink-3)' }">Daily transfer limit</p>
            <p class="text-2xl font-display tabnum" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
              {{ eur(account.dailyLimit) }}
            </p>
            <p class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">per day</p>
          </div>
        </VCard>

        <VCard>
          <div class="p-5">
            <p class="text-xs font-medium uppercase tracking-[.1em] mb-1" :style="{ color: 'var(--ink-3)' }">Absolute limit</p>
            <p class="text-2xl font-display tabnum" style="font-weight: 400;"
              :style="{ color: Number(account.absoluteLimit) < 0 ? 'var(--debit)' : 'var(--ink)' }">
              {{ eur(account.absoluteLimit) }}
            </p>
            <p class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">minimum balance</p>
          </div>
        </VCard>

        <VCard>
          <div class="p-5">
            <p class="text-xs font-medium uppercase tracking-[.1em] mb-1" :style="{ color: 'var(--ink-3)' }">Account holder</p>
            <p class="text-base font-semibold" :style="{ color: 'var(--ink)' }">{{ ownerName }}</p>
            <p class="text-xs mt-1 truncate" :style="{ color: 'var(--ink-3)' }">{{ ownerEmail }}</p>
          </div>
        </VCard>
      </div>

      <!-- Recent transactions -->
      <h2 class="text-base font-semibold mb-4" :style="{ color: 'var(--ink)' }">Recent transactions</h2>
      <VCard>
        <div class="p-2">
          <div v-if="!transactions.length" class="py-10 text-center">
            <AlignLeft class="w-8 h-8 mx-auto mb-2" :style="{ color: 'var(--ink-3)' }" />
            <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No transactions yet for this account.</p>
          </div>
          <ActivityRow
            v-for="tx in transactions"
            :key="tx.id"
            :tx="tx"
            :my-ibans="allIbans"
          />
        </div>
      </VCard>
    </template>
  </CustomerLayout>
</template>
