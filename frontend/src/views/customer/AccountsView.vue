<script setup>
import { ref, onMounted } from 'vue'
import { RouterLink } from 'vue-router'
import CustomerLayout from '../../components/CustomerLayout.vue'
import VPageHeader from '../../components/ui/VPageHeader.vue'
import VCard from '../../components/ui/VCard.vue'
import CopyChip from '../../components/ui/CopyChip.vue'
import { getMyAccounts } from '../../services/accounts'
import { eur } from '../../utils/format'

const accounts = ref([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    accounts.value = data
  } catch {
    error.value = 'Could not load accounts.'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <CustomerLayout>
    <VPageHeader eyebrow="Banking" title="Accounts" />

    <div v-if="error" class="mb-6 px-4 py-3 rounded-xl text-sm"
      :style="{ background: 'rgba(155,44,44,.08)', color: 'var(--debit)', border: '1px solid rgba(155,44,44,.2)' }">
      {{ error }}
    </div>

    <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <div v-for="i in 2" :key="i" class="skeleton h-44 rounded-2xl" />
    </div>

    <template v-else>
      <div v-if="!accounts.length" class="rounded-2xl border py-16 text-center"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }">
        <p class="text-sm" :style="{ color: 'var(--ink-3)' }">No accounts found.</p>
      </div>

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <RouterLink
          v-for="(account, i) in accounts"
          :key="account.iban"
          :to="`/customer/account/${account.iban}`"
          class="no-underline"
        >
          <div
            class="rounded-2xl p-6 h-52 flex flex-col justify-between lift cursor-pointer"
            :class="i === 0 ? 'card-vault' : 'card-savings'"
          >
            <div>
              <p class="text-xs font-medium uppercase tracking-[.1em] opacity-70">{{ account.accountType }}</p>
            </div>
            <div>
              <div class="text-3xl font-display tabnum mb-3" style="font-weight: 400;">
                {{ eur(account.balance) }}
              </div>
              <CopyChip :value="account.iban" :light="i === 0" />
            </div>
          </div>
        </RouterLink>
      </div>

      <!-- Limits summary -->
      <div v-if="accounts.length" class="mt-6 grid grid-cols-1 md:grid-cols-2 gap-4">
        <VCard v-for="account in accounts" :key="account.iban + '-limits'">
          <div class="p-5">
            <p class="text-sm font-semibold mb-4" :style="{ color: 'var(--ink)' }">{{ account.accountType }} limits</p>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <p class="text-xs" :style="{ color: 'var(--ink-3)' }">Daily limit</p>
                <p class="text-sm font-medium mt-1 tabnum" :style="{ color: 'var(--ink)' }">
                  {{ eur(account.dailyLimit) }}
                </p>
              </div>
              <div>
                <p class="text-xs" :style="{ color: 'var(--ink-3)' }">Absolute limit</p>
                <p class="text-sm font-medium mt-1 tabnum"
                  :style="{ color: Number(account.absoluteLimit) < 0 ? 'var(--debit)' : 'var(--ink)' }">
                  {{ eur(account.absoluteLimit) }}
                </p>
              </div>
            </div>
          </div>
        </VCard>
      </div>
    </template>
  </CustomerLayout>
</template>
