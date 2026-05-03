<script setup>
import { ref } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { transferBetweenCustomers } from '../../services/employee'
import { ArrowLeftRight, CheckCircle2, AlertCircle, ArrowRight } from 'lucide-vue-next'

const fromIban = ref('')
const toIban = ref('')
const amount = ref('')
const description = ref('')
const error = ref('')
const success = ref(false)
const loading = ref(false)

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }

async function executeTransfer() {
  error.value = ''
  success.value = false
  loading.value = true
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
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <EmployeeLayout>
    <div class="max-w-xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-white">Manual Transfer</h1>
        <p class="text-sm text-gray-500 mt-1">Move funds between any two customer accounts.</p>
      </div>

      <!-- Success banner -->
      <div v-if="success" class="mb-6 flex items-center gap-3 bg-[#00D9A3]/10 border border-[#00D9A3]/20 rounded-2xl px-5 py-4">
        <CheckCircle2 class="w-5 h-5 text-[#00D9A3] flex-shrink-0" />
        <p class="text-sm font-semibold text-[#00D9A3]">Transfer executed successfully.</p>
      </div>

      <!-- Error banner -->
      <div v-if="error" class="mb-6 flex items-center gap-3 bg-[#FF5E5B]/10 border border-[#FF5E5B]/20 rounded-2xl px-5 py-4">
        <AlertCircle class="w-5 h-5 text-[#FF5E5B] flex-shrink-0" />
        <p class="text-sm font-semibold text-[#FF5E5B]">{{ error }}</p>
      </div>

      <div class="bg-[#0D0D14] border border-white/[0.05] rounded-2xl overflow-hidden">
        <!-- Header accent -->
        <div class="h-1 w-full bg-gradient-to-r from-[#FF5E5B] to-[#FF8A87]"></div>

        <div class="p-6 space-y-5">
          <!-- From IBAN -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Source IBAN</label>
            <input
              v-model="fromIban"
              type="text"
              placeholder="NL00INHO0000000000"
              class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl px-4 py-3 text-sm text-white font-mono uppercase focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/40 placeholder:text-gray-700 transition-all"
              @input="fromIban = fromIban.toUpperCase()"
            />
          </div>

          <!-- Arrow separator -->
          <div class="flex items-center gap-3">
            <div class="flex-1 h-px bg-white/[0.05]"></div>
            <div class="w-8 h-8 rounded-full bg-[#FF5E5B]/10 flex items-center justify-center">
              <ArrowRight class="w-3.5 h-3.5 text-[#FF5E5B]" />
            </div>
            <div class="flex-1 h-px bg-white/[0.05]"></div>
          </div>

          <!-- To IBAN -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Destination IBAN</label>
            <input
              v-model="toIban"
              type="text"
              placeholder="NL00INHO0000000000"
              class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl px-4 py-3 text-sm text-white font-mono uppercase focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/40 placeholder:text-gray-700 transition-all"
              @input="toIban = toIban.toUpperCase()"
            />
          </div>

          <!-- Amount -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Amount (€)</label>
            <div class="relative">
              <span class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500 text-sm font-semibold">€</span>
              <input
                v-model="amount"
                type="number"
                placeholder="0.00"
                step="0.01"
                min="0.01"
                class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl pl-8 pr-4 py-3 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/40 placeholder:text-gray-700 transition-all"
              />
            </div>
          </div>

          <!-- Note -->
          <div>
            <label class="block text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2">Internal Note <span class="text-gray-700 normal-case font-normal">(optional)</span></label>
            <input
              v-model="description"
              type="text"
              placeholder="Reason for this transfer"
              class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl px-4 py-3 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#FF5E5B]/40 placeholder:text-gray-700 transition-all"
            />
          </div>

          <!-- Submit -->
          <button
            @click="executeTransfer"
            :disabled="loading || !fromIban || !toIban || !amount"
            class="w-full bg-[#FF5E5B] hover:bg-[#E54542] disabled:opacity-50 text-white rounded-xl py-3 text-sm font-semibold flex items-center justify-center gap-2 transition-colors shadow-lg shadow-[#FF5E5B]/20"
          >
            <svg v-if="loading" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
            <ArrowLeftRight v-else class="w-4 h-4" />
            {{ loading ? 'Processing...' : 'Execute Transfer' }}
          </button>
        </div>
      </div>

      <!-- Info note -->
      <p class="text-xs text-gray-700 text-center mt-4">Transaction limits still apply to all employee-initiated transfers.</p>
    </div>
  </EmployeeLayout>
</template>
