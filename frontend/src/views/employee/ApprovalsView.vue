<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, approveCustomer } from '../../services/employee'
import { CheckCircle2, XCircle, User } from 'lucide-vue-next'

const pendingCustomers = ref([])
const loading = ref(true)
const selectedId = ref(null)
const dailyLimit = ref('2000')
const absoluteLimit = ref('0')
const approving = ref(false)

onMounted(async () => {
  try {
    const { data } = await getPendingCustomers()
    pendingCustomers.value = data
  } finally {
    loading.value = false
  }
})

async function handleApprove(customerId) {
  approving.value = true
  try {
    await approveCustomer(customerId, {
      dailyLimit: parseFloat(dailyLimit.value),
      absoluteLimit: parseFloat(absoluteLimit.value),
    })
    pendingCustomers.value = pendingCustomers.value.filter(c => c.id !== customerId)
    selectedId.value = null
  } finally {
    approving.value = false
  }
}
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Pending Approvals</h1>
      <p class="text-sm text-gray-500 mt-1">{{ pendingCustomers.length }} customer{{ pendingCustomers.length !== 1 ? 's' : '' }} waiting for review.</p>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div v-for="i in 3" :key="i" class="skeleton h-52 rounded-2xl"></div>
    </div>

    <!-- Empty state -->
    <div v-else-if="pendingCustomers.length === 0" class="flex flex-col items-center justify-center py-20 bg-[#0D0D14] border border-white/[0.05] rounded-2xl">
      <div class="w-14 h-14 bg-[#00D9A3]/10 rounded-full flex items-center justify-center mb-4">
        <CheckCircle2 class="w-7 h-7 text-[#00D9A3]" />
      </div>
      <h3 class="text-white font-semibold mb-1">All caught up</h3>
      <p class="text-sm text-gray-600">No pending approvals at the moment.</p>
    </div>

    <!-- Cards -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div
        v-for="customer in pendingCustomers"
        :key="customer.id"
        class="bg-[#0D0D14] border border-white/[0.05] rounded-2xl p-5 flex flex-col hover:border-white/10 transition-all"
      >
        <!-- Customer info -->
        <div class="flex items-start gap-3 mb-5">
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-[#7B61FF]/40 to-[#5C45CC]/40 flex items-center justify-center font-bold text-[#7B61FF] text-sm flex-shrink-0">
            {{ customer.firstName.charAt(0) }}{{ customer.lastName.charAt(0) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="font-semibold text-white truncate">{{ customer.firstName }} {{ customer.lastName }}</p>
            <p class="text-xs text-gray-600 truncate">{{ customer.email }}</p>
          </div>
          <span class="text-[10px] font-semibold px-2 py-1 rounded-full bg-yellow-500/10 text-yellow-500 flex-shrink-0">PENDING</span>
        </div>

        <div class="grid grid-cols-2 gap-y-3 text-xs mb-5 flex-1">
          <div>
            <p class="text-gray-600 mb-0.5">Phone</p>
            <p class="text-gray-300 font-medium">{{ customer.phoneNumber || '—' }}</p>
          </div>
          <div>
            <p class="text-gray-600 mb-0.5">BSN</p>
            <p class="text-gray-300 font-medium font-mono">{{ customer.bsn }}</p>
          </div>
        </div>

        <!-- Approve form inline -->
        <div v-if="selectedId === customer.id" class="pt-4 border-t border-white/[0.05] space-y-3">
          <p class="text-xs font-semibold text-gray-400 uppercase tracking-wider">Set Account Limits</p>
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-[10px] text-gray-600 mb-1">Daily Limit (€)</label>
              <input v-model="dailyLimit" type="number" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#00D9A3]/30" />
            </div>
            <div>
              <label class="block text-[10px] text-gray-600 mb-1">Minimum Balance (€)</label>
              <input v-model="absoluteLimit" type="number" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-lg px-3 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-[#00D9A3]/30" />
            </div>
          </div>
          <div class="flex gap-2">
            <button @click="selectedId = null" class="flex-1 px-3 py-2 text-xs text-gray-500 hover:text-white bg-white/[0.04] hover:bg-white/[0.07] rounded-lg transition-all">Cancel</button>
            <button
              @click="handleApprove(customer.id)"
              :disabled="approving"
              class="flex-[2] px-3 py-2 text-xs font-semibold text-white bg-[#00D9A3] hover:bg-[#00c091] rounded-lg flex items-center justify-center gap-1.5 transition-colors disabled:opacity-60 shadow-lg shadow-[#00D9A3]/20"
            >
              <svg v-if="approving" class="w-3 h-3 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
              <CheckCircle2 v-else class="w-3 h-3" />
              {{ approving ? 'Approving...' : 'Approve Account' }}
            </button>
          </div>
        </div>

        <div v-else class="flex gap-2 pt-4 border-t border-white/[0.05] mt-auto">
          <button class="px-3 py-2 text-xs text-[#FF5E5B] hover:bg-[#FF5E5B]/10 rounded-lg flex items-center gap-1 transition-colors">
            <XCircle class="w-3.5 h-3.5" /> Deny
          </button>
          <button
            @click="selectedId = customer.id"
            class="flex-1 px-3 py-2 text-xs font-semibold text-white bg-[#7B61FF] hover:bg-[#6A52E5] rounded-lg transition-colors"
          >
            Review &amp; Set Limits
          </button>
        </div>
      </div>
    </div>
  </EmployeeLayout>
</template>
