<script setup>
import IconLogo from './icons/IconLogo.vue'
import { LogOut } from 'lucide-vue-next'

const props = defineProps({
  showLogout: { type: Boolean, default: false },
})

const emit = defineEmits(['logout'])
</script>

<template>
  <div class="min-h-screen flex items-center justify-center p-6" style="background: #0B0F0D;">
    <div
      class="w-full max-w-[960px] rounded-3xl overflow-hidden flex flex-col"
      style="aspect-ratio: 4/3; background: var(--bg);"
    >
      <!-- ATM Header -->
      <header
        class="flex items-center justify-between px-8 py-4 border-b"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <div class="flex items-center gap-2.5">
          <IconLogo class="w-6 h-6" :style="{ color: 'var(--accent)' }" />
          <span class="font-display text-[17px]" :style="{ color: 'var(--ink)' }">Impreza Bank ATM</span>
        </div>
        <button
          v-if="showLogout"
          class="flex items-center gap-2 h-8 px-3 rounded-lg text-sm font-medium lift"
          :style="{ background: 'var(--surface-2)', color: 'var(--ink-2)' }"
          @click="emit('logout')"
        >
          <LogOut class="w-4 h-4" />
          Sign out
        </button>
      </header>

      <!-- ATM Content -->
      <div class="flex-1 overflow-y-auto">
        <slot />
      </div>

      <!-- ATM Footer -->
      <footer
        class="flex items-center justify-between px-8 py-3 border-t"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
      >
        <span class="text-xs" :style="{ color: 'var(--ink-3)' }">Impreza Bank ATM · Damrak 70 · Amsterdam</span>
        <span class="text-xs" :style="{ color: 'var(--ink-3)' }">Session secured · TLS 1.3</span>
      </footer>
    </div>
  </div>
</template>
