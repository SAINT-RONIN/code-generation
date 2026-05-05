<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Users } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VPill from '../../components/ui/VPill.vue'
import { getCustomers } from '../../services/employee'

const router = useRouter()
const customers = ref([])
const loading = ref(true)
const error = ref('')
const query = ref('')

onMounted(async () => {
  try {
    const { data } = await getCustomers({ status: 'ACTIVE', size: 200, sort: 'id,asc' })
    customers.value = data.content ?? []
  } catch {
    error.value = 'Failed to load customers.'
  } finally {
    loading.value = false
  }
})

const filtered = computed(() => {
  if (!query.value.trim()) return customers.value
  const q = query.value.toLowerCase()
  return customers.value.filter(c =>
    `${c.firstName} ${c.lastName}`.toLowerCase().includes(q) ||
    c.email.toLowerCase().includes(q)
  )
})

function initials(c) {
  return `${c.firstName[0] ?? ''}${c.lastName[0] ?? ''}`.toUpperCase()
}

function statusTone(status) {
  if (status === 'ACTIVE') return 'success'
  if (status === 'CLOSED') return 'danger'
  return 'warn'
}
</script>

<template>
  <EmployeeLayout>
    <VPageHeader
      eyebrow="Staff portal"
      title="Customers"
      :sub="loading ? '' : `${filtered.length} customer${filtered.length !== 1 ? 's' : ''}`"
    />

    <div v-if="error" class="mb-6 px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">{{ error }}</div>

    <!-- Search -->
    <div class="relative max-w-sm mb-6">
      <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4" :style="{ color: 'var(--ink-3)' }" />
      <input
        v-model="query"
        type="text"
        placeholder="Search by name or email…"
        class="w-full h-10 pl-9 pr-4 text-sm rounded-xl border"
        :style="{ background: 'var(--surface)', color: 'var(--ink)', borderColor: 'var(--line-2)' }"
      />
    </div>

    <!-- Loading skeleton -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 5" :key="i" class="skeleton h-16 rounded-xl" />
    </div>

    <template v-else>
      <div v-if="!filtered.length" class="rounded-2xl border py-20 text-center"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }">
        <Users class="w-10 h-10 mx-auto mb-3" :style="{ color: 'var(--ink-3)' }" />
        <p class="text-base font-medium" :style="{ color: 'var(--ink)' }">No customers found</p>
        <p class="text-sm mt-1" :style="{ color: 'var(--ink-3)' }">{{ query ? 'Try a different search.' : 'Approved customers will appear here.' }}</p>
      </div>

      <VCard v-else>
        <div class="overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="border-b" :style="{ borderColor: 'var(--line)' }">
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider" :style="{ color: 'var(--ink-3)' }">Customer</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider hidden md:table-cell" :style="{ color: 'var(--ink-3)' }">BSN</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider hidden md:table-cell" :style="{ color: 'var(--ink-3)' }">Phone</th>
                <th class="px-5 py-3 text-[11px] font-semibold uppercase tracking-wider" :style="{ color: 'var(--ink-3)' }">Status</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="c in filtered"
                :key="c.id"
                class="row cursor-pointer border-b"
                :style="{ borderColor: 'var(--line)' }"
                @click="router.push(`/employee/customers/${c.id}`)"
              >
                <td class="px-5 py-4">
                  <div class="flex items-center gap-3">
                    <div class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0"
                      :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }">
                      {{ initials(c) }}
                    </div>
                    <div>
                      <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ c.firstName }} {{ c.lastName }}</p>
                      <p class="text-xs" :style="{ color: 'var(--ink-3)' }">{{ c.email }}</p>
                    </div>
                  </div>
                </td>
                <td class="px-5 py-4 hidden md:table-cell">
                  <span class="font-mono text-xs" :style="{ color: 'var(--ink-2)' }">{{ c.bsn }}</span>
                </td>
                <td class="px-5 py-4 hidden md:table-cell text-sm" :style="{ color: 'var(--ink-2)' }">
                  {{ c.phoneNumber }}
                </td>
                <td class="px-5 py-4">
                  <VPill :tone="statusTone(c.status)">{{ c.status.toLowerCase() }}</VPill>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </VCard>
    </template>
  </EmployeeLayout>
</template>
