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
    <aside class="hidden md:flex w-64 flex-col flex-shrink-0 relative" style="background: linear-gradient(180deg, #0E0E17 0%, #0A0A0F 100%); border-right: 1px solid rgba(255,255,255,0.05);">
      <div class="absolute top-0 left-0 right-0 h-48 pointer-events-none" style="background: radial-gradient(ellipse at 50% 0%, rgba(255,94,91,0.08) 0%, transparent 70%)"></div>

      <!-- Logo -->
      <div class="p-5 flex items-center gap-3 relative" style="border-bottom: 1px solid rgba(255,255,255,0.05);">
        <div class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0" style="background: linear-gradient(135deg, #FF5E5B, #D93836); box-shadow: 0 0 16px rgba(255,94,91,0.3);">
          <Shield class="w-4 h-4 text-white" />
        </div>
        <div>
          <p class="font-bold text-sm leading-tight text-white">Nova Bank</p>
          <p class="text-[10px] tracking-widest uppercase" style="color: #FF5E5B; opacity: 0.7;">Staff Portal</p>
        </div>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-5 space-y-0.5 relative">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl transition-all duration-200 font-medium text-sm group"
          :class="isActive(item.path)
            ? 'nav-active-red text-[#FF5E5B] pl-4'
            : 'text-gray-500 hover:text-gray-200 hover:bg-white/[0.04]'"
        >
          <component
            :is="item.icon"
            class="w-4 h-4 flex-shrink-0 transition-all"
            :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-gray-600 group-hover:text-gray-300'"
          />
          {{ item.label }}
        </RouterLink>
      </nav>

      <!-- Footer -->
      <div class="p-3 relative" style="border-top: 1px solid rgba(255,255,255,0.05);">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-3 py-2.5 text-sm font-medium text-gray-500 hover:text-[#FF5E5B] rounded-xl transition-all group"
          style="transition: background 0.2s;"
          @mouseenter="$event.currentTarget.style.background='rgba(255,94,91,0.07)'"
          @mouseleave="$event.currentTarget.style.background=''"
        >
          <LogOut class="w-4 h-4" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main content -->
    <div class="flex-1 flex flex-col overflow-hidden">
      <!-- Mobile top bar -->
      <div class="md:hidden flex items-center justify-between px-4 py-3 bg-[#0E0E17]" style="border-bottom: 1px solid rgba(255,255,255,0.05);">
        <div class="flex items-center gap-2">
          <Shield class="w-4 h-4 text-[#FF5E5B]" />
          <span class="font-bold text-sm">Staff Portal</span>
        </div>
        <button @click="logout" class="text-gray-500 hover:text-white p-1 transition-colors">
          <LogOut class="w-4 h-4" />
        </button>
      </div>

      <main class="flex-1 overflow-y-auto relative dot-grid">
        <div class="absolute top-0 left-0 right-0 h-72 pointer-events-none" style="background: linear-gradient(180deg, rgba(255,94,91,0.04) 0%, transparent 100%)"></div>
        <div class="p-5 md:p-8 max-w-7xl mx-auto relative">
          <slot />
        </div>
      </main>
    </div>

    <!-- Mobile bottom nav -->
    <div class="md:hidden fixed bottom-0 left-0 right-0 flex z-50" style="background: rgba(14,14,23,0.96); backdrop-filter: blur(20px); border-top: 1px solid rgba(255,255,255,0.05);">
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
