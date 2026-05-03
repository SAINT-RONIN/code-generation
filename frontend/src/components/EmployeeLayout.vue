<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Activity, Users, CheckCircle, ArrowLeftRight, FileText, Shield, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Activity,       label: 'Overview',     path: '/employee' },
  { icon: Users,          label: 'Customers',    path: '/employee/customers' },
  { icon: CheckCircle,    label: 'Approvals',    path: '/employee/approvals' },
  { icon: ArrowLeftRight, label: 'Transfers',    path: '/employee/transfer' },
  { icon: FileText,       label: 'Transactions', path: '/employee/transactions' },
]

function isActive(path) {
  return path === '/employee' ? route.path === '/employee' : route.path.startsWith(path)
}

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
  router.push('/login')
}
</script>

<template>
  <div class="flex h-screen bg-[#0A0A0F] text-white">

    <a href="#main-content" class="skip-link">Skip to main content</a>

    <!-- Desktop Sidebar -->
    <aside
      class="hidden md:flex w-64 bg-[#14141A] border-r border-white/5 flex-col flex-shrink-0"
      role="complementary"
      aria-label="Staff navigation"
    >
      <div class="p-6 flex items-center gap-3 border-b border-white/5">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-[#FF5E5B] to-[#D93836] flex items-center justify-center flex-shrink-0" aria-hidden="true">
          <Shield class="w-4 h-4 text-white" />
        </div>
        <div>
          <span class="font-bold text-sm tracking-tight text-white block">Nova Bank Staff</span>
          <span class="text-[10px] text-white/50 font-mono tracking-widest uppercase">Internal</span>
        </div>
      </div>

      <nav class="flex-1 px-4 py-6 space-y-1" role="navigation" aria-label="Staff menu">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-4 py-3 rounded-xl transition-all font-medium text-sm"
          :class="isActive(item.path)
            ? 'bg-[#FF5E5B]/10 text-[#FF5E5B]'
            : 'text-gray-400 hover:text-white hover:bg-white/5'"
          :aria-current="isActive(item.path) ? 'page' : undefined"
        >
          <component :is="item.icon" class="w-5 h-5 flex-shrink-0" aria-hidden="true" />
          {{ item.label }}
        </RouterLink>
      </nav>

      <div class="p-4 border-t border-white/5">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium text-gray-400 hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/10 transition-all"
          aria-label="Sign out of staff portal"
        >
          <LogOut class="w-5 h-5 flex-shrink-0" aria-hidden="true" />
          Sign out
        </button>
      </div>
    </aside>

    <!-- Main Content -->
    <div class="flex-1 flex flex-col overflow-hidden">

      <!-- Mobile top bar -->
      <header class="md:hidden flex items-center justify-between px-4 h-14 bg-[#14141A] border-b border-white/5 flex-shrink-0 sticky top-0 z-20">
        <div class="flex items-center gap-2">
          <Shield class="w-4 h-4 text-[#FF5E5B]" aria-hidden="true" />
          <span class="font-bold text-sm text-white">Staff Portal</span>
        </div>
        <button @click="logout" class="p-2 text-gray-400 hover:text-white transition-colors rounded-lg" aria-label="Sign out">
          <LogOut class="w-4 h-4" aria-hidden="true" />
        </button>
      </header>

      <main id="main-content" class="flex-1 overflow-y-auto pb-20 md:pb-0" tabindex="-1">
        <div class="p-4 md:p-8 max-w-7xl mx-auto">
          <slot />
        </div>
      </main>
    </div>

    <!-- Mobile Bottom Nav -->
    <nav
      class="md:hidden fixed bottom-0 left-0 right-0 flex z-50 bg-[#14141A]/90 backdrop-blur-xl border-t border-white/5"
      aria-label="Mobile navigation"
    >
      <RouterLink
        v-for="item in navItems.slice(0, 4)"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 flex-1 py-3 transition-colors"
        :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-gray-500'"
        :aria-current="isActive(item.path) ? 'page' : undefined"
        :aria-label="item.label"
      >
        <component :is="item.icon" class="w-5 h-5" aria-hidden="true" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>
