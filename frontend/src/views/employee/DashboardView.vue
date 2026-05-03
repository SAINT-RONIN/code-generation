<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, getAllAccounts } from '../../services/employee'
import { getAll } from '../../services/transactions'
import { Users, UserPlus, FileText, ArrowLeftRight } from 'lucide-vue-next'

const pendingCount = ref(0)
const totalAccounts = ref(0)
const todayTxCount = ref(0)

onMounted(async () => {
  const [pendingRes, accountsRes, txRes] = await Promise.all([
    getPendingCustomers(),
    getAllAccounts({ size: 1 }),
    getAll({ size: 1 }),
  ])
  pendingCount.value = pendingRes.data.length
  totalAccounts.value = accountsRes.data.totalElements || 0
  todayTxCount.value = txRes.data.totalElements || 0
})

const quickTasks = [
  { title: 'Approve Customers', desc: 'Review pending applications', icon: UserPlus, path: '/employee/approvals' },
  { title: 'Customer Directory', desc: 'Search and manage accounts', icon: Users, path: '/employee/customers' },
  { title: 'System Transactions', desc: 'View global ledger', icon: FileText, path: '/employee/transactions' },
  { title: 'Manual Transfer', desc: 'Move funds between customers', icon: ArrowLeftRight, path: '/employee/transfer' },
]
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2">Overview</h1>
      <p class="text-gray-400">Welcome back to the staff portal.</p>
    </div>

    <div class="grid grid-cols-2 md:grid-cols-3 gap-4 mb-10">
      <div class="bg-[#14141A] border border-white/5 rounded-2xl p-5">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-4 bg-[#00D9A3]/10 text-[#00D9A3]">
          <UserPlus class="w-5 h-5" />
        </div>
        <p class="text-sm font-medium text-gray-400 mb-1">Pending Approvals</p>
        <p class="text-2xl font-bold tabular-nums text-white">{{ pendingCount }}</p>
      </div>
      <div class="bg-[#14141A] border border-white/5 rounded-2xl p-5">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-4 bg-blue-400/10 text-blue-400">
          <Users class="w-5 h-5" />
        </div>
        <p class="text-sm font-medium text-gray-400 mb-1">Total Accounts</p>
        <p class="text-2xl font-bold tabular-nums text-white">{{ totalAccounts }}</p>
      </div>
      <div class="bg-[#14141A] border border-white/5 rounded-2xl p-5">
        <div class="w-10 h-10 rounded-xl flex items-center justify-center mb-4 bg-[#7B61FF]/10 text-[#7B61FF]">
          <FileText class="w-5 h-5" />
        </div>
        <p class="text-sm font-medium text-gray-400 mb-1">Total Transactions</p>
        <p class="text-2xl font-bold tabular-nums text-white">{{ todayTxCount }}</p>
      </div>
    </div>

    <h2 class="text-xl font-bold text-white mb-4">Quick Tasks</h2>
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <RouterLink
        v-for="task in quickTasks"
        :key="task.path"
        :to="task.path"
        class="group bg-[#14141A] border border-white/5 rounded-2xl p-6 hover:border-[#FF5E5B]/30 hover:bg-[#1C1C24] transition-all"
      >
        <div class="w-12 h-12 rounded-xl bg-white/5 flex items-center justify-center mb-4 group-hover:bg-[#FF5E5B]/10 group-hover:text-[#FF5E5B] transition-colors text-gray-400">
          <component :is="task.icon" class="w-6 h-6" />
        </div>
        <h3 class="font-bold text-white mb-1">{{ task.title }}</h3>
        <p class="text-sm text-gray-400">{{ task.desc }}</p>
      </RouterLink>
    </div>
  </EmployeeLayout>
</template>
