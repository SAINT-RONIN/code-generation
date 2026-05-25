<script setup>
import { ref, onMounted } from 'vue'
import { Check, X, CheckSquare } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VPill from '../../components/ui/VPill.vue'
import VBtn from '../../components/ui/VBtn.vue'
import VModal from '../../components/ui/VModal.vue'
import VField from '../../components/ui/VField.vue'
import VTextInput from '../../components/ui/VTextInput.vue'
import { getCustomers, updateCustomer } from '../../services/employee'
import { extractError } from '../../utils/error'

const pending = ref([])
const loading = ref(true)
const error = ref('')

const selected = ref(null)
const dailyLimit = ref('2000')
const absoluteLimit = ref('0')
const approving = ref(false)
const approved = ref(false)

onMounted(async () => {
  await loadPending()
})

async function loadPending() {
  loading.value = true
  error.value = ''
  try {
    const { data } = await getCustomers({ status: 'PENDING', size: 200 })
    pending.value = data.content ?? []
  } catch {
    error.value = 'Could not load pending registrations.'
  } finally {
    loading.value = false
  }
}

function openReview(p) {
  selected.value = p
  dailyLimit.value = '2000'
  absoluteLimit.value = '0'
  approved.value = false
}

async function handleApprove() {
  approving.value = true
  try {
    await updateCustomer(selected.value.id, {
      status: 'ACTIVE',
      dailyLimit: parseFloat(dailyLimit.value),
      absoluteLimit: parseFloat(absoluteLimit.value),
    })
    pending.value = pending.value.filter(p => p.id !== selected.value.id)
    approved.value = true
  } catch (e) {
    error.value = extractError(e, 'Approval failed. Please try again.')
    selected.value = null
  } finally {
    approving.value = false
  }
}
</script>

<template>
  <EmployeeLayout>
    <VPageHeader
      eyebrow="Staff portal"
      title="Approvals"
      :sub="loading ? '' : `${pending.length} registration${pending.length !== 1 ? 's' : ''} pending review`"
    />

    <!-- Error -->
    <div
      v-if="error"
      class="mb-6 px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
    >{{ error }}</div>

    <!-- Loading -->
    <div v-if="loading" class="space-y-2">
      <div v-for="i in 4" :key="i" class="skeleton h-16 rounded-xl" />
    </div>

    <!-- Empty state -->
    <div
      v-else-if="!pending.length"
      class="rounded-2xl border py-20 text-center"
      :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
    >
      <div
        class="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4"
        :style="{ background: 'var(--accent-soft)' }"
      >
        <CheckSquare class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
      </div>
      <p class="text-base font-medium" :style="{ color: 'var(--ink)' }">All caught up!</p>
      <p class="text-sm mt-1" :style="{ color: 'var(--ink-3)' }">No pending registrations at this time.</p>
    </div>

    <!-- List -->
    <VCard v-else>
      <div class="divide-y" :style="{ borderColor: 'var(--line)' }">
        <div
          v-for="p in pending"
          :key="p.id"
          class="flex items-center gap-4 px-5 py-4 row cursor-pointer"
          @click="openReview(p)"
        >
          <div
            class="w-9 h-9 rounded-full flex items-center justify-center text-xs font-semibold flex-shrink-0"
            :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
          >{{ p.firstName[0] }}{{ p.lastName[0] }}</div>

          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ p.firstName }} {{ p.lastName }}</p>
            <p class="text-xs truncate" :style="{ color: 'var(--ink-3)' }">{{ p.email }}</p>
          </div>

          <VPill tone="warn">Pending</VPill>

          <button
            class="w-8 h-8 rounded-lg flex items-center justify-center lift"
            :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
            @click.stop="openReview(p)"
          >
            <Check class="w-4 h-4" />
          </button>
        </div>
      </div>
    </VCard>

    <!-- Review modal -->
    <VModal :open="!!selected" @close="selected = null">
      <div v-if="selected" class="p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold" :style="{ color: 'var(--ink)' }">Review registration</h2>
          <button @click="selected = null" :style="{ color: 'var(--ink-3)' }"><X class="w-5 h-5" /></button>
        </div>

        <div v-if="!approved">
          <!-- Customer info -->
          <div class="rounded-xl p-4 mb-5 space-y-3" :style="{ background: 'var(--surface-2)' }">
            <div class="flex justify-between text-sm">
              <span :style="{ color: 'var(--ink-3)' }">Name</span>
              <span class="font-medium" :style="{ color: 'var(--ink)' }">{{ selected.firstName }} {{ selected.lastName }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span :style="{ color: 'var(--ink-3)' }">Email</span>
              <span :style="{ color: 'var(--ink)' }">{{ selected.email }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span :style="{ color: 'var(--ink-3)' }">BSN</span>
              <span class="font-mono" :style="{ color: 'var(--ink)' }">{{ selected.bsn }}</span>
            </div>
            <div class="flex justify-between text-sm">
              <span :style="{ color: 'var(--ink-3)' }">Phone</span>
              <span :style="{ color: 'var(--ink)' }">{{ selected.phoneNumber }}</span>
            </div>
          </div>

          <!-- Limits -->
          <div class="grid grid-cols-2 gap-3 mb-5">
            <VField label="Daily limit (€)">
              <VTextInput v-model="dailyLimit" type="number" placeholder="2000" />
            </VField>
            <VField label="Absolute limit (€)">
              <VTextInput v-model="absoluteLimit" type="number" placeholder="0" />
            </VField>
          </div>

          <div class="flex gap-3">
            <VBtn variant="secondary" size="md" class="flex-1" @click="selected = null">Cancel</VBtn>
            <VBtn variant="primary" size="md" class="flex-[2]" :disabled="approving" @click="handleApprove">
              {{ approving ? 'Approving…' : 'Approve & create accounts' }}
            </VBtn>
          </div>
        </div>

        <div v-else class="text-center py-6">
          <div
            class="w-14 h-14 rounded-full flex items-center justify-center mx-auto mb-4"
            :style="{ background: 'var(--accent-soft)' }"
          >
            <Check class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
          </div>
          <p class="font-semibold mb-1" :style="{ color: 'var(--ink)' }">{{ selected.firstName }} {{ selected.lastName }} approved</p>
          <p class="text-sm mb-4" :style="{ color: 'var(--ink-3)' }">Checking and savings accounts created.</p>
          <VBtn variant="primary" size="md" @click="selected = null">Done</VBtn>
        </div>
      </div>
    </VModal>
  </EmployeeLayout>
</template>
