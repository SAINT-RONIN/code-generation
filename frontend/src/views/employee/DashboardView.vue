<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, getAllAccounts } from '../../services/employee'
import { getAll } from '../../services/transactions'
import { Users, UserPlus, FileText, ArrowLeftRight, TrendingUp } from 'lucide-vue-next'

const stats = ref([
  { label: 'Total Customers',    value: '—', icon: Users,       color: 'text-blue-400',    bg: 'bg-blue-400/10',    path: '/employee/customers' },
  { label: 'Pending Approvals',  value: '—', icon: UserPlus,    color: 'text-[#00D9A3]',   bg: 'bg-[#00D9A3]/10',  path: '/employee/approvals' },
  { label: "Today's TXs",        value: '—', icon: FileText,    color: 'text-[#7B61FF]',   bg: 'bg-[#7B61FF]/10',  path: '/employee/transactions' },
  { label: 'Total Accounts',     value: '—', icon: TrendingUp,  color: 'text-[#FF5E5B]',   bg: 'bg-[#FF5E5B]/10',  path: '/employee/customers' },
])

const pendingCount = ref(0)

const quickTasks = [
  { title: 'Approve Customers',   desc: 'Review pending registrations', icon: UserPlus,       path: '/employee/approvals' },
  { title: 'Customer Directory',  desc: 'Search and manage accounts',   icon: Users,          path: '/employee/customers' },
  { title: 'System Transactions', desc: 'View global ledger',           icon: FileText,       path: '/employee/transactions' },
  { title: 'Manual Transfer',     desc: 'Move funds between users',     icon: ArrowLeftRight, path: '/employee/transfer' },
]

onMounted(async () => {
  const [pendingRes, accountsRes, txRes] = await Promise.allSettled([
    getPendingCustomers(),
    getAllAccounts({ size: 1 }),
    getAll({ size: 1 }),
  ])
  if (pendingRes.status === 'fulfilled') {
    pendingCount.value = pendingRes.value.data.length
    stats.value[0].value = '—'
    stats.value[1].value = pendingRes.value.data.length
  }
  if (accountsRes.status === 'fulfilled') {
    stats.value[3].value = accountsRes.value.data.totalElements ?? '—'
  }
  if (txRes.status === 'fulfilled') {
    stats.value[2].value = txRes.value.data.totalElements ?? '—'
  }
})
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2">Overview</h1>
      <p class="text-gray-400">Welcome back to the staff portal.</p>
    </div>

    <!-- Stats -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-10">
      <RouterLink
        v-for="stat in stats"
        :key="stat.label"
        :to="stat.path"
        class="bg-[#14141A] border border-white/5 rounded-2xl p-5 hover:bg-[#1C1C24] transition-colors"
        :aria-label="`${stat.label}: ${stat.value}`"
      >
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-4" :class="`${stat.bg} ${stat.color}`">
          <component :is="stat.icon" class="w-5 h-5" aria-hidden="true" />
        </div>
        <p class="text-sm font-medium text-gray-400 mb-1">{{ stat.label }}</p>
        <p class="text-2xl font-bold tabular-nums text-white">{{ stat.value }}</p>
      </RouterLink>
    </div>

    <!-- Quick Tasks -->
    <h2 class="text-xl font-bold text-white mb-4">Quick Tasks</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <RouterLink
        v-for="task in quickTasks"
        :key="task.path"
        :to="task.path"
        class="group bg-[#14141A] border border-white/5 rounded-2xl p-6 hover:border-[#FF5E5B]/30 hover:bg-[#1C1C24] transition-all"
        :aria-label="`${task.title}: ${task.desc}`"
      >
        <div class="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center mb-4 group-hover:bg-[#FF5E5B]/10 group-hover:text-[#FF5E5B] transition-colors text-gray-400">
          <component :is="task.icon" class="w-6 h-6" aria-hidden="true" />
        </div>
        <h3 class="font-bold text-white mb-1">{{ task.title }}</h3>
        <p class="text-sm text-gray-400">{{ task.desc }}</p>
      </RouterLink>
    </div>
  </EmployeeLayout>
</template>
