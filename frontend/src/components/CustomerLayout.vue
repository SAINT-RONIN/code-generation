<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Home, CreditCard, ArrowLeftRight, History, Search, LogOut, Bell } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Home, label: 'Home', path: '/customer' },
  { icon: CreditCard, label: 'Accounts', path: '/customer/accounts' },
  { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer' },
  { icon: History, label: 'History', path: '/customer/transactions' },
  { icon: Search, label: 'Find IBAN', path: '/customer/find' },
]

function isActive(path) {
  if (path === '/customer') return route.path === '/customer'
  return route.path.startsWith(path)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen bg-[#0A0A0F] text-white overflow-hidden">
    <!-- Desktop Sidebar -->
    <aside class="hidden md:flex w-60 bg-[#0D0D14] border-r border-white/[0.05] flex-col flex-shrink-0">
      <!-- Logo -->
      <div class="p-5 flex items-center gap-3 border-b border-white/[0.05]">
        <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center shadow-lg shadow-[#7B61FF]/20 flex-shrink-0">
          <span class="text-white font-bold text-sm">N</span>
        </div>
        <span class="font-bold text-base tracking-tight">Nova Bank</span>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-5 space-y-0.5">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all font-medium text-sm group"
          :class="isActive(item.path)
            ? 'bg-[#7B61FF]/10 text-[#7B61FF]'
            : 'text-gray-500 hover:text-gray-200 hover:bg-white/[0.04]'"
        >
          <component :is="item.icon" class="w-4.5 h-4.5 flex-shrink-0" :class="isActive(item.path) ? 'text-[#7B61FF]' : 'text-gray-600 group-hover:text-gray-300'" />
          {{ item.label }}
          <div v-if="isActive(item.path)" class="ml-auto w-1.5 h-1.5 rounded-full bg-[#7B61FF]"></div>
        </RouterLink>
      </nav>

      <!-- Footer -->
      <div class="p-3 border-t border-white/[0.05]">
        <RouterLink to="/atm" class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-gray-500 hover:text-gray-200 hover:bg-white/[0.04] transition-all mb-0.5">
          <div class="w-4.5 h-4.5 text-gray-600">💳</div>
          ATM Terminal
        </RouterLink>
        <button @click="logout" class="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-gray-500 hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/8 rounded-xl transition-all">
          <LogOut class="w-4.5 h-4.5" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Top bar (mobile) -->
      <div class="md:hidden flex items-center justify-between px-4 py-3 border-b border-white/[0.05] bg-[#0D0D14]">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center">
            <span class="text-white font-bold text-xs">N</span>
          </div>
          <span class="font-bold text-sm">Nova Bank</span>
        </div>
        <button @click="logout" class="text-gray-500 hover:text-white transition-colors p-1">
          <LogOut class="w-4.5 h-4.5" />
        </button>
      </div>

      <!-- Scrollable content -->
      <main class="flex-1 overflow-y-auto relative">
        <div class="absolute top-0 left-0 right-0 h-64 bg-gradient-to-b from-[#7B61FF]/[0.04] to-transparent pointer-events-none"></div>
        <div class="p-5 md:p-8 max-w-5xl mx-auto relative">
          <slot />
        </div>
      </main>
    </div>

    <!-- Mobile bottom nav -->
    <div class="md:hidden fixed bottom-0 left-0 right-0 bg-[#0D0D14]/95 backdrop-blur-xl border-t border-white/[0.05] flex z-50 safe-area-inset-bottom">
      <RouterLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 flex-1 py-2.5 px-1 transition-colors"
        :class="isActive(item.path) ? 'text-[#7B61FF]' : 'text-gray-600'"
      >
        <component :is="item.icon" class="w-5 h-5" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </div>
  </div>
</template>
