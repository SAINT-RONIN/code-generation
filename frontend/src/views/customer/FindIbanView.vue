<script setup>
import { ref } from 'vue'
import CustomerLayout from '../../components/CustomerLayout.vue'
import { searchByName } from '../../services/accounts'
import { Copy, Check } from 'lucide-vue-next'

const firstName = ref('')
const lastName = ref('')
const results = ref([])
const searched = ref(false)
const copied = ref('')

async function search() {
  if (!firstName.value || !lastName.value) return
  searched.value = true
  const { data } = await searchByName(firstName.value, lastName.value)
  results.value = data
}

function copyIban(iban) {
  navigator.clipboard.writeText(iban)
  copied.value = iban
  setTimeout(() => (copied.value = ''), 2000)
}
</script>

<template>
  <CustomerLayout>
    <div class="max-w-2xl mx-auto">
      <h1 class="text-3xl font-bold tracking-tight text-white mb-2">Find IBAN</h1>
      <p class="text-gray-400 mb-8">Search for customers by their name to get their checking account IBAN.</p>

      <form @submit.prevent="search" class="flex gap-3 mb-8">
        <input
          v-model="firstName"
          type="text"
          placeholder="First name"
          required
          class="flex-1 bg-[#14141A] border border-white/5 rounded-2xl py-4 px-5 text-white focus:outline-none focus:border-[#7B61FF] transition-colors placeholder:text-gray-600 text-lg"
        />
        <input
          v-model="lastName"
          type="text"
          placeholder="Last name"
          required
          class="flex-1 bg-[#14141A] border border-white/5 rounded-2xl py-4 px-5 text-white focus:outline-none focus:border-[#7B61FF] transition-colors placeholder:text-gray-600 text-lg"
        />
        <button type="submit" class="bg-[#7B61FF] hover:bg-[#6A52E5] text-white px-6 rounded-2xl font-medium transition-colors">
          Search
        </button>
      </form>

      <div v-if="searched && results.length === 0" class="text-center py-12 text-gray-500">
        <p>No customers found.</p>
      </div>

      <div v-if="results.length > 0" class="space-y-4">
        <div
          v-for="result in results"
          :key="result.iban"
          class="bg-[#14141A] border border-[#7B61FF]/30 rounded-2xl p-6"
        >
          <p class="text-xl font-bold text-white mb-4">{{ result.firstName }} {{ result.lastName }}</p>
          <div class="flex items-center justify-between bg-black/20 p-4 rounded-xl border border-white/5">
            <span class="font-mono text-lg tracking-wider text-white">{{ result.iban }}</span>
            <button
              @click="copyIban(result.iban)"
              class="p-2 rounded-lg transition-colors"
              :class="copied === result.iban ? 'bg-[#00D9A3]/20 text-[#00D9A3]' : 'bg-white/5 text-gray-400 hover:text-white hover:bg-white/10'"
            >
              <Check v-if="copied === result.iban" class="w-5 h-5" />
              <Copy v-else class="w-5 h-5" />
            </button>
          </div>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
