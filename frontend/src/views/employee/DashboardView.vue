<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import { Users, CheckSquare, ArrowLeftRight, AlignLeft } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VPill from '../../components/ui/VPill.vue'
import { getCustomers, getAllAccounts } from '../../services/employee'
import { getTransactions } from '../../services/transactions'
import { eur } from '../../utils/format'

const pending = ref([])
const recentTx = ref([])
const totalAccounts = ref(0)
const loading = ref(true)

onMounted(async () => {
  try {
    const [pendingRes, accountsRes, txRes] = await Promise.allSettled([
      getCustomers({ status: 'PENDING', size: 10 }),
      getAllAccounts({ size: 1 }),
      getTransactions({ size: 5, sort: 'timestamp,desc' }),
    ])
    if (pendingRes.status === 'fulfilled')  pending.value = pendingRes.value.data.content ?? []
    if (accountsRes.status === 'fulfilled') totalAccounts.value = accountsRes.value.data.totalElements ?? 0
    if (txRes.status === 'fulfilled')       recentTx.value = txRes.value.data.content ?? []
  } catch {
    // unexpected error; show empty gracefully
  } finally {
    loading.value = false
  }
})

const stats = [
  { label: 'Total accounts',    getValue: () => totalAccounts.value, to: '/employee/customers',    icon: Users },
  { label: 'Pending approvals', getValue: () => pending.value.length, to: '/employee/approvals',   icon: CheckSquare },
  { label: 'Recent transfers',  getValue: () => recentTx.value.length, to: '/employee/transactions', icon: AlignLeft },
]

const quickActions = [
  { label: 'Initiate transfer', to: '/employee/transfer',     icon: ArrowLeftRight },
  { label: 'All accounts',      to: '/employee/customers',    icon: Users },
  { label: 'Approvals',         to: '/employee/approvals',    icon: CheckSquare },
  { label: 'Audit log',         to: '/employee/transactions', icon: AlignLeft },
]

function fmtTime(ts) {
  if (!ts) return ''
  return new Date(ts).toLocaleString('en-GB', { day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit' })
}
</script>

<template>
  <EmployeeLayout>
    <VPageHeader eyebrow="Staff portal" title="Dashboard" />

    <!-- Loading -->
    <div v-if="loading" class="space-y-4">
      <div class="grid grid-cols-2 lg:grid-cols-3 gap-4">
        <div v-for="i in 3" :key="i" class="skeleton h-28 rounded-2xl" />
      </div>
    </div>

    <template v-else>
      <!-- Stats -->
      <div class="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        <RouterLink
          v-for="stat in stats"
          :key="stat.label"
          :to="stat.to"
          class="rounded-2xl border p-5 no-underline lift"
          :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        >
          <div
            class="w-9 h-9 rounded-xl flex items-center justify-center mb-4"
            :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
          >
            <component :is="stat.icon" class="w-4 h-4" />
          </div>
          <p class="text-xs font-medium mb-1" :style="{ color: 'var(--ink-3)' }">{{ stat.label }}</p>
          <p class="text-3xl font-display tabnum" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">{{ stat.getValue() }}</p>
        </RouterLink>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- Pending approvals preview -->
        <div>
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-base font-semibold" :style="{ color: 'var(--ink)' }">Pending approvals</h2>
            <RouterLink to="/employee/approvals" class="text-sm font-medium no-underline" :style="{ color: 'var(--accent)' }">View all</RouterLink>
          </div>
          <VCard>
            <div v-if="pending.length" class="divide-y" :style="{ '--tw-divide-opacity': 1, borderColor: 'var(--line)' }">
              <div
                v-for="p in pending.slice(0, 4)"
                :key="p.id"
                class="flex items-center gap-3 px-4 py-3 row"
              >
                <div
                  class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0"
                  :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
                >{{ p.firstName[0] }}{{ p.lastName[0] }}</div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-medium truncate" :style="{ color: 'var(--ink)' }">{{ p.firstName }} {{ p.lastName }}</p>
                  <p class="text-xs truncate" :style="{ color: 'var(--ink-3)' }">{{ p.email }}</p>
                </div>
                <VPill tone="warn">Pending</VPill>
              </div>
            </div>
            <div v-else class="py-10 text-center">
              <CheckSquare class="w-8 h-8 mx-auto mb-2" :style="{ color: 'var(--ink-3)' }" />
              <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No pending approvals</p>
            </div>
          </VCard>
        </div>

        <!-- Quick actions -->
        <div>
          <h2 class="text-base font-semibold mb-4" :style="{ color: 'var(--ink)' }">Quick actions</h2>
          <div class="grid grid-cols-2 gap-3">
            <RouterLink
              v-for="action in quickActions"
              :key="action.label"
              :to="action.to"
              class="qa-tile no-underline rounded-2xl p-4 flex flex-col gap-2 lift"
            >
              <div
                class="w-8 h-8 rounded-lg flex items-center justify-center"
                :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
              >
                <component :is="action.icon" class="w-4 h-4" />
              </div>
              <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ action.label }}</p>
            </RouterLink>
          </div>
        </div>
      </div>

      <!-- Recent transactions -->
      <div class="mt-6">
        <div class="flex items-center justify-between mb-4">
          <h2 class="text-base font-semibold" :style="{ color: 'var(--ink)' }">Recent activity</h2>
          <RouterLink to="/employee/transactions" class="text-sm font-medium no-underline" :style="{ color: 'var(--accent)' }">View all</RouterLink>
        </div>
        <VCard>
          <div v-if="recentTx.length" class="divide-y" :style="{ borderColor: 'var(--line)' }">
            <div
              v-for="tx in recentTx"
              :key="tx.id"
              class="flex items-center gap-4 px-5 py-3.5 row"
            >
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium truncate" :style="{ color: 'var(--ink)' }">{{ tx.description || tx.transactionType }}</p>
                <p class="text-xs font-mono" :style="{ color: 'var(--ink-3)' }">{{ tx.fromIban }} → {{ tx.toIban }}</p>
              </div>
              <div class="text-right flex-shrink-0">
                <p class="text-sm font-medium tabnum" :style="{ color: 'var(--credit)' }">{{ eur(tx.amount) }}</p>
                <p class="text-xs" :style="{ color: 'var(--ink-3)' }">{{ fmtTime(tx.timestamp) }}</p>
              </div>
            </div>
          </div>
          <div v-else class="py-10 text-center">
            <AlignLeft class="w-8 h-8 mx-auto mb-2" :style="{ color: 'var(--ink-3)' }" />
            <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No transactions yet</p>
          </div>
        </VCard>
      </div>
    </template>
  </EmployeeLayout>
</template>
