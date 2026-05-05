<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { ArrowLeft, AlignLeft, Send, Shield } from 'lucide-vue-next'
import EmployeeLayout from '../../components/EmployeeLayout.vue'
import VCard from '../../components/ui/VCard.vue'
import VPill from '../../components/ui/VPill.vue'
import VBtn from '../../components/ui/VBtn.vue'
import VField from '../../components/ui/VField.vue'
import VTextInput from '../../components/ui/VTextInput.vue'
import VModal from '../../components/ui/VModal.vue'
import CopyChip from '../../components/ui/CopyChip.vue'
import ActivityRow from '../../components/ActivityRow.vue'
import { getAllAccounts, updateAccount, updateCustomer } from '../../services/employee'
import { getTransactions } from '../../services/transactions'

const route = useRoute()
const customerId = Number(route.params.id)

const customer = ref(null)
const accounts = ref([])
const transactions = ref([])
const loading = ref(true)
const error = ref('')
const tab = ref('overview')
const confirmClose = ref(false)
const closeText = ref('')
const closing = ref(false)
const editLimits = ref({ daily: '', absolute: '' })
const savingLimits = ref(false)
const toast = ref('')

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}

const primary = computed(() => accounts.value[0] ?? null)
const allIbans = computed(() => accounts.value.map(a => a.iban))
const totalBalance = computed(() => accounts.value.reduce((s, a) => s + Number(a.balance), 0))
const allClosed = computed(() => accounts.value.length > 0 && accounts.value.every(a => !a.active))
const customerName = computed(() => customer.value ? `${customer.value.firstName} ${customer.value.lastName}` : '')
const initials = computed(() =>
  customerName.value.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase()
)

onMounted(async () => {
  try {
    // Get customer info
    const { data: cusData } = await getAllAccounts({ size: 500, sort: 'iban,asc' })
    const allAcc = cusData.content ?? []
    // Filter accounts for this customer (by user.id)
    accounts.value = allAcc.filter(a => a.user?.id === customerId)

    // Get customer object from first account's user
    if (accounts.value[0]?.user) {
      customer.value = accounts.value[0].user
    }

    if (accounts.value[0]) {
      const txRes = await getTransactions({ size: 10, sort: 'timestamp,desc', iban: accounts.value[0].iban })
      transactions.value = txRes.data.content ?? []
      editLimits.value = { daily: accounts.value[0].dailyLimit, absolute: accounts.value[0].absoluteLimit }
    }
  } catch {
    error.value = 'Could not load customer details.'
  }
  finally { loading.value = false }
})

async function saveLimits() {
  savingLimits.value = true
  try {
    for (const acc of accounts.value) {
      await updateAccount(acc.iban, {
        dailyLimit: parseFloat(editLimits.value.daily),
        absoluteLimit: parseFloat(editLimits.value.absolute),
      })
    }
    await refreshAccounts()
    toast.value = 'Limits updated'
    setTimeout(() => { toast.value = '' }, 2500)
  } catch (e) {
    const msg = e?.response?.data?.message || e?.response?.data || 'Failed to save limits'
    toast.value = typeof msg === 'string' ? msg : 'Failed to save limits'
    setTimeout(() => { toast.value = '' }, 3000)
  } finally { savingLimits.value = false }
}

async function handleClose() {
  if (closeText.value !== 'CLOSE') return
  closing.value = true
  try {
    await updateCustomer(customerId, { status: 'CLOSED' })
    confirmClose.value = false
    toast.value = 'All accounts closed — customer login disabled'
    setTimeout(() => { toast.value = '' }, 3000)
    await refreshAccounts()
  } catch {
    toast.value = 'Failed to close accounts'
    setTimeout(() => { toast.value = '' }, 2500)
  } finally { closing.value = false }
}

const reopening = ref(false)
async function handleReopen() {
  reopening.value = true
  try {
    await updateCustomer(customerId, { status: 'ACTIVE' })
    toast.value = 'Accounts reopened — customer can log in again'
    setTimeout(() => { toast.value = '' }, 3000)
    await refreshAccounts()
  } catch {
    toast.value = 'Failed to reopen accounts'
    setTimeout(() => { toast.value = '' }, 2500)
  } finally { reopening.value = false }
}

async function refreshAccounts() {
  const { data } = await getAllAccounts({ size: 500, sort: 'iban,asc' })
  const all = data.content ?? []
  accounts.value = all.filter(a => a.user?.id === customerId)
}

const TABS = ['overview', 'accounts', 'transactions', 'limits']
</script>

<template>
  <EmployeeLayout>

    <!-- Back link -->
    <RouterLink
      to="/employee/customers"
      class="inline-flex items-center gap-2 text-sm font-medium mb-8 no-underline lift"
      :style="{ color: 'var(--ink-3)' }"
    >
      <ArrowLeft class="w-4 h-4" />
      All customers
    </RouterLink>

    <!-- Loading -->
    <div v-if="loading" class="space-y-6">
      <div class="skeleton h-28 rounded-2xl" />
      <div class="grid grid-cols-3 gap-5">
        <div v-for="i in 3" :key="i" class="skeleton h-32 rounded-2xl" />
      </div>
    </div>

    <!-- Error -->
    <div v-else-if="error" class="px-5 py-4 rounded-2xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">
      {{ error }}
    </div>

    <template v-else-if="primary || customer">

      <!-- ── Hero header ─────────────────────────────── -->
      <div
        class="rounded-2xl p-8 mb-8 flex flex-col sm:flex-row sm:items-center justify-between gap-6"
        :style="{ background: 'var(--surface)', border: '1px solid var(--line)' }"
      >
        <div class="flex items-center gap-5">
          <!-- Avatar -->
          <div
            class="w-16 h-16 rounded-2xl flex items-center justify-center font-display text-2xl flex-shrink-0"
            :style="{ background: 'var(--accent)', color: 'var(--accent-ink)', fontWeight: 400 }"
          >{{ initials }}</div>

          <div>
            <h1
              class="font-display tracking-tight leading-none mb-1"
              style="font-size: 32px; font-weight: 400;"
              :style="{ color: 'var(--ink)' }"
            >{{ customerName }}</h1>
            <p class="text-sm" :style="{ color: 'var(--ink-3)' }">{{ customer?.email }}</p>
            <div class="flex items-center gap-2 mt-2">
              <VPill :tone="allClosed ? 'danger' : 'success'">
                {{ allClosed ? 'Suspended' : 'Active' }}
              </VPill>
              <span class="text-xs" :style="{ color: 'var(--ink-3)' }">
                {{ accounts.length }} account{{ accounts.length !== 1 ? 's' : '' }}
              </span>
            </div>
          </div>
        </div>

        <!-- Total + actions -->
        <div class="flex flex-col sm:items-end gap-4">
          <div>
            <p class="text-xs font-medium uppercase tracking-[.12em] mb-1" :style="{ color: 'var(--ink-3)' }">Total balance</p>
            <p class="font-display tabnum" style="font-size: 36px; font-weight: 300; line-height: 1;" :style="{ color: 'var(--ink)' }">
              {{ eur(totalBalance) }}
            </p>
          </div>
          <div class="flex gap-2 flex-wrap">
            <RouterLink to="/employee/transfer">
              <VBtn variant="secondary" size="sm">
                <Send class="w-3.5 h-3.5" /> Initiate transfer
              </VBtn>
            </RouterLink>

            <!-- Reopen — shown when all accounts are closed -->
            <VBtn
              v-if="allClosed"
              size="sm"
              :disabled="reopening"
              style="background: var(--credit); color: #fff;"
              @click="handleReopen"
            >
              <span class="w-2 h-2 rounded-full bg-white/80 mr-1 inline-block" />
              {{ reopening ? 'Reopening…' : 'Reopen accounts' }}
            </VBtn>

            <!-- Close — shown when any account is active -->
            <VBtn
              v-else
              variant="destructive"
              size="sm"
              @click="confirmClose = true; closeText = ''"
            >
              Close accounts
            </VBtn>
          </div>
        </div>
      </div>

      <!-- ── Tabs ───────────────────────────────────── -->
      <div class="flex border-b mb-8" :style="{ borderColor: 'var(--line)' }">
        <button
          v-for="t in TABS"
          :key="t"
          class="h-11 px-5 text-sm font-medium capitalize transition-colors"
          :style="{
            color: tab === t ? 'var(--ink)' : 'var(--ink-3)',
            borderBottom: `2px solid ${tab === t ? 'var(--accent)' : 'transparent'}`,
            marginBottom: '-1px',
          }"
          @click="tab = t"
        >{{ t }}</button>
      </div>

      <!-- ── Overview ───────────────────────────────── -->
      <div v-if="tab === 'overview'" class="space-y-5">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-5">

          <!-- Customer info -->
          <VCard>
            <div class="p-6">
              <p class="text-[11px] font-semibold uppercase tracking-[.14em] mb-4" :style="{ color: 'var(--ink-3)' }">
                Customer info
              </p>
              <div class="space-y-3">
                <div>
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Name</p>
                  <p class="text-sm font-medium" :style="{ color: 'var(--ink)' }">{{ customerName }}</p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Email</p>
                  <p class="text-sm truncate" :style="{ color: 'var(--ink)' }">{{ customer?.email }}</p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Accounts</p>
                  <p class="text-sm" :style="{ color: 'var(--ink)' }">{{ accounts.length }}</p>
                </div>
              </div>
            </div>
          </VCard>

          <!-- Balances -->
          <VCard>
            <div class="p-6">
              <p class="text-[11px] font-semibold uppercase tracking-[.14em] mb-4" :style="{ color: 'var(--ink-3)' }">
                Balances
              </p>
              <div class="space-y-3">
                <div v-for="acc in accounts" :key="acc.iban">
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">
                    {{ acc.accountType }}
                  </p>
                  <p class="font-display tabnum" style="font-size: 22px; font-weight: 400; line-height: 1.1;" :style="{ color: 'var(--ink)' }">
                    {{ eur(acc.balance) }}
                  </p>
                </div>
                <div class="pt-3 mt-1 border-t" :style="{ borderColor: 'var(--line)' }">
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Total</p>
                  <p class="font-display tabnum" style="font-size: 26px; font-weight: 400; line-height: 1.1;" :style="{ color: 'var(--ink)' }">
                    {{ eur(totalBalance) }}
                  </p>
                </div>
              </div>
            </div>
          </VCard>

          <!-- Limits -->
          <VCard>
            <div class="p-6">
              <div class="flex items-center justify-between mb-4">
                <p class="text-[11px] font-semibold uppercase tracking-[.14em]" :style="{ color: 'var(--ink-3)' }">
                  Transfer limits
                </p>
                <button
                  class="text-xs font-medium"
                  :style="{ color: 'var(--accent)' }"
                  @click="tab = 'limits'"
                >Edit</button>
              </div>
              <div class="space-y-3">
                <div>
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Daily limit</p>
                  <p class="font-display tabnum" style="font-size: 22px; font-weight: 400; line-height: 1.1;" :style="{ color: 'var(--ink)' }">
                    {{ eur(primary?.dailyLimit) }}
                  </p>
                </div>
                <div>
                  <p class="text-[11px] uppercase tracking-[.1em] mb-0.5" :style="{ color: 'var(--ink-3)' }">Absolute limit</p>
                  <p class="font-display tabnum" style="font-size: 22px; font-weight: 400; line-height: 1.1;"
                    :style="{ color: Number(primary?.absoluteLimit) < 0 ? 'var(--debit)' : 'var(--ink)' }">
                    {{ eur(primary?.absoluteLimit) }}
                  </p>
                </div>
              </div>
            </div>
          </VCard>
        </div>
      </div>

      <!-- ── Accounts ────────────────────────────────── -->
      <div v-if="tab === 'accounts'" class="grid grid-cols-1 md:grid-cols-2 gap-5">
        <div
          v-for="(acc, i) in accounts"
          :key="acc.iban"
          class="rounded-2xl p-7"
          :class="i === 0 ? 'card-vault' : 'card-savings'"
        >
          <div class="flex items-start justify-between mb-8">
            <div>
              <p class="text-[11px] font-medium uppercase tracking-[.14em] opacity-70 mb-1">{{ acc.accountType }}</p>
              <VPill :tone="acc.active ? 'success' : 'danger'" class="opacity-90">
                {{ acc.active ? 'Active' : 'Closed' }}
              </VPill>
            </div>
          </div>

          <p
            class="font-display tabnum mb-6"
            style="font-size: 44px; font-weight: 300; line-height: 1;"
          >{{ eur(acc.balance) }}</p>

          <div class="pt-5 border-t" :style="{ borderColor: i === 0 ? 'rgba(255,255,255,.15)' : 'var(--line)' }">
            <p class="text-[11px] uppercase tracking-[.12em] opacity-60 mb-2">IBAN</p>
            <CopyChip :value="acc.iban" :light="i === 0" />
          </div>
        </div>
      </div>

      <!-- ── Transactions ────────────────────────────── -->
      <div v-if="tab === 'transactions'">
        <VCard>
          <div class="p-2">
            <div v-if="!transactions.length" class="py-16 text-center">
              <AlignLeft class="w-10 h-10 mx-auto mb-3" :style="{ color: 'var(--ink-3)' }" />
              <p class="text-base font-medium" :style="{ color: 'var(--ink)' }">No transactions yet</p>
              <p class="text-sm mt-1" :style="{ color: 'var(--ink-3)' }">Transactions for this customer will appear here.</p>
            </div>
            <ActivityRow
              v-for="tx in transactions"
              :key="tx.id"
              :tx="tx"
              :my-ibans="allIbans"
            />
          </div>
        </VCard>
      </div>

      <!-- ── Limits editor ──────────────────────────── -->
      <div v-if="tab === 'limits'" class="max-w-lg">
        <VCard>
          <div class="p-7">
            <h3 class="font-display text-2xl mb-1" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
              Transfer limits
            </h3>
            <p class="text-sm mb-7" :style="{ color: 'var(--ink-3)' }">
              Changes apply immediately to all of this customer's accounts.
            </p>

            <div class="space-y-5 mb-7">
              <VField label="Daily transfer limit" hint="Maximum the customer can send in a single day.">
                <VTextInput v-model="editLimits.daily" type="number" placeholder="2000" />
              </VField>
              <VField label="Absolute limit" hint="Balance can never drop below this. Use a negative number to allow overdraft.">
                <VTextInput v-model="editLimits.absolute" type="number" placeholder="0" />
              </VField>
            </div>

            <!-- Audit note -->
            <div class="flex items-start gap-3 rounded-xl p-4 mb-6" :style="{ background: 'var(--surface-2)' }">
              <Shield class="w-4 h-4 mt-0.5 flex-shrink-0" :style="{ color: 'var(--accent)' }" />
              <p class="text-xs leading-relaxed" :style="{ color: 'var(--ink-2)' }">
                Limit changes are applied to all <strong>{{ accounts.length }}</strong> account{{ accounts.length !== 1 ? 's' : '' }} belonging to this customer and are logged under your employee account.
              </p>
            </div>

            <div class="flex gap-3">
              <VBtn variant="secondary" size="md" @click="tab = 'overview'">Cancel</VBtn>
              <VBtn variant="primary" size="md" class="flex-1" :disabled="savingLimits" @click="saveLimits">
                {{ savingLimits ? 'Saving…' : 'Save changes' }}
              </VBtn>
            </div>
          </div>
        </VCard>
      </div>

    </template>

    <!-- ── Close confirm modal ────────────────────── -->
    <VModal :open="confirmClose" @close="confirmClose = false">
      <div class="p-7">
        <h3 class="font-display text-2xl mb-2" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
          Close all accounts?
        </h3>
        <p class="text-sm mb-6" :style="{ color: 'var(--ink-2)' }">
          This permanently closes all accounts for <strong>{{ customerName }}</strong>.
          Combined balance: <strong class="tabnum">{{ eur(totalBalance) }}</strong>.
          This action cannot be undone.
        </p>
        <VField label='Type "CLOSE" to confirm'>
          <VTextInput v-model="closeText" placeholder="CLOSE" />
        </VField>
        <div class="flex gap-3 mt-6">
          <VBtn variant="secondary" size="md" class="flex-1" @click="confirmClose = false">Cancel</VBtn>
          <VBtn
            variant="destructive" size="md" class="flex-1"
            :disabled="closeText !== 'CLOSE' || closing"
            :style="{ opacity: closeText !== 'CLOSE' ? 0.4 : 1 }"
            @click="handleClose"
          >{{ closing ? 'Closing…' : 'Close permanently' }}</VBtn>
        </div>
      </div>
    </VModal>

    <!-- Toast -->
    <Transition enter-active-class="toast" leave-active-class="opacity-0 transition-opacity duration-200">
      <div
        v-if="toast"
        class="toast fixed bottom-6 right-6 z-50 rounded-xl px-5 py-3 text-sm font-medium shadow-xl"
        :style="{ background: 'var(--ink)', color: 'var(--bg)' }"
      >{{ toast }}</div>
    </Transition>

  </EmployeeLayout>
</template>
