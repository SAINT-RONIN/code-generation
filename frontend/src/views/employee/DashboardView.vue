<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, getAllAccounts } from '../../services/employee'
import { getAll } from '../../services/transactions'
import { Users, UserPlus, FileText, ArrowLeftRight, ChevronRight } from 'lucide-vue-next'

const stats = ref([
  { label: 'Pending approvals', value: '—', icon: UserPlus, accent: '#00D9A3', path: '/employee/approvals' },
  { label: 'Total accounts',    value: '—', icon: Users,    accent: '#7B61FF', path: '/employee/customers' },
  { label: 'Transactions',      value: '—', icon: FileText, accent: '#F59E0B', path: '/employee/transactions' },
])

const tasks = [
  { title: 'Pending Approvals', desc: 'Review and approve new customers', icon: UserPlus,       path: '/employee/approvals',     accent: '#00D9A3' },
  { title: 'Customer Accounts', desc: 'Manage limits and account status', icon: Users,          path: '/employee/customers',     accent: '#7B61FF' },
  { title: 'System Ledger',     desc: 'Browse all platform transactions', icon: FileText,       path: '/employee/transactions',  accent: '#F59E0B' },
  { title: 'Manual Transfer',   desc: 'Move funds between any accounts',  icon: ArrowLeftRight, path: '/employee/transfer',      accent: '#FF5E5B' },
]

onMounted(async () => {
  const [pendingRes, accountsRes, txRes] = await Promise.allSettled([
    getPendingCustomers(),
    getAllAccounts({ size: 1 }),
    getAll({ size: 1 }),
  ])
  if (pendingRes.status === 'fulfilled')  stats.value[0].value = pendingRes.value.data.length
  if (accountsRes.status === 'fulfilled') stats.value[1].value = accountsRes.value.data.totalElements ?? '—'
  if (txRes.status === 'fulfilled')       stats.value[2].value = txRes.value.data.totalElements ?? '—'
})
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Staff overview</h1>
      <p class="text-sm mt-1" style="color:#6B6B7E;">Here's what needs your attention today.</p>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-3 gap-4 mb-10" role="list" aria-label="Key metrics">
      <RouterLink
        v-for="stat in stats"
        :key="stat.label"
        :to="stat.path"
        class="rounded-2xl p-5 transition-colors group"
        style="background:#0E0E16; border:1px solid rgba(255,255,255,0.07);"
        role="listitem"
        :aria-label="`${stat.label}: ${stat.value}`"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="w-9 h-9 rounded-xl flex items-center justify-center" :style="`background:${stat.accent}18;`">
            <component :is="stat.icon" class="w-4 h-4" :style="`color:${stat.accent}`" aria-hidden="true" />
          </div>
          <ChevronRight class="w-4 h-4 transition-colors" style="color:#2E2E3E;" aria-hidden="true" />
        </div>
        <p class="text-2xl font-bold tabular-nums text-white mb-1">{{ stat.value }}</p>
        <p class="text-xs" style="color:#6B6B7E;">{{ stat.label }}</p>
      </RouterLink>
    </div>

    <!-- Quick tasks -->
    <h2 class="text-xs font-semibold uppercase tracking-widest mb-4" style="color:#4A4A5E;">Quick actions</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
      <RouterLink
        v-for="task in tasks"
        :key="task.path"
        :to="task.path"
        class="rounded-2xl p-5 transition-colors group"
        style="background:#0E0E16; border:1px solid rgba(255,255,255,0.07);"
        :aria-label="`${task.title}: ${task.desc}`"
      >
        <div
          class="w-10 h-10 rounded-xl flex items-center justify-center mb-4 transition-colors"
          :style="`background:${task.accent}10;`"
        >
          <component :is="task.icon" class="w-5 h-5" :style="`color:${task.accent}`" aria-hidden="true" />
        </div>
        <p class="font-semibold text-sm text-white mb-1">{{ task.title }}</p>
        <p class="text-xs leading-relaxed" style="color:#6B6B7E;">{{ task.desc }}</p>
      </RouterLink>
    </div>
  </EmployeeLayout>
</template>
