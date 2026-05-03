<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Home, CreditCard, ArrowLeftRight, History, Search, LogOut, Cpu } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Home,           label: 'Home',      path: '/customer' },
  { icon: CreditCard,     label: 'Accounts',  path: '/customer/accounts' },
  { icon: ArrowLeftRight, label: 'Transfer',  path: '/customer/transfer' },
  { icon: History,        label: 'History',   path: '/customer/transactions' },
  { icon: Search,         label: 'Find IBAN', path: '/customer/find' },
]

function isActive(path) {
  return path === '/customer' ? route.path === '/customer' : route.path.startsWith(path)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen overflow-hidden" style="background:#08080D; color:#fff;">

    <!-- Skip to main content (keyboard) -->
    <a href="#main-content" class="skip-link">Skip to main content</a>

    <!-- ── Desktop Sidebar ── -->
    <aside
      class="hidden md:flex w-60 flex-col flex-shrink-0"
      style="background:#0E0E16; border-right:1px solid rgba(255,255,255,0.06);"
      aria-label="Main navigation"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 px-5 h-16" style="border-bottom:1px solid rgba(255,255,255,0.06);">
        <div
          class="w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0"
          style="background:linear-gradient(135deg,#7B61FF,#5C45CC);"
          aria-hidden="true"
        >
          <span class="text-white font-bold text-sm">N</span>
        </div>
        <span class="font-semibold text-[15px] text-white tracking-tight">Nova Bank</span>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-4 space-y-0.5" role="navigation" aria-label="Customer menu">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors"
          :class="isActive(item.path)
            ? 'text-white'
            : 'text-[#6B6B7E] hover:text-white hover:bg-white/[0.04]'"
          :style="isActive(item.path) ? 'background:rgba(123,97,255,0.12);' : ''"
          :aria-current="isActive(item.path) ? 'page' : undefined"
        >
          <component
            :is="item.icon"
            class="w-4 h-4 flex-shrink-0"
            :style="isActive(item.path) ? 'color:#7B61FF' : ''"
            aria-hidden="true"
          />
          {{ item.label }}
        </RouterLink>
      </nav>

      <!-- Footer -->
      <div class="px-3 pb-4 space-y-0.5" style="border-top:1px solid rgba(255,255,255,0.06); padding-top:12px;">
        <RouterLink
          to="/atm"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[#6B6B7E] hover:text-white hover:bg-white/[0.04] transition-colors"
          :aria-current="route.path.startsWith('/atm') ? 'page' : undefined"
        >
          <Cpu class="w-4 h-4 flex-shrink-0" aria-hidden="true" />
          ATM Terminal
        </RouterLink>
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[#6B6B7E] hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/[0.06] transition-colors"
          aria-label="Sign out of Nova Bank"
        >
          <LogOut class="w-4 h-4 flex-shrink-0" aria-hidden="true" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- ── Main ── -->
    <div class="flex-1 flex flex-col overflow-hidden">

      <!-- Mobile top bar -->
      <header
        class="md:hidden flex items-center justify-between px-4 h-14 flex-shrink-0"
        style="background:#0E0E16; border-bottom:1px solid rgba(255,255,255,0.06);"
      >
        <div class="flex items-center gap-2" aria-label="Nova Bank">
          <div class="w-7 h-7 rounded-lg flex items-center justify-center flex-shrink-0" style="background:linear-gradient(135deg,#7B61FF,#5C45CC);" aria-hidden="true">
            <span class="text-white font-bold text-xs">N</span>
          </div>
          <span class="font-semibold text-sm text-white">Nova Bank</span>
        </div>
        <button
          @click="logout"
          class="p-2 text-[#6B6B7E] hover:text-white transition-colors rounded-lg"
          aria-label="Sign out"
        >
          <LogOut class="w-4 h-4" aria-hidden="true" />
        </button>
      </header>

      <!-- Scrollable content -->
      <main id="main-content" class="flex-1 overflow-y-auto" tabindex="-1">
        <div class="px-5 py-8 md:px-8 max-w-4xl mx-auto">
          <slot />
        </div>
      </main>
    </div>

    <!-- ── Mobile bottom nav ── -->
    <nav
      class="md:hidden fixed bottom-0 left-0 right-0 flex z-50"
      style="background:rgba(14,14,22,0.97); backdrop-filter:blur(16px); border-top:1px solid rgba(255,255,255,0.06);"
      aria-label="Mobile navigation"
    >
      <RouterLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 flex-1 py-3 transition-colors"
        :class="isActive(item.path) ? 'text-[#7B61FF]' : 'text-[#4A4A5E]'"
        :aria-current="isActive(item.path) ? 'page' : undefined"
        :aria-label="item.label"
      >
        <component :is="item.icon" class="w-5 h-5" aria-hidden="true" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>
