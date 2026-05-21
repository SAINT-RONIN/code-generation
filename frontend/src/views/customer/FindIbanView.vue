<script setup>
import { ref } from 'vue'
import { RouterLink } from 'vue-router'
import { Search, Send } from 'lucide-vue-next'
import CustomerLayout from '../../components/CustomerLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import CopyChip from '../../components/ui/CopyChip.vue'
import VBtn from '../../components/ui/VBtn.vue'
import { searchByName } from '../../services/accounts'

const query = ref('')
const results = ref([])
const loading = ref(false)
const searched = ref(false)

let searchTimer = null

async function onInput() {
  searched.value = false
  results.value = []
  clearTimeout(searchTimer)
  if (!query.value.trim()) return

  searchTimer = setTimeout(async () => {
    loading.value = true
    try {
      const parts = query.value.trim().split(/\s+/)
      const firstName = parts[0]
      const lastName = parts.slice(1).join(' ')
      const iban = query.value.trim().replace(/\s+/g, '')
      const { data } = await searchByName(firstName, lastName, iban)
      results.value = data
    } catch {
      results.value = []
    } finally {
      loading.value = false
      searched.value = true
    }
  }, 400)
}
</script>

<template>
  <CustomerLayout>
    <VPageHeader eyebrow="Banking" title="Find IBAN" sub="Search by a customer's name to get their IBAN." />

    <div class="max-w-lg mb-6">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4" :style="{ color: 'var(--ink-3)' }" />
        <input
          v-model="query"
          type="text"
          placeholder="First name, or first and last name…"
          class="w-full h-11 pl-9 pr-4 text-sm rounded-xl border"
          :style="{ background: 'var(--surface)', color: 'var(--ink)', borderColor: 'var(--line-2)' }"
          @input="onInput"
        />
      </div>
      <p class="text-xs mt-2" :style="{ color: 'var(--ink-3)' }">Try "Test Customer" or just "Test"</p>
    </div>

    <div class="max-w-lg space-y-3">
      <div v-if="loading" class="space-y-3">
        <div v-for="i in 3" :key="i" class="skeleton h-16 rounded-2xl" />
      </div>

      <div
        v-else-if="searched && !results.length"
        class="text-sm py-8 text-center"
        :style="{ color: 'var(--ink-3)' }"
      >No customers found for "{{ query }}"</div>

      <div
        v-for="r in results"
        :key="r.checkingIban"
        class="rounded-2xl border p-4 flex items-center gap-4"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <div
          class="w-10 h-10 rounded-full flex items-center justify-center text-sm font-semibold flex-shrink-0"
          :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
        >{{ r.firstName[0] }}{{ r.lastName[0] }}</div>

        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium mb-1" :style="{ color: 'var(--ink)' }">{{ r.firstName }} {{ r.lastName }}</p>
          <CopyChip :value="r.checkingIban" />
        </div>

        <RouterLink :to="`/customer/transfer?mode=other`">
          <VBtn variant="softAccent" size="sm">
            <Send class="w-3.5 h-3.5" /> Send
          </VBtn>
        </RouterLink>
      </div>

      <p v-if="!query && !loading" class="text-sm" :style="{ color: 'var(--ink-3)' }">
        Start typing to search.
      </p>
    </div>
  </CustomerLayout>
</template>
