<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getPendingCustomers, approveCustomer } from '../../services/employee'
import { CheckCircle, XCircle } from 'lucide-vue-next'

const pendingCustomers = ref([])
const selectedId = ref(null)
const dailyLimit = ref('2000')
const absoluteLimit = ref('0')

onMounted(async () => {
  const { data } = await getPendingCustomers()
  pendingCustomers.value = data
})

async function handleApprove(customerId) {
  await approveCustomer(customerId, {
    dailyLimit: parseFloat(dailyLimit.value),
    absoluteLimit: parseFloat(absoluteLimit.value),
  })
  pendingCustomers.value = pendingCustomers.value.filter(c => c.id !== customerId)
  selectedId.value = null
}
</script>

<template>
  <EmployeeLayout>
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-8">
      <div>
        <h1 class="text-2xl font-bold tracking-tight text-white">Pending Approvals</h1>
        <p class="text-gray-400 text-sm">{{ pendingCustomers.length }} customers waiting for review</p>
      </div>
    </div>

    <div v-if="pendingCustomers.length === 0" class="text-center py-20 bg-[#14141A] border border-white/5 rounded-2xl">
      <div class="w-16 h-16 bg-[#00D9A3]/10 text-[#00D9A3] rounded-full flex items-center justify-center mx-auto mb-4">
        <CheckCircle class="w-8 h-8" />
      </div>
      <h2 class="text-xl font-bold text-white mb-1">All caught up</h2>
      <p class="text-gray-500">No pending approvals at the moment.</p>
    </div>

    <div v-else class="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-4">
      <div
        v-for="customer in pendingCustomers"
        :key="customer.id"
        class="bg-[#14141A] border border-white/5 rounded-2xl p-5 flex flex-col"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 rounded-full bg-gradient-to-tr from-gray-700 to-gray-600 flex items-center justify-center font-bold text-white">
              {{ customer.firstName.charAt(0) }}
            </div>
            <div>
              <h3 class="font-bold text-white">{{ customer.firstName }} {{ customer.lastName }}</h3>
              <p class="text-xs text-gray-500">{{ customer.email }}</p>
            </div>
          </div>
          <span class="text-xs font-medium px-2 py-1 rounded bg-yellow-500/10 text-yellow-500">Review</span>
        </div>

        <div class="space-y-2 mb-6 flex-1 text-sm">
          <div class="flex justify-between">
            <span class="text-gray-500">Phone</span>
            <span class="text-gray-300">{{ customer.phoneNumber || 'N/A' }}</span>
          </div>
          <div class="flex justify-between">
            <span class="text-gray-500">BSN</span>
            <span class="text-gray-300">{{ customer.bsn }}</span>
          </div>
        </div>

        <div v-if="selectedId === customer.id" class="space-y-3 pt-4 border-t border-white/5">
          <div class="grid grid-cols-2 gap-2">
            <div>
              <label class="block text-xs text-gray-500 mb-1">Daily Limit (€)</label>
              <input v-model="dailyLimit" type="number" class="w-full bg-[#1C1C24] border border-white/10 rounded px-3 py-1.5 text-sm text-white focus:outline-none" />
            </div>
            <div>
              <label class="block text-xs text-gray-500 mb-1">Absolute Limit (€)</label>
              <input v-model="absoluteLimit" type="number" class="w-full bg-[#1C1C24] border border-white/10 rounded px-3 py-1.5 text-sm text-white focus:outline-none" />
            </div>
          </div>
          <div class="flex gap-2">
            <button @click="selectedId = null" class="flex-1 px-3 py-2 text-sm text-gray-400 hover:text-white bg-white/5 rounded-lg">Cancel</button>
            <button
              @click="handleApprove(customer.id)"
              class="flex-1 px-3 py-2 text-sm font-bold text-white bg-[#00D9A3] hover:bg-[#00c091] rounded-lg flex items-center justify-center gap-1 shadow-lg shadow-[#00D9A3]/20"
            >
              <CheckCircle class="w-4 h-4" /> Approve
            </button>
          </div>
        </div>

        <div v-else class="flex gap-2 pt-4 border-t border-white/5 mt-auto">
          <button class="px-3 py-2 text-sm text-[#FF5E5B] hover:bg-[#FF5E5B]/10 rounded-lg flex items-center gap-1 transition-colors">
            <XCircle class="w-4 h-4" /> Deny
          </button>
          <button
            @click="selectedId = customer.id"
            class="flex-1 px-3 py-2 text-sm font-bold text-white bg-[#7B61FF] hover:bg-[#6A52E5] rounded-lg transition-colors"
          >
            Review & Set Limits
          </button>
        </div>
      </div>
    </div>
  </EmployeeLayout>
</template>
