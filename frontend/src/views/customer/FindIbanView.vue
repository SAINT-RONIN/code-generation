<script setup>
import { ref } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { searchByName } from '../../services/accounts'
import { Copy, Check, Search, User } from 'lucide-vue-next'

const firstName = ref('')
const lastName = ref('')
const results = ref([])
const searched = ref(false)
const loading = ref(false)
const copied = ref('')

async function search() {
  if (!firstName.value || !lastName.value) return
  loading.value = true
  searched.value = true
  try {
    const { data } = await searchByName(firstName.value, lastName.value)
    results.value = data
  } finally {
    loading.value = false
  }
}

function copyIban(iban) {
  navigator.clipboard.writeText(iban)
  copied.value = iban
  setTimeout(() => (copied.value = ''), 2000)
}
</script>

<template>
  <CustomerLayout>
    <div class="max-w-xl mx-auto">
      <div class="mb-8">
        <h1 class="text-2xl font-bold text-white">Find IBAN</h1>
        <p class="text-sm text-gray-500 mt-1">Search customers by name to get their checking account IBAN.</p>
      </div>

      <form @submit.prevent="search" class="bg-[#0D0D14] border border-white/[0.06] rounded-2xl p-5 mb-6">
        <div class="grid grid-cols-2 gap-3 mb-4">
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-1.5">First name</label>
            <input v-model="firstName" required type="text" placeholder="John" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-2.5 px-3.5 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/30 placeholder:text-gray-700 transition-all" />
          </div>
          <div>
            <label class="block text-xs font-medium text-gray-500 mb-1.5">Last name</label>
            <input v-model="lastName" required type="text" placeholder="Doe" class="w-full bg-[#1C1C24] border border-white/[0.06] rounded-xl py-2.5 px-3.5 text-white text-sm focus:outline-none focus:ring-2 focus:ring-[#7B61FF]/30 placeholder:text-gray-700 transition-all" />
          </div>
        </div>
        <button type="submit" :disabled="loading" class="w-full bg-gradient-to-r from-[#7B61FF] to-[#6050D0] text-white rounded-xl py-2.5 text-sm font-semibold flex items-center justify-center gap-2 hover:shadow-lg hover:shadow-[#7B61FF]/20 disabled:opacity-60 transition-all">
          <svg v-if="loading" class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
          <Search v-else class="w-4 h-4" />
          {{ loading ? 'Searching...' : 'Search Customer' }}
        </button>
      </form>

      <div v-if="searched && !loading">
        <div v-if="results.length === 0" class="text-center py-10 text-gray-600">
          <User class="w-8 h-8 mx-auto mb-3 opacity-30" />
          <p class="text-sm">No customers found with that name.</p>
        </div>

        <div v-else class="space-y-3">
          <div
            v-for="result in results"
            :key="result.iban"
            class="bg-[#0D0D14] border border-[#7B61FF]/20 rounded-2xl p-5 hover:border-[#7B61FF]/40 transition-all"
          >
            <div class="flex items-center gap-3 mb-4">
              <div class="w-10 h-10 rounded-full bg-gradient-to-br from-[#7B61FF] to-[#00D9A3] flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
                {{ result.firstName.charAt(0) }}{{ result.lastName.charAt(0) }}
              </div>
              <div>
                <p class="font-semibold text-white">{{ result.firstName }} {{ result.lastName }}</p>
                <p class="text-xs text-gray-600">Checking Account</p>
              </div>
            </div>

            <div class="flex items-center justify-between bg-black/30 rounded-xl border border-white/[0.06] px-4 py-3">
              <span class="font-mono text-sm tracking-wider text-white">{{ result.iban }}</span>
              <button
                @click="copyIban(result.iban)"
                class="ml-3 p-1.5 rounded-lg transition-all flex-shrink-0"
                :class="copied === result.iban ? 'bg-[#00D9A3]/15 text-[#00D9A3]' : 'bg-white/[0.05] text-gray-500 hover:text-white hover:bg-white/10'"
              >
                <Check v-if="copied === result.iban" class="w-3.5 h-3.5" />
                <Copy v-else class="w-3.5 h-3.5" />
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
