<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { transfer } from '../../services/transactions'
import { getMyAccounts } from '../../services/accounts'
import { searchByName } from '../../services/accounts'
import { Check, ArrowDown } from 'lucide-vue-next'
import CustomerLayout from '../../components/CustomerLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import VField from '../../components/ui/VField.vue'
import VTextInput from '../../components/ui/VTextInput.vue'
import VBtn from '../../components/ui/VBtn.vue'
function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0) }

const route = useRoute()
const mode = computed(() => route.query.mode === 'own' ? 'own' : 'other')

const accounts = ref([])
const loadingAccounts = ref(true)

const step = ref(1)
const fromIban = ref('')
const toIban = ref('')
const recipientSearch = ref('')
const recipientResults = ref([])
const selectedRecipient = ref(null)
const amount = ref('')
const note = ref('')
const error = ref('')
const loading = ref(false)
const searching = ref(false)

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data
    fromIban.value = data[0]?.iban ?? ''
  } catch {
    error.value = 'Could not load your accounts.'
  } finally {
    loadingAccounts.value = false
  }
})

const fromAccount = computed(() => accounts.value.find(a => a.iban === fromIban.value))
const otherAccount = computed(() => accounts.value.find(a => a.iban !== fromIban.value))

let searchTimer = null
async function onSearchInput() {
  selectedRecipient.value = null
  toIban.value = ''
  clearTimeout(searchTimer)
  if (!recipientSearch.value.trim()) { recipientResults.value = []; return }
  searchTimer = setTimeout(async () => {
    searching.value = true
    try {
      const parts = recipientSearch.value.trim().split(/\s+/)
      const firstName = parts[0]
      const lastName = parts.slice(1).join(' ')
      const iban = recipientSearch.value.trim().replace(/\s+/g, '')
      const { data } = await searchByName(firstName, lastName, iban)
      recipientResults.value = data
    } catch {
      recipientResults.value = []
    } finally {
      searching.value = false
    }
  }, 350)
}

function selectRecipient(r) {
  selectedRecipient.value = r
  toIban.value = r.checkingIban
  recipientSearch.value = `${r.firstName} ${r.lastName}`
  recipientResults.value = []
}

function goReview() {
  error.value = ''
  if (!amount.value || parseFloat(amount.value) <= 0) { error.value = 'Enter a valid amount.'; return }
  if (mode.value === 'own') {
    if (!otherAccount.value) { error.value = 'No second account found.'; return }
    toIban.value = otherAccount.value.iban
  } else if (!toIban.value) {
    error.value = 'Select a recipient.'
    return
  }
  step.value = 2
}

async function confirmTransfer() {
  error.value = ''
  loading.value = true
  try {
    await transfer({
      fromIban: fromIban.value,
      toIban: toIban.value,
      amount: parseFloat(amount.value),
      description: note.value || 'Transfer',
    })
    await refreshAccounts()
    step.value = 3
  } catch (e) {
    error.value = e?.response?.data?.message || 'Transfer failed. Please try again.'
    step.value = 1
  } finally {
    loading.value = false
  }
}

async function refreshAccounts() {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data
  } catch { /* keep stale data if refresh fails */ }
}

async function reset() {
  step.value = 1
  amount.value = ''
  note.value = ''
  toIban.value = ''
  selectedRecipient.value = null
  recipientSearch.value = ''
  recipientResults.value = []
  error.value = ''
  await refreshAccounts()
}
</script>

<template>
  <CustomerLayout>
    <VPageHeader
      eyebrow="Banking"
      :title="mode === 'own' ? 'Move money' : 'Send to someone'"
    />

    <div class="max-w-lg">
      <!-- Step 1: Compose -->
      <div v-if="step === 1" class="space-y-5">
        <div v-if="error" class="px-4 py-3 rounded-xl text-sm"
          :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">
          {{ error }}
        </div>

        <!-- From account -->
        <VField label="From account">
          <div class="space-y-2 mt-1">
            <button
              v-for="account in accounts"
              :key="account.iban"
              type="button"
              class="w-full flex items-center gap-3 p-4 rounded-xl border text-left transition-colors"
              :style="fromIban === account.iban
                ? { borderColor: 'var(--accent)', background: 'var(--accent-soft)' }
                : { borderColor: 'var(--line-2)', background: 'var(--surface)' }"
              @click="fromIban = account.iban"
            >
              <div class="w-8 h-8 rounded-lg flex-shrink-0 flex items-center justify-center"
                :style="{ background: 'var(--surface-2)' }">
                <div class="w-3 h-3 rounded-full" :style="{ background: 'var(--accent)' }" />
              </div>
              <div class="flex-1 min-w-0">
                <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ account.accountType }}</p>
                <p class="text-xs font-mono truncate" :style="{ color: 'var(--ink-3)' }">{{ account.iban }}</p>
              </div>
              <p class="text-sm font-medium tabnum flex-shrink-0" :style="{ color: 'var(--ink)' }">{{ eur(account.balance) }}</p>
            </button>
          </div>
        </VField>

        <div class="flex items-center gap-3">
          <div class="h-px flex-1" :style="{ background: 'var(--line)' }" />
          <div class="w-7 h-7 rounded-full flex items-center justify-center"
            :style="{ background: 'var(--surface-2)', color: 'var(--ink-3)' }">
            <ArrowDown class="w-3.5 h-3.5" />
          </div>
          <div class="h-px flex-1" :style="{ background: 'var(--line)' }" />
        </div>

        <!-- Recipient (other) -->
        <template v-if="mode === 'other'">
          <VField label="Recipient">
            <VTextInput
              v-model="recipientSearch"
              placeholder="Search by name…"
              @input="onSearchInput"
            />
            <div v-if="searching" class="mt-1 text-xs px-3" :style="{ color: 'var(--ink-3)' }">Searching…</div>
            <div
              v-else-if="recipientResults.length"
              class="mt-1 rounded-xl border overflow-hidden"
              :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
            >
              <button
                v-for="r in recipientResults"
                :key="r.checkingIban"
                type="button"
                class="w-full flex items-center gap-3 px-4 py-3 text-left text-sm row"
                @click="selectRecipient(r)"
              >
                <div class="w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-semibold flex-shrink-0"
                  :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }">
                  {{ r.firstName[0] }}{{ r.lastName[0] }}
                </div>
                <div class="flex-1 min-w-0">
                  <p class="font-medium truncate" :style="{ color: 'var(--ink)' }">{{ r.firstName }} {{ r.lastName }}</p>
                  <p class="text-xs font-mono truncate" :style="{ color: 'var(--ink-3)' }">{{ r.checkingIban }}</p>
                </div>
              </button>
            </div>
            <div
              v-else-if="recipientSearch && !selectedRecipient"
              class="mt-1 text-xs px-1" :style="{ color: 'var(--ink-3)' }"
            >No customers found.</div>
          </VField>
        </template>

        <!-- Own account destination -->
        <template v-else>
          <VField label="To account">
            <div class="h-10 px-3 flex items-center rounded-lg text-sm border"
              :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink-2)' }">
              <template v-if="otherAccount">
                {{ otherAccount.accountType }} · {{ otherAccount.iban }}
              </template>
              <template v-else>No other account available</template>
            </div>
          </VField>
        </template>

        <!-- Amount -->
        <VField label="Amount">
          <div class="flex items-center gap-2 h-20 px-5 rounded-xl border"
            :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)' }">
            <span class="text-3xl font-light leading-none" :style="{ color: 'var(--ink-3)' }">€</span>
            <input
              v-model="amount" type="number" placeholder="0,00" step="0.01" min="0.01"
              class="flex-1 h-full text-4xl font-display bg-transparent focus:outline-none tabnum leading-none"
              style="border: none; padding: 0;"
              :style="{ color: 'var(--ink)' }"
            />
          </div>
          <p v-if="fromAccount" class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">
            Available: {{ eur(fromAccount.balance) }}
          </p>
        </VField>

        <VField label="Note" hint="Optional">
          <VTextInput v-model="note" placeholder="What's this for?" />
        </VField>

        <VBtn variant="primary" size="lg" class="w-full" @click="goReview">Review transfer</VBtn>
      </div>

      <!-- Step 2: Review -->
      <div v-if="step === 2">
        <VCard>
          <div class="p-6 space-y-4">
            <div class="flex items-center justify-between">
              <span class="text-sm" :style="{ color: 'var(--ink-3)' }">Amount</span>
              <span class="text-2xl font-display tabnum" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
                {{ eur(amount) }}
              </span>
            </div>
            <div class="h-px" :style="{ background: 'var(--line)' }" />
            <div class="flex items-start justify-between">
              <span class="text-sm" :style="{ color: 'var(--ink-3)' }">From</span>
              <div class="text-right">
                <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ fromAccount?.accountType }}</p>
                <p class="text-xs font-mono" :style="{ color: 'var(--ink-3)' }">{{ fromIban }}</p>
              </div>
            </div>
            <div class="flex items-start justify-between">
              <span class="text-sm" :style="{ color: 'var(--ink-3)' }">To</span>
              <div class="text-right">
                <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">
                  {{ selectedRecipient ? `${selectedRecipient.firstName} ${selectedRecipient.lastName}` : (otherAccount?.accountType ?? 'External') }}
                </p>
                <p class="text-xs font-mono" :style="{ color: 'var(--ink-3)' }">{{ toIban }}</p>
              </div>
            </div>
            <div v-if="note" class="flex justify-between">
              <span class="text-sm" :style="{ color: 'var(--ink-3)' }">Note</span>
              <span class="text-sm" :style="{ color: 'var(--ink)' }">{{ note }}</span>
            </div>
          </div>
        </VCard>
        <div v-if="error" class="mt-4 px-4 py-3 rounded-xl text-sm"
          :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)' }">{{ error }}</div>
        <div class="flex gap-3 mt-4">
          <VBtn variant="secondary" size="lg" class="flex-1" @click="step = 1">Back</VBtn>
          <VBtn variant="primary" size="lg" class="flex-[2]" :disabled="loading" @click="confirmTransfer">
            {{ loading ? 'Processing…' : 'Confirm transfer' }}
          </VBtn>
        </div>
      </div>

      <!-- Step 3: Success -->
      <div v-if="step === 3" class="text-center py-10">
        <div class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6"
          :style="{ background: 'var(--accent-soft)' }">
          <Check class="w-10 h-10" :style="{ color: 'var(--accent)' }" />
        </div>
        <h2 class="font-display text-3xl mb-2" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">Done!</h2>
        <p class="text-sm mb-8" :style="{ color: 'var(--ink-2)' }">{{ eur(amount) }} sent.</p>
        <div class="flex gap-3 justify-center">
          <VBtn variant="secondary" size="md" @click="reset">Make another</VBtn>
          <RouterLink to="/customer">
            <VBtn variant="primary" size="md">Go to dashboard</VBtn>
          </RouterLink>
        </div>
      </div>
    </div>
  </CustomerLayout>
</template>
