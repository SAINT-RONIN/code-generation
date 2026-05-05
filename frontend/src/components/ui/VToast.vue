<script setup>
import { toastMsg } from '../../composables/useToast'
import { Check } from 'lucide-vue-next'
</script>

<template>
  <Teleport to="body">
    <div
      v-if="toastMsg"
      class="toast fixed bottom-6 right-6 z-50 rounded-xl px-4 py-3 flex items-center gap-2.5 text-[13px] shadow-lg"
      :style="{ background: 'var(--ink)', color: 'var(--bg)' }"
    >
      <span
        class="w-5 h-5 rounded-full flex items-center justify-center flex-shrink-0"
        :style="{ background: 'var(--credit)' }"
      >
        <Check class="w-3 h-3" />
      </span>
      {{ toastMsg }}
    </div>
  </Teleport>
</template>
