<script setup>
import { ref } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { transferBetweenCustomers } from '../../services/employee'
import { ArrowRight, CheckCircle2, AlertCircle } from 'lucide-vue-next'

const fromIban = ref('')
const toIban = ref('')
const amount = ref('')
const description = ref('')
const error = ref('')
const success = ref(false)

async function executeTransfer() {
  error.value = ''
  success.value = false
  try {
    await transferBetweenCustomers({
      fromIban: fromIban.value,
      toIban: toIban.value,
      amount: parseFloat(amount.value),
      description: description.value || 'Employee Transfer',
    })
    success.value = true
    fromIban.value = ''
    toIban.value = ''
    amount.value = ''
    description.value = ''
  } catch (e) {
    error.value = e.response?.data?.error || 'Transfer failed.'
  }
}
</script>

<template>
  <EmployeeLayout>
    <div class="max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2">Manual Transfer</h1>
      <p class="text-gray-400 mb-8">Move funds between any two customer checking accounts.</p>

      <div v-if="success" class="mb-8 p-4 bg-[#00D9A3]/10 border border-[#00D9A3]/20 rounded-xl flex items-center gap-3 text-[#00D9A3]">
        <CheckCircle2 class="w-6 h-6 flex-shrink-0" />
        <p class="font-medium">Transfer executed successfully.</p>
      </div>

      <div v-if="error" class="mb-8 p-4 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-xl text-[#FF5E5B] flex items-center gap-3">
        <AlertCircle class="w-6 h-6 flex-shrink-0" />
        <p class="font-medium">{{ error }}</p>
      </div>

      <div class="bg-[#14141A] border border-white/5 rounded-2xl p-6 space-y-6">
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Source IBAN</label>
          <input
            v-model="fromIban"
            type="text"
            placeholder="NL00 INHO 0000 0000 00"
            class="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B] font-mono uppercase"
            @input="fromIban = fromIban.toUpperCase()"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Destination IBAN</label>
          <input
            v-model="toIban"
            type="text"
            placeholder="NL00 INHO 0000 0000 00"
            class="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B] font-mono uppercase"
            @input="toIban = toIban.toUpperCase()"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Amount (€)</label>
          <input
            v-model="amount"
            type="number"
            placeholder="0.00"
            step="0.01"
            min="0.01"
            class="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B]"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-2">Internal Note</label>
          <input
            v-model="description"
            type="text"
            placeholder="Reason for this transfer"
            class="w-full bg-black/20 border border-white/10 rounded-xl p-4 text-white focus:outline-none focus:border-[#FF5E5B]"
          />
        </div>

        <button
          @click="executeTransfer"
          class="w-full bg-[#FF5E5B] hover:bg-[#E54542] text-white p-4 rounded-xl font-bold text-lg transition-colors flex items-center justify-center gap-2"
        >
          Execute Transfer <ArrowRight class="w-5 h-5" />
        </button>
      </div>
    </div>
  </EmployeeLayout>
</template>
