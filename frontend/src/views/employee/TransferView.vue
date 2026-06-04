<script setup>
import { ref, computed, onMounted } from 'vue'
import { transferBetweenCustomers, getAllAccounts } from '../../services/employee'
import { ArrowLeftRight, Check, Shield } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VField from '../../components/ui/VField.vue'
import VTextInput from '../../components/ui/VTextInput.vue'
import VBtn from '../../components/ui/VBtn.vue'
import { eur } from '../../utils/format'
import { extractError } from '../../utils/error'

const accounts = ref([])
const loadingAccounts = ref(true)

const fromIban = ref('')
const toIban = ref('')
const amount = ref('')
const note = ref('')
const error = ref('')
const loading = ref(false)
const success = ref(false)
const successData = ref(null)

function ownerName(acc) {
  if (!acc) return ''
  const u = acc.user
  if (!u) return acc.iban
  return `${u.firstName} ${u.lastName}`
}

onMounted(async () => {
  try {
    const { data } = await getAllAccounts({ active: true, accountType: 'CHECKING', size: 200, sort: 'iban,asc' })
    accounts.value = data.content ?? []
  } catch {
    error.value = 'Could not load customer accounts.'
  } finally {
    loadingAccounts.value = false
  }
})

const fromAccount = computed(() => accounts.value.find(a => a.iban === fromIban.value))
const toAccount = computed(() => accounts.value.find(a => a.iban === toIban.value))

const toOptions = computed(() => accounts.value.filter(a => a.iban !== fromIban.value))

async function executeTransfer() {
  error.value = ''
  if (!fromIban.value) { error.value = 'Select a source account.'; return }
  if (!toIban.value) { error.value = 'Select a destination account.'; return }
  if (!amount.value || parseFloat(amount.value) <= 0) { error.value = 'Enter a valid amount.'; return }
  if (!note.value.trim()) { error.value = 'An audit note is required.'; return }

  loading.value = true
  try {
    const res = await transferBetweenCustomers({
      fromIban: fromIban.value,
      toIban: toIban.value,
      amount: parseFloat(amount.value),
      description: note.value,
    })
    successData.value = res.data
    success.value = true
  } catch (e) {
    error.value = extractError(e, 'Transfer failed. Please check the details and try again.')
  } finally {
    loading.value = false
  }
}

function reset() {
  fromIban.value = ''
  toIban.value = ''
  amount.value = ''
  note.value = ''
  error.value = ''
  success.value = false
  successData.value = null
}
</script>

<template>
  <EmployeeLayout>
    <VPageHeader eyebrow="Staff portal" title="Initiate transfer" sub="Move funds between customer checking accounts." />

    <div class="max-w-lg">
      <div v-if="!success">
        <!-- Error -->
        <div
          v-if="error"
          class="mb-4 px-4 py-3 rounded-xl text-sm"
          :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }"
        >{{ error }}</div>

        <VCard>
          <div class="p-6 space-y-5">
            <!-- From -->
            <VField label="From account">
              <select
                v-model="fromIban"
                :disabled="loadingAccounts"
                class="w-full h-10 px-3 text-sm rounded-lg border"
                :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)', color: 'var(--ink)' }"
              >
                <option value="">{{ loadingAccounts ? 'Loading…' : 'Select account…' }}</option>
                <option v-for="a in accounts" :key="a.iban" :value="a.iban">
                  {{ ownerName(a) }} — {{ a.iban }} ({{ eur(a.balance) }})
                </option>
              </select>
            </VField>

            <div class="flex items-center gap-3">
              <div class="h-px flex-1" :style="{ background: 'var(--line)' }" />
              <div
                class="w-7 h-7 rounded-full flex items-center justify-center"
                :style="{ background: 'var(--surface-2)', color: 'var(--ink-3)' }"
              ><ArrowLeftRight class="w-3.5 h-3.5" /></div>
              <div class="h-px flex-1" :style="{ background: 'var(--line)' }" />
            </div>

            <!-- To -->
            <VField label="To account">
              <select
                v-model="toIban"
                :disabled="loadingAccounts || !fromIban"
                class="w-full h-10 px-3 text-sm rounded-lg border"
                :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)', color: 'var(--ink)' }"
              >
                <option value="">Select account…</option>
                <option v-for="a in toOptions" :key="a.iban" :value="a.iban">
                  {{ ownerName(a) }} — {{ a.iban }} ({{ eur(a.balance) }})
                </option>
              </select>
            </VField>

            <!-- Amount -->
            <VField label="Amount">
              <div
                class="flex items-center gap-2 h-20 px-5 rounded-xl border"
                :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)' }"
              >
                <span class="text-3xl font-light leading-none" :style="{ color: 'var(--ink-3)' }">€</span>
                <input
                  v-model="amount"
                  type="number"
                  placeholder="0,00"
                  step="0.01"
                  class="flex-1 h-full text-4xl font-display bg-transparent focus:outline-none tabnum leading-none"
                  style="border: none; padding: 0;"
                  :style="{ color: 'var(--ink)' }"
                />
              </div>
            </VField>

            <!-- Note -->
            <VField label="Audit note" hint="Required for compliance">
              <VTextInput v-model="note" placeholder="Reason for this transfer" />
            </VField>

            <!-- Audit info -->
            <div class="flex items-start gap-3 rounded-xl p-4" :style="{ background: 'var(--surface-2)' }">
              <Shield class="w-4 h-4 mt-0.5 flex-shrink-0" :style="{ color: 'var(--accent)' }" />
              <p class="text-xs leading-relaxed" :style="{ color: 'var(--ink-2)' }">
                This transfer is logged under your employee account for audit purposes. Customer transaction limits apply.
              </p>
            </div>

            <VBtn variant="primary" size="lg" class="w-full" :disabled="loading || loadingAccounts" @click="executeTransfer">
              <ArrowLeftRight class="w-4 h-4" />
              {{ loading ? 'Processing…' : 'Execute transfer' }}
            </VBtn>
          </div>
        </VCard>
      </div>

      <!-- Success -->
      <div v-else class="text-center py-10">
        <div
          class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6"
          :style="{ background: 'var(--accent-soft)' }"
        >
          <Check class="w-10 h-10" :style="{ color: 'var(--accent)' }" />
        </div>
        <h2 class="font-display text-3xl mb-2" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">Transfer complete</h2>
        <p class="text-sm mb-1" :style="{ color: 'var(--ink-2)' }">
          {{ eur(amount) }} moved from {{ ownerName(fromAccount) }} to {{ ownerName(toAccount) }}
        </p>
        <p class="text-xs mb-8" :style="{ color: 'var(--ink-3)' }">Logged to the audit trail.</p>
        <VBtn variant="secondary" size="md" @click="reset">Make another transfer</VBtn>
      </div>
    </div>
  </EmployeeLayout>
</template>
