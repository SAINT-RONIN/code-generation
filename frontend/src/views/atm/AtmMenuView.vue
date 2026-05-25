<script setup>
import { RouterLink } from 'vue-router'
import { ArrowUp, ArrowDown } from 'lucide-vue-next'
import AtmShell from '../../components/AtmShell.vue'
import { useAuth } from '../../composables/useAuth'

const { logout: authLogout } = useAuth()
function logout() { authLogout('/atm/login') }
</script>

<template>
  <AtmShell :showLogout="true" @logout="logout">
    <div class="flex flex-col items-center justify-center h-full p-10">
      <p class="text-sm mb-1" :style="{ color: 'var(--ink-3)' }">Welcome back,</p>
      <h1 class="font-display text-4xl mb-10" style="font-weight: 400;" :style="{ color: 'var(--ink)' }">
        What would you like to do?
      </h1>

      <div class="grid grid-cols-2 gap-5 w-full max-w-md">
        <RouterLink
          to="/atm/withdraw"
          class="no-underline rounded-2xl border p-8 flex flex-col items-center gap-4 lift cursor-pointer"
          :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        >
          <div
            class="w-14 h-14 rounded-xl flex items-center justify-center"
            :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
          >
            <ArrowUp class="w-7 h-7" />
          </div>
          <div class="text-center">
            <p class="text-base font-semibold" :style="{ color: 'var(--ink)' }">Withdraw</p>
            <p class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">Get cash</p>
          </div>
        </RouterLink>

        <RouterLink
          to="/atm/deposit"
          class="no-underline rounded-2xl border p-8 flex flex-col items-center gap-4 lift cursor-pointer"
          :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        >
          <div
            class="w-14 h-14 rounded-xl flex items-center justify-center"
            :style="{ background: 'var(--accent-soft)', color: 'var(--accent)' }"
          >
            <ArrowDown class="w-7 h-7" />
          </div>
          <div class="text-center">
            <p class="text-base font-semibold" :style="{ color: 'var(--ink)' }">Deposit</p>
            <p class="text-xs mt-1" :style="{ color: 'var(--ink-3)' }">Add cash</p>
          </div>
        </RouterLink>
      </div>
    </div>
  </AtmShell>
</template>
