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
  { icon: FileText, label: 'Ledger', path: '/employee/transactions' },
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
  <div class="flex h-screen bg-[#0A0A0F] text-white overflow-hidden">
    <!-- Desktop Sidebar -->
    <aside class="hidden md:flex w-60 bg-[#0D0D14] border-r border-white/[0.05] flex-col flex-shrink-0">
      <!-- Logo -->
      <div class="p-5 flex items-center gap-3 border-b border-white/[0.05]">
        <div class="w-8 h-8 rounded-xl bg-gradient-to-br from-[#FF5E5B] to-[#D93836] flex items-center justify-center shadow-lg shadow-[#FF5E5B]/20 flex-shrink-0">
          <Shield class="w-4 h-4 text-white" />
        </div>
        <div>
          <p class="font-bold text-sm leading-tight">Nova Bank</p>
          <p class="text-[10px] text-gray-600 font-mono tracking-widest uppercase">Staff Portal</p>
        </div>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-5 space-y-0.5">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all font-medium text-sm group"
          :class="isActive(item.path)
            ? 'bg-[#FF5E5B]/10 text-[#FF5E5B]'
            : 'text-gray-500 hover:text-gray-200 hover:bg-white/[0.04]'"
        >
          <component :is="item.icon" class="w-4.5 h-4.5 flex-shrink-0" :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-gray-600 group-hover:text-gray-300'" />
          {{ item.label }}
          <div v-if="isActive(item.path)" class="ml-auto w-1.5 h-1.5 rounded-full bg-[#FF5E5B]"></div>
        </RouterLink>
      </nav>

      <!-- Footer -->
      <div class="p-3 border-t border-white/[0.05]">
        <button @click="logout" class="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-gray-500 hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/8 rounded-xl transition-all">
          <LogOut class="w-4.5 h-4.5" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Mobile top bar -->
      <div class="md:hidden flex items-center justify-between px-4 py-3 border-b border-white/[0.05] bg-[#0D0D14]">
        <div class="flex items-center gap-2">
          <Shield class="w-4 h-4 text-[#FF5E5B]" />
          <span class="font-bold text-sm">Staff Portal</span>
        </div>
        <button @click="logout" class="text-gray-500 hover:text-white p-1 transition-colors">
          <LogOut class="w-4.5 h-4.5" />
        </button>
      </div>

      <main class="flex-1 overflow-y-auto">
        <div class="p-5 md:p-8 max-w-7xl mx-auto">
          <slot />
        </div>
      </main>
    </div>

    <!-- Mobile bottom nav -->
    <div class="md:hidden fixed bottom-0 left-0 right-0 bg-[#0D0D14]/95 backdrop-blur-xl border-t border-white/[0.05] flex z-50">
      <RouterLink
        v-for="item in navItems.slice(0, 4)"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 flex-1 py-2.5 px-1 transition-colors"
        :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-gray-600'"
      >
        <component :is="item.icon" class="w-5 h-5" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </div>
  </div>
</template>
