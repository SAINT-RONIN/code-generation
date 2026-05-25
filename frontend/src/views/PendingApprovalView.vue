<script setup>
import { Clock } from 'lucide-vue-next'
import IconLogo from '../components/icons/IconLogo.vue'
import VBtn from '../components/ui/VBtn.vue'
import VPill from '../components/ui/VPill.vue'
import { useAuth } from '../composables/useAuth'

const { logout } = useAuth()
</script>

<template>
  <div class="min-h-screen flex flex-col items-center justify-center p-8" :style="{ background: 'var(--bg)' }">
    <div class="w-full max-w-sm text-center">
      <!-- Logo -->
      <div class="flex items-center justify-center gap-2.5 mb-10">
        <IconLogo class="w-7 h-7" :style="{ color: 'var(--accent)' }" />
        <span class="font-display text-[19px]" :style="{ color: 'var(--ink)' }">Impreza Bank</span>
      </div>

      <!-- Clock icon -->
      <div
        class="w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-8"
        :style="{ background: 'var(--accent-soft)' }"
      >
        <Clock class="w-9 h-9" :style="{ color: 'var(--accent)' }" />
      </div>

      <h1 class="font-display mb-3" style="font-size: 44px; line-height: 1.05; font-weight: 400;" :style="{ color: 'var(--ink)' }">
        Thanks for joining.
      </h1>

      <p class="text-sm leading-relaxed mb-6" :style="{ color: 'var(--ink-2)' }">
        Our team is reviewing your application. We'll activate your account within 1–2 business days and notify you by email.
      </p>

      <div class="flex justify-center mb-8">
        <VPill tone="warn">Under review</VPill>
      </div>

      <VBtn variant="secondary" size="md" class="w-full" @click="logout">
        Sign out
      </VBtn>
    </div>
  </div>
</template>
