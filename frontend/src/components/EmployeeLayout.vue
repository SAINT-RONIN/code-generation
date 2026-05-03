<script setup>
import { useRoute, useRouter } from 'vue-router'
import { Activity, Users, CheckCircle, ArrowLeftRight, FileText, Shield, LogOut } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const navItems = [
  { icon: Activity,       label: 'Overview',   path: '/employee' },
  { icon: Users,          label: 'Customers',  path: '/employee/customers' },
  { icon: CheckCircle,    label: 'Approvals',  path: '/employee/approvals' },
  { icon: ArrowLeftRight, label: 'Transfers',  path: '/employee/transfer' },
  { icon: FileText,       label: 'Ledger',     path: '/employee/transactions' },
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
  <div class="flex h-screen overflow-hidden" style="background:#08080D; color:#fff;">

    <a href="#main-content" class="skip-link">Skip to main content</a>

    <!-- ── Desktop Sidebar ── -->
    <aside
      class="hidden md:flex w-60 flex-col flex-shrink-0"
      style="background:#0E0E16; border-right:1px solid rgba(255,255,255,0.06);"
      aria-label="Staff navigation"
    >
      <!-- Logo -->
      <div class="flex items-center gap-3 px-5 h-16" style="border-bottom:1px solid rgba(255,255,255,0.06);">
        <div
          class="w-8 h-8 rounded-xl flex items-center justify-center flex-shrink-0"
          style="background:linear-gradient(135deg,#FF5E5B,#D93836);"
          aria-hidden="true"
        >
          <Shield class="w-4 h-4 text-white" aria-hidden="true" />
        </div>
        <div>
          <p class="font-semibold text-[15px] text-white tracking-tight leading-tight">Nova Bank</p>
          <p class="text-[10px] font-medium tracking-widest uppercase" style="color:#FF5E5B; opacity:0.65;">Staff</p>
        </div>
      </div>

      <!-- Nav -->
      <nav class="flex-1 px-3 py-4 space-y-0.5" role="navigation" aria-label="Staff menu">
        <RouterLink
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors"
          :class="isActive(item.path)
            ? 'text-white'
            : 'text-[#6B6B7E] hover:text-white hover:bg-white/[0.04]'"
          :style="isActive(item.path) ? 'background:rgba(255,94,91,0.1);' : ''"
          :aria-current="isActive(item.path) ? 'page' : undefined"
        >
          <component
            :is="item.icon"
            class="w-4 h-4 flex-shrink-0"
            :style="isActive(item.path) ? 'color:#FF5E5B' : ''"
            aria-hidden="true"
          />
          {{ item.label }}
        </RouterLink>
      </nav>

      <!-- Footer -->
      <div class="px-3 pb-4" style="border-top:1px solid rgba(255,255,255,0.06); padding-top:12px;">
        <button
          @click="logout"
          class="w-full flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-[#6B6B7E] hover:text-[#FF5E5B] hover:bg-[#FF5E5B]/[0.06] transition-colors"
          aria-label="Sign out of staff portal"
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
        <div class="flex items-center gap-2">
          <Shield class="w-4 h-4" style="color:#FF5E5B;" aria-hidden="true" />
          <span class="font-semibold text-sm text-white">Staff Portal</span>
        </div>
        <button @click="logout" class="p-2 text-[#6B6B7E] hover:text-white transition-colors rounded-lg" aria-label="Sign out">
          <LogOut class="w-4 h-4" aria-hidden="true" />
        </button>
      </header>

      <main id="main-content" class="flex-1 overflow-y-auto" tabindex="-1">
        <div class="px-5 py-8 md:px-8 max-w-6xl mx-auto">
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
        v-for="item in navItems.slice(0,4)"
        :key="item.path"
        :to="item.path"
        class="flex flex-col items-center gap-1 flex-1 py-3 transition-colors"
        :class="isActive(item.path) ? 'text-[#FF5E5B]' : 'text-[#4A4A5E]'"
        :aria-current="isActive(item.path) ? 'page' : undefined"
        :aria-label="item.label"
      >
        <component :is="item.icon" class="w-5 h-5" aria-hidden="true" />
        <span class="text-[10px] font-medium">{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>
