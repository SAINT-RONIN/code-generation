<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Home, CreditCard, ArrowLeftRight, History, Search, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Home, label: 'Home', path: '/customer' },
  { icon: CreditCard, label: 'Accounts', path: '/customer/accounts' },
  { icon: ArrowLeftRight, label: 'Transfer', path: '/customer/transfer' },
  { icon: History, label: 'Transactions', path: '/customer/transactions' },
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
  <div class="flex h-screen bg-[#0A0A0F] text-white">
    <aside class="hidden md:flex w-64 bg-[#14141A] border-r border-white/5 flex-col">
      <div class="p-6 flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#7B61FF] to-[#5C45CC] flex items-center justify-center">
          <span class="text-white font-bold text-sm">NL</span>
        </div>
        <span class="font-bold text-lg tracking-tight">Nova Bank</span>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm"
          :class="isActive(item.path) ? 'bg-[#7B61FF]/10 text-[#7B61FF]' : 'text-gray-400 hover:text-white hover:bg-white/5'"
        >
          <component :is="item.icon" class="w-5 h-5" />
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="p-4 border-t border-white/5">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-4 py-3 text-sm font-medium text-gray-400 hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/10 rounded-xl transition-all"
        >
          <LogOut class="w-5 h-5" />
          Sign out
        </button>
      </div>
    </aside>

    <main class="flex-1 overflow-y-auto pb-20 md:pb-0 relative">
      <div class="absolute top-0 left-0 w-full h-64 bg-gradient-to-b from-[#7B61FF]/5 to-transparent pointer-events-none" />
      <div class="p-4 md:p-8 max-w-5xl mx-auto relative z-10">
        <slot />
      </div>
    </main>

    <div class="md:hidden fixed bottom-0 left-0 w-full bg-[#14141A]/90 backdrop-blur-xl border-t border-white/5 flex justify-around p-2 z-50">
      <RouterLink
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 p-2 rounded-lg min-w-[56px]"
        :class="isActive(item.path) ? 'text-[#7B61FF]' : 'text-gray-500'"
      >
        <component :is="item.icon" class="w-5 h-5" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </div>
  </div>
</template>
