<script setup>
import { ref, onMounted } from 'vue'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import { getAllAccounts, updateLimits, closeAccount } from '../../services/employee'
import { CreditCard, ChevronDown } from 'lucide-vue-next'

const accounts = ref([])
const page = ref(0)
const totalPages = ref(0)
const expandedIban = ref(null)
const editLimits = ref({ dailyLimit: '', absoluteLimit: '' })
const successIban = ref('')

function formatEur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(val)
}

async function loadAccounts() {
  const { data } = await getAllAccounts({ page: page.value, size: 15 })
  accounts.value = data.content || []
  totalPages.value = data.totalPages || 0
}

onMounted(loadAccounts)

function toggleExpand(iban, account) {
  if (expandedIban.value === iban) {
    expandedIban.value = null
    return
  }
  expandedIban.value = iban
  editLimits.value = { dailyLimit: account.dailyLimit, absoluteLimit: account.absoluteLimit }
}

async function saveLimits(iban) {
  await updateLimits(iban, { dailyLimit: parseFloat(editLimits.value.dailyLimit), absoluteLimit: parseFloat(editLimits.value.absoluteLimit) })
  successIban.value = iban
  expandedIban.value = null
  setTimeout(() => (successIban.value = ''), 3000)
  await loadAccounts()
}

async function handleCloseAccount(iban) {
  if (!confirm('Close this account?')) return
  await closeAccount(iban)
  await loadAccounts()
}
</script>

<template>
  <EmployeeLayout>
    <div class="mb-8">
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2">Customer Accounts</h1>
      <p class="text-gray-400">View and manage all customer accounts.</p>
    </div>

    <div class="bg-[#14141A] rounded-2xl border border-white/5 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="border-b border-white/5 text-sm font-medium text-gray-400 bg-black/20">
              <th class="p-4">Account</th>
              <th class="p-4">Owner</th>
              <th class="p-4">Balance</th>
              <th class="p-4">Status</th>
              <th class="p-4"></th>
            </tr>
          </thead>
          <tbody class="divide-y divide-white/5">
            <template v-for="account in accounts" :key="account.iban">
              <tr class="hover:bg-white/[0.02] transition-colors group cursor-pointer" @click="toggleExpand(account.iban, account)">
                <td class="p-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0"
                      :class="account.accountType === 'CHECKING' ? 'bg-[#7B61FF]/10 text-[#7B61FF]' : 'bg-[#00D9A3]/10 text-[#00D9A3]'">
                      <CreditCard class="w-5 h-5" />
                    </div>
                    <div>
                      <p class="font-medium text-white capitalize">{{ account.accountType.toLowerCase() }}</p>
                      <p class="text-xs text-gray-500 font-mono">{{ account.iban }}</p>
                    </div>
                  </div>
                </td>
                <td class="p-4">
                  <p class="text-sm text-white">{{ account.ownerName }}</p>
                  <p class="text-xs text-gray-500">{{ account.ownerEmail }}</p>
                </td>
                <td class="p-4">
                  <p class="font-medium tabular-nums text-white">{{ formatEur(account.balance) }}</p>
                </td>
                <td class="p-4">
                  <span class="text-xs font-medium px-2.5 py-1 rounded-full"
                    :class="account.active ? 'bg-[#00D9A3]/10 text-[#00D9A3]' : 'bg-[#FF5E5B]/10 text-[#FF5E5B]'">
                    {{ account.active ? 'Active' : 'Closed' }}
                  </span>
                </td>
                <td class="p-4 text-right">
                  <ChevronDown class="w-4 h-4 text-gray-500 transition-transform inline-block" :class="expandedIban === account.iban ? 'rotate-180' : ''" />
                </td>
              </tr>
              <tr v-if="expandedIban === account.iban" class="bg-[#1C1C24]">
                <td colspan="5" class="px-4 pb-4 pt-2">
                  <div class="grid grid-cols-2 md:grid-cols-4 gap-3 items-end">
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">Daily Limit (€)</label>
                      <input v-model="editLimits.dailyLimit" type="number" class="w-full bg-[#14141A] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
                    </div>
                    <div>
                      <label class="block text-xs text-gray-500 mb-1">Absolute Limit (€)</label>
                      <input v-model="editLimits.absoluteLimit" type="number" class="w-full bg-[#14141A] border border-white/10 rounded-lg px-3 py-2 text-sm text-white focus:outline-none" />
                    </div>
                    <button @click.stop="saveLimits(account.iban)" class="bg-[#7B61FF] hover:bg-[#6A52E5] text-white rounded-lg py-2 text-sm font-medium transition-colors">Save Limits</button>
                    <button v-if="account.active" @click.stop="handleCloseAccount(account.iban)" class="bg-[#FF5E5B]/10 hover:bg-[#FF5E5B]/20 text-[#FF5E5B] rounded-lg py-2 text-sm font-medium transition-colors">Close Account</button>
                  </div>
                  <p v-if="successIban === account.iban" class="text-xs text-[#00D9A3] mt-2">Limits updated successfully.</p>
                </td>
              </tr>
            </template>
          </tbody>
        </table>

        <div v-if="accounts.length === 0" class="p-12 text-center text-gray-500">
          <p>No accounts found.</p>
        </div>
      </div>
    </div>

    <div v-if="totalPages > 1" class="flex justify-center gap-3 mt-6">
      <button :disabled="page === 0" @click="page--; loadAccounts()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Prev</button>
      <span class="px-4 py-2 text-sm text-gray-400">{{ page + 1 }} / {{ totalPages }}</span>
      <button :disabled="page >= totalPages - 1" @click="page++; loadAccounts()" class="px-4 py-2 rounded-lg bg-[#14141A] border border-white/10 text-sm text-white disabled:opacity-30">Next</button>
    </div>
  </EmployeeLayout>
</template>
