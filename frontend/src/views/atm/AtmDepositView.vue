<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, RouterLink } from 'vue-router'
import { atmDeposit } from '../../services/transactions'
import { getMyAccounts } from '../../services/accounts'
import { Check, ArrowLeft } from 'lucide-vue-next'
import AtmShell from '../../components/AtmShell.vue'

const router = useRouter()
const accounts = ref([])
const selectedIban = ref('')
const customAmount = ref('')
const error = ref('')
const loading = ref(false)
const done = ref(false)
const depositedAmount = ref(0)
const PRESETS = [20, 50, 100, 200]

function eur(val) { return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0) }

onMounted(async () => {
  const { data } = await getMyAccounts()
  accounts.value = data
  selectedIban.value = data.find(a => a.accountType === 'CHECKING')?.iban || data[0]?.iban || ''
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/atm/login')
}

async function doDeposit(amount) {
  if (!amount || amount <= 0) { error.value = 'Select or enter a valid amount'; return }
  if (!selectedIban.value) { error.value = 'No account selected'; return }
  error.value = ''
  loading.value = true
  try {
    await atmDeposit({ iban: selectedIban.value, amount })
    depositedAmount.value = amount
    done.value = true
    setTimeout(() => router.push('/atm/menu'), 5000)
  } catch (e) {
    error.value = e?.response?.data?.message || 'Deposit failed.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AtmShell :showLogout="true" @logout="logout">
    <div class="flex flex-col items-center justify-center h-full p-10">
      <div v-if="done" class="text-center">
        <div class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6" :style="{ background: 'var(--accent-soft)' }">
          <Check class="w-10 h-10" :style="{ color: 'var(--accent)' }" />
        </div>
        <h1 class="font-display text-3xl mb-2" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">Deposit complete.</h1>
        <p class="text-4xl font-display tabnum mb-2" style="font-weight: 300;" :style="{ color: 'var(--accent)' }">{{ eur(depositedAmount) }}</p>
        <p class="text-sm" :style="{ color: 'var(--ink-3)' }">Returning to menu…</p>
      </div>
      <div v-else class="w-full max-w-sm">
        <RouterLink
          to="/atm/menu"
          class="inline-flex items-center gap-2 text-sm font-medium mb-6 no-underline px-3 py-2 rounded-lg"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)', border: '1px solid var(--line-2)' }"
        >
          <ArrowLeft class="w-4 h-4" /> Back to menu
        </RouterLink>
        <h1 class="font-display text-3xl mb-6" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">Deposit</h1>
        <div v-if="error" class="mb-4 px-4 py-2.5 rounded-xl text-sm" :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)' }">{{ error }}</div>
        <div class="mb-5">
          <label class="block text-xs font-medium mb-2" :style="{ color: 'var(--ink-3)' }">Account</label>
          <select v-model="selectedIban" class="w-full h-10 px-3 text-sm rounded-xl border" :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }">
            <option v-for="a in accounts" :key="a.iban" :value="a.iban">{{ a.accountType }} — {{ eur(a.balance) }}</option>
          </select>
        </div>
        <div class="grid grid-cols-4 gap-2 mb-4">
          <button v-for="amt in PRESETS" :key="amt" class="h-14 rounded-xl text-sm font-medium lift border" :style="{ background: 'var(--surface)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" @click="doDeposit(amt)">€{{ amt }}</button>
        </div>
        <div class="flex gap-2">
          <div class="flex-1 relative">
            <span class="absolute left-3 top-1/2 -translate-y-1/2 text-sm" :style="{ color: 'var(--ink-3)' }">€</span>
            <input v-model="customAmount" type="number" placeholder="Other amount" class="w-full h-11 pl-7 pr-3 text-sm rounded-xl border" :style="{ background: 'var(--surface-2)', borderColor: 'var(--line-2)', color: 'var(--ink)' }" />
          </div>
          <button class="h-11 px-4 rounded-xl text-sm font-medium lift" :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }" :disabled="loading" @click="doDeposit(parseFloat(customAmount))">Deposit</button>
        </div>
      </div>
    </div>
  </AtmShell>
</template>
