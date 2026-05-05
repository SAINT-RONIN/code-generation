<script setup>
import { ref } from 'vue'
import { useRoute, useRouter, RouterLink } from 'vue-router'
import { Home, Users, CheckSquare, Send, AlignLeft, Bell, Settings, LogOut } from 'lucide-vue-next'
import IconLogo from './icons/IconLogo.vue'
import VPill from './ui/VPill.vue'

const route = useRoute()
const router = useRouter()
const showUserMenu = ref(false)

const navItems = [
  { label: 'Dashboard',         to: '/employee',              icon: Home },
  { label: 'Customers',         to: '/employee/customers',    icon: Users },
  { label: 'Approvals',         to: '/employee/approvals',    icon: CheckSquare },
  { label: 'Transactions',      to: '/employee/transactions', icon: AlignLeft },
  { label: 'Initiate transfer', to: '/employee/transfer',     icon: Send },
]

function isActive(to) {
  if (to === '/employee') return route.path === '/employee'
  return route.path.startsWith(to)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="min-h-screen flex flex-col" :style="{ background: 'var(--bg)' }">
    <!-- TopBar -->
    <header
      class="h-16 flex items-center px-8 gap-4 border-b flex-shrink-0 z-10"
      :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
    >
      <RouterLink to="/employee" class="flex items-center gap-2.5 flex-shrink-0 no-underline">
        <IconLogo class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
        <span class="font-display text-[19px]" :style="{ color: 'var(--ink)' }">Impreza Bank</span>
      </RouterLink>
      <VPill tone="accent">Employee</VPill>

      <div class="ml-auto flex items-center gap-2 relative">
        <button
          class="relative w-9 h-9 rounded-xl flex items-center justify-center"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)' }"
          aria-label="Notifications"
        >
          <Bell class="w-4 h-4" />
        </button>

        <!-- Avatar button -->
        <button
          class="flex items-center gap-2 h-9 px-3 rounded-xl lift"
          :style="{ background: 'var(--surface-2)' }"
          @click="showUserMenu = !showUserMenu"
        >
          <div
            class="w-6 h-6 rounded-full flex items-center justify-center text-[10px] font-semibold"
            :style="{ background: 'var(--accent)', color: 'var(--accent-ink)' }"
          >E</div>
          <span class="text-sm font-medium hidden sm:block" :style="{ color: 'var(--ink)' }">Employee</span>
        </button>

        <!-- Dropdown -->
        <div
          v-if="showUserMenu"
          class="absolute top-full right-0 mt-2 w-44 rounded-xl border shadow-lg overflow-hidden z-50"
          :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        >
          <div class="px-4 py-3 border-b" :style="{ borderColor: 'var(--line)' }">
            <p class="text-sm font-semibold" :style="{ color: 'var(--ink)' }">Employee</p>
            <p class="text-xs" :style="{ color: 'var(--ink-3)' }">Staff portal</p>
          </div>
          <button
            class="w-full flex items-center gap-2.5 px-4 py-3 text-sm font-medium text-left row"
            :style="{ color: 'var(--debit)' }"
            @click="logout"
          >
            <LogOut class="w-4 h-4" /> Log out
          </button>
        </div>

        <!-- Backdrop -->
        <div v-if="showUserMenu" class="fixed inset-0 z-40" @click="showUserMenu = false" />
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
