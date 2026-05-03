<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, getAllAccounts } from '../../services/employee'
import { getAll } from '../../services/transactions'
import { Users, UserPlus, FileText, ArrowLeftRight, ChevronRight } from 'lucide-vue-next'

const stats = ref([
  { label: 'Pending Approvals', value: '—', icon: UserPlus, color: 'text-[#00D9A3]', bg: 'bg-[#00D9A3]/10', path: '/employee/approvals' },
  { label: 'Total Accounts', value: '—', icon: Users, color: 'text-blue-400', bg: 'bg-blue-400/10', path: '/employee/customers' },
  { label: 'Total Transactions', value: '—', icon: FileText, color: 'text-[#7B61FF]', bg: 'bg-[#7B61FF]/10', path: '/employee/transactions' },
])

const quickTasks = [
  { title: 'Pending Approvals', desc: 'Review and approve new customers', icon: UserPlus, path: '/employee/approvals', accent: 'group-hover:text-[#00D9A3] group-hover:bg-[#00D9A3]/10' },
  { title: 'Customer Accounts', desc: 'View, manage, and update limits', icon: Users, path: '/employee/customers', accent: 'group-hover:text-blue-400 group-hover:bg-blue-400/10' },
  { title: 'System Ledger', desc: 'Browse all platform transactions', icon: FileText, path: '/employee/transactions', accent: 'group-hover:text-[#7B61FF] group-hover:bg-[#7B61FF]/10' },
  { title: 'Manual Transfer', desc: 'Move funds between any accounts', icon: ArrowLeftRight, path: '/employee/transfer', accent: 'group-hover:text-[#FF5E5B] group-hover:bg-[#FF5E5B]/10' },
]

onMounted(async () => {
  const [pendingRes, accountsRes, txRes] = await Promise.allSettled([
    getPendingCustomers(),
    getAllAccounts({ size: 1 }),
    getAll({ size: 1 }),
  ])
  if (pendingRes.status === 'fulfilled') stats.value[0].value = pendingRes.value.data.length
  if (accountsRes.status === 'fulfilled') stats.value[1].value = accountsRes.value.data.totalElements ?? '—'
  if (txRes.status === 'fulfilled') stats.value[2].value = txRes.value.data.totalElements ?? '—'
})
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Staff Overview</h1>
      <p class="text-sm text-gray-500 mt-1">Welcome back. Here's what needs your attention.</p>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-3 gap-4 mb-10">
      <RouterLink
        v-for="stat in stats"
        :key="stat.label"
        :to="stat.path"
        class="bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-5 hover:border-white/10 transition-all group"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="w-9 h-9 rounded-xl flex items-center justify-center" :class="[stat.bg, stat.color]">
            <component :is="stat.icon" class="w-4.5 h-4.5" />
          </div>
          <ChevronRight class="w-4 h-4 text-gray-700 group-hover:text-gray-400 transition-colors" />
        </div>
        <p class="text-2xl font-bold tabular-nums text-white mb-1">{{ stat.value }}</p>
        <p class="text-xs text-gray-600">{{ stat.label }}</p>
      </RouterLink>
    </div>

    <!-- Quick tasks -->
    <div class="mb-4">
      <h2 class="text-sm font-semibold text-gray-400 uppercase tracking-wider">Quick Actions</h2>
    </div>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3">
      <RouterLink
        v-for="task in quickTasks"
        :key="task.path"
        :to="task.path"
        class="group bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-5 hover:border-white/10 transition-all"
      >
        <div class="w-10 h-10 rounded-xl bg-white/[0.04] flex items-center justify-center mb-4 text-gray-500 transition-all" :class="task.accent">
          <component :is="task.icon" class="w-5 h-5" />
        </div>
        <p class="font-semibold text-white text-sm mb-1">{{ task.title }}</p>
        <p class="text-xs text-gray-600 leading-relaxed">{{ task.desc }}</p>
      </RouterLink>
    </div>
  </EmployeeLayout>
</template>
