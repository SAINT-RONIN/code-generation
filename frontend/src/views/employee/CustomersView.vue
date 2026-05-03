<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getAllAccounts, updateLimits, closeAccount } from '../../services/employee'
import { CreditCard, ChevronDown, TrendingUp, Shield, Check } from 'lucide-vue-next'

const accounts = ref([])
const page = ref(0)
const totalPages = ref(0)
const loading = ref(true)
const expandedIban = ref(null)
const editLimits = ref({ dailyLimit: '', absoluteLimit: '' })
const saving = ref(false)
const savedIban = ref('')

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val) }

async function loadAccounts() {
  loading.value = true
  try {
    const { data } = await getAllAccounts({ page: page.value, size: 15 })
    accounts.value = data.content || []
    totalPages.value = data.totalPages || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadAccounts)

function toggleExpand(iban, account) {
  if (expandedIban.value === iban) { expandedIban.value = null; return }
  expandedIban.value = iban
  editLimits.value = { dailyLimit: account.dailyLimit, absoluteLimit: account.absoluteLimit }
}

async function saveLimits(iban) {
  saving.value = true
  try {
    await updateLimits(iban, { dailyLimit: parseFloat(editLimits.value.dailyLimit), absoluteLimit: parseFloat(editLimits.value.absoluteLimit) })
    savedIban.value = iban
    expandedIban.value = null
    setTimeout(() => (savedIban.value = ''), 3000)
    await loadAccounts()
  } finally {
    saving.value = false
  }
}

async function handleClose(iban) {
  if (!confirm('Close this account? This cannot be undone.')) return
  await closeAccount(iban)
  await loadAccounts()
}
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-2xl font-bold text-white">Customer Accounts</h1>
      <p class="text-sm text-gray-500 mt-1">Manage limits and account status. Click a row to expand.</p>
    </div>

    <div v-if="loading" class="space-y-2">
      <div v-for="i in 6" :key="i" class="skeleton h-16 rounded-xl"></div>
    </div>

    <div v-else class="bg-[#14141A] rounded-2xl border border-white/[0.05] overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left">
          <thead>
            <tr class="border-b border-white/[0.05] text-[11px] font-semibold text-gray-600 uppercase tracking-wider">
              <th class="px-4 py-3">Account</th>
              <th class="px-4 py-3">Owner</th>
              <th class="px-4 py-3">Balance</th>
              <th class="px-4 py-3">Limits</th>
              <th class="px-4 py-3">Status</th>
              <th class="px-4 py-3 w-8"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/[0.03]">
            <template v-for="account in accounts" :key="account.iban">
              <tr
                class="hover:bg-white/[0.02] transition-colors cursor-pointer"
                @click="toggleExpand(account.iban, account)"
              >
                <td class="px-4 py-3.5">
                  <div class="flex items-center gap-2.5">
                    <div class="w-8 h-8 rounded-lg flex items-center justify-center flex-shrink-0"
                      :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/15 text-[#7B61FF]' : 'bg-[#00D9A3]/15 text-[#00D9A3]'">
                      <CreditCard class="w-3.5 h-3.5" />
                    </div>
                    <div>
                      <p class="text-sm font-medium text-white capitalize">{{ account.accountType.toLowerCase() }}</p>
                      <p class="text-[10px] text-gray-600 font-mono">{{ account.iban.slice(0, 8) }}···</p>
                    </div>
                  </div>
                </td>
                <td class="px-4 py-3.5">
                  <p class="text-sm text-white">{{ account.ownerName }}</p>
                  <p class="text-[11px] text-gray-600 truncate max-w-[140px]">{{ account.ownerEmail }}</p>
                </td>
                <td class="px-4 py-3.5">
                  <p class="text-sm font-semibold tabular-nums text-white">{{ eur(account.balance) }}</p>
                </td>
                <td class="px-4 py-3.5">
                  <p class="text-[11px] text-gray-500">Daily: <span class="text-gray-300">{{ eur(account.dailyLimit) }}</span></p>
                  <p class="text-[11px] text-gray-500">Min: <span class="text-gray-300">{{ eur(account.absoluteLimit) }}</span></p>
                </td>
                <td class="px-4 py-3.5">
                  <span class="inline-flex items-center gap-1 text-[11px] font-semibold px-2 py-1 rounded-full"
                    :class="account.active ? 'bg-[#00D9A3]/10 text-[#00D9A3]' : 'bg-[#FF5E5B]/10 text-[#FF5E5B]'">
                    <div class="w-1.5 h-1.5 rounded-full" :class="account.active ? 'bg-[#00D9A3]' : 'bg-[#FF5E5B]'"></div>
                    {{ account.active ? 'Active' : 'Closed' }}
                  </span>
                </td>
                <td class="px-4 py-3.5 text-right">
                  <ChevronDown class="w-4 h-4 text-gray-600 transition-transform inline-block" :class="expandedIban === account.iban ? 'rotate-180' : ''" />
                </td>
              </tr>

              <!-- Expanded row -->
              <tr v-if="expandedIban === account.iban">
                <td colspan="6" class="bg-[#0A0A10] border-t border-white/[0.04]">
                  <div class="px-5 py-4">
                    <p class="text-[11px] font-semibold text-gray-500 uppercase tracking-wider mb-3">Update Limits</p>
                    <div class="flex flex-col sm:flex-row gap-3 items-end">
                      <div class="flex-1">
                        <label class="block text-[11px] text-gray-600 mb-1.5 flex items-center gap-1.5"><TrendingUp class="w-3 h-3" /> Daily Limit (€)</label>
                        <input v-model="editLimits.dailyLimit" type="number" class="w-full bg-[#14141A] border border-white/[0.07] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
                      </div>
                      <div class="flex-1">
                        <label class="block text-[11px] text-gray-600 mb-1.5 flex items-center gap-1.5"><Shield class="w-3 h-3" /> Minimum Balance (€)</label>
                        <input v-model="editLimits.absoluteLimit" type="number" class="w-full bg-[#14141A] border border-white/[0.07] rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-1 focus:ring-[#7B61FF]/30" />
                      </div>
                      <button @click.stop="saveLimits(account.iban)" :disabled="saving" class="px-5 py-2 bg-[#7B61FF] hover:bg-[#6A52E5] text-white rounded-lg text-sm font-semibold transition-colors disabled:opacity-60 flex items-center gap-2">
                        <svg v-if="saving" class="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                        <Check v-else class="w-3.5 h-3.5" />
                        Save
                      </button>
                      <button v-if="account.active" @click.stop="handleClose(account.iban)" class="px-4 py-2 bg-[#FF5E5B]/10 hover:bg-[#FF5E5B]/20 text-[#FF5E5B] rounded-lg text-sm font-semibold transition-colors">
                        Close
                      </button>
                    </div>
                    <p v-if="savedIban === account.iban" class="text-xs text-[#00D9A3] mt-2 flex items-center gap-1">
                      <Check class="w-3 h-3" /> Limits updated successfully
                    </p>
                  </div>
                </td>
              </tr>
            </template>
          </tbody>
        </table>

        <div v-if="accounts.length === 0" class="py-14 text-center text-gray-600 text-sm">No accounts found.</div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex items-center justify-center gap-2 mt-5">
      <button :disabled="page === 0" @click="page--; loadAccounts()" class="px-3 py-1.5 rounded-lg bg-[#14141A] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">← Prev</button>
      <span class="text-xs text-gray-600 px-2">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadAccounts()" class="px-3 py-1.5 rounded-lg bg-[#14141A] border border-white/10 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition-all">Next →</button>
    </div>
  </EmployeeLayout>
</template>
