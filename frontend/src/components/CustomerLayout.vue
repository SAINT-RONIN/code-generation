<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { Home, Wallet, Send, AlignLeft, Search, Settings, Bell, LogOut } from 'lucide-vue-next'
import IconLogo from './icons/IconLogo.vue'
import { getMyAccounts } from '../services/accounts'

const route = useRoute()
const router = useRouter()

const ownerName = ref('')
const showUserMenu = ref(false)

const initials = computed(() => {
  const parts = ownerName.value.trim().split(' ')
  if (parts.length >= 2) return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  return ownerName.value.slice(0, 2).toUpperCase() || '?'
})
const firstName = computed(() => ownerName.value.split(' ')[0] || 'Account')

onMounted(async () => {
  try {
    const { data } = await getMyAccounts()
    const u = data[0]?.user
    ownerName.value = u ? `${u.firstName} ${u.lastName}` : ''
  } catch { /* ignore */ }
})

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}

const navItems = [
  { label: 'Overview',  to: '/customer',              icon: Home },
  { label: 'Accounts',  to: '/customer/accounts',     icon: Wallet },
  { label: 'Transfer',  to: '/customer/transfer',     icon: Send },
  { label: 'History',   to: '/customer/transactions', icon: AlignLeft },
  { label: 'Find IBAN', to: '/customer/find',         icon: Search },
]

function isActive(to) {
  if (to === '/customer') return route.path === '/customer'
  return route.path.startsWith(to)
}
</script>

<template>
  <div class="min-h-screen flex flex-col" :style="{ background: 'var(--bg)' }">
    <!-- TopBar -->
    <header
      class="h-16 flex items-center px-8 gap-4 border-b flex-shrink-0 z-10"
      :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
    >
      <!-- Logo -->
      <RouterLink to="/customer" class="flex items-center gap-2.5 flex-shrink-0 no-underline">
        <IconLogo class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
        <span class="font-display text-[19px]" :style="{ color: 'var(--ink)' }">Impreza Bank</span>
      </RouterLink>

      <!-- Search -->
      <div class="flex-1 hidden md:flex justify-center">
        <div class="relative w-64">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4" :style="{ color: 'var(--ink-3)' }" />
          <input
            type="text"
            placeholder="Search…"
            class="w-full h-9 pl-9 pr-3 text-sm rounded-xl"
            :style="{ background: 'var(--surface-2)', color: 'var(--ink)', border: 'none', outline: 'none' }"
          />
        </div>
      </div>

      <!-- Right: Bell + User menu -->
      <div class="ml-auto flex items-center gap-2 relative">
        <button
          class="relative w-9 h-9 rounded-xl flex items-center justify-center"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)' }"
          aria-label="Notifications"
        >
          <Bell class="w-4 h-4" />
          <span class="absolute top-2 right-2 w-1.5 h-1.5 rounded-full" :style="{ background: 'var(--accent)' }" />
        </button>

        <!-- Avatar button — click to open menu -->
        <button
          class="flex items-center gap-2 h-9 px-3 rounded-xl lift"
          :style="{ background: 'var(--surface-2)' }"
          @click="showUserMenu = !showUserMenu"
        >
          <div
            class="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-semibold"
            :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
          >{{ initials }}</div>
          <span class="text-sm font-medium hidden sm:block" :style="{ color: 'var(--ink)' }">{{ firstName }}</span>
        </button>

        <!-- Dropdown menu -->
        <div
          v-if="showUserMenu"
          class="absolute top-full right-0 mt-2 w-44 rounded-xl border shadow-lg overflow-hidden z-50"
          :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        >
          <div class="px-4 py-3 border-b" :style="{ borderColor: 'var(--line)' }">
            <p class="text-sm font-semibold truncate" :style="{ color: 'var(--ink)' }">{{ ownerName || firstName }}</p>
            <p class="text-xs" :style="{ color: 'var(--ink-3)' }">Customer</p>
          </div>
          <button
            class="w-full flex items-center gap-2.5 px-4 py-3 text-sm font-medium text-left row"
            :style="{ color: 'var(--debit)' }"
            @click="logout"
          >
            <LogOut class="w-4 h-4" /> Log out
          </button>
        </div>

        <!-- Click-outside backdrop -->
        <div
          v-if="showUserMenu"
          class="fixed inset-0 z-40"
          @click="showUserMenu = false"
        />
      </div>
    </header>

    <div class="flex flex-1 overflow-hidden">
      <!-- Sidebar -->
      <nav
        class="w-[232px] flex-shrink-0 hidden lg:flex flex-col px-6 py-8 border-r"
        :style="{ background: 'var(--bg)', borderColor: 'var(--line)' }"
      >
        <div class="flex flex-col gap-1 flex-1">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="nav-item flex items-center gap-3 h-10 px-3 rounded-lg text-sm font-medium no-underline transition-colors"
            :aria-current="isActive(item.to) ? 'page' : undefined"
            :style="isActive(item.to)
              ? { background: 'var(--surface-2)', color: 'var(--ink)' }
              : { color: 'var(--ink-2)' }"
          >
            <component :is="item.icon" class="w-4 h-4 flex-shrink-0" />
            {{ item.label }}
          </RouterLink>

          <button
            disabled
            class="nav-item flex items-center gap-3 h-10 px-3 rounded-lg text-sm font-medium opacity-40 cursor-not-allowed text-left"
            :style="{ color: 'var(--ink-2)' }"
          >
            <Settings class="w-4 h-4 flex-shrink-0" />
            Settings
          </button>
        </div>

        <div class="mt-6 border-t pt-4" :style="{ borderColor: 'var(--line)' }">
          <button
            class="nav-item flex items-center gap-3 h-10 px-3 rounded-lg text-sm font-medium w-full text-left lift"
            :style="{ color: 'var(--debit)' }"
            @click="logout"
          >
            <LogOut class="w-4 h-4 flex-shrink-0" />
            Log out
          </button>
        </div>
      </nav>

      <!-- Main Content -->
      <main class="flex-1 overflow-y-auto">
        <div class="px-6 lg:px-12 py-10 max-w-[1280px] mx-auto w-full">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>
