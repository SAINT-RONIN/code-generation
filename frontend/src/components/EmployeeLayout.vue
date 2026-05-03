<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Activity, Users, CheckCircle, ArrowLeftRight, FileText, Shield, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Activity, label: 'Overview', path: '/employee' },
  { icon: Users, label: 'Customers', path: '/employee/customers' },
  { icon: CheckCircle, label: 'Approvals', path: '/employee/approvals' },
  { icon: ArrowLeftRight, label: 'Transfers', path: '/employee/transfer' },
  { icon: FileText, label: 'Transactions', path: '/employee/transactions' },
]

function isActive(path) {
  if (path === '/employee') return route.path === '/employee'
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
      <div class="p-6 flex items-center gap-3 border-b border-white/5">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#FF5E5B] to-[#D93836] flex items-center justify-center">
          <Shield class="w-4 h-4 text-white" />
        </div>
        <div>
          <span class="font-bold text-sm tracking-tight block">Nova Bank Staff</span>
          <span class="text-[10px] text-gray-500 font-mono tracking-widest uppercase">Internal</span>
        </div>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-1">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm"
          :class="isActive(item.path) ? 'bg-[#FF5E5B]/10 text-[#FF5E5B]' : 'text-gray-400 hover:text-white hover:bg-white/5'"
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

    <main class="flex-1 overflow-y-auto">
      <div class="md:hidden flex items-center justify-between p-4 bg-[#14141A] border-b border-white/5 sticky top-0 z-20">
        <div class="flex items-center gap-2">
          <Shield class="w-5 h-5 text-[#FF5E5B]" />
          <span class="font-bold text-sm">Staff Portal</span>
        </div>
        <button @click="logout"><LogOut class="w-5 h-5 text-gray-400" /></button>
      </div>
      <div class="p-4 md:p-8 max-w-7xl mx-auto">
        <slot />
      </div>
    </main>

    <div class="md:hidden fixed bottom-0 left-0 w-full bg-[#14141A] border-t border-white/5 flex justify-around p-2 z-50">
      <RouterLink
        v-for="item in navItems.slice(0, 4)"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 p-2 rounded-lg min-w-[56px]"
        :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-gray-500'"
      >
        <component :is="item.icon" class="w-5 h-5" />
        <span class="text-[10px]">{{ item.label }}</span>
      </RouterLink>
    </div>
  </div>
</template>
