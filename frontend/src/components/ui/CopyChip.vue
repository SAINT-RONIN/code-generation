<script setup>
import { ref } from 'vue'
import { Check, Copy } from 'lucide-vue-next'

const props = defineProps({
  value: { type: String, required: true },
  light: { type: Boolean, default: false },
})

const copied = ref(false)

function copy(e) {
  e.stopPropagation()
  navigator.clipboard?.writeText(props.value.replace(/\s/g, ''))
  copied.value = true
  setTimeout(() => { copied.value = false }, 1500)
}
</script>

<template>
  <button
    class="inline-flex items-center gap-2"
    :aria-label="`Copy ${value}`"
    @click="copy"
  >
    <span
      class="font-mono text-[13px] tracking-[.04em]"
      :style="{ color: light ? 'var(--accent-ink)' : 'var(--ink)', fontFamily: '\'JetBrains Mono\', monospace' }"
    >{{ value }}</span>
    <span
      class="w-7 h-7 rounded-md flex items-center justify-center"
      :style="{
        background: light ? 'rgba(255,255,255,.10)' : 'var(--surface-2)',
        color: light ? 'var(--accent-ink)' : 'var(--ink-2)',
      }"
    >
      <Check v-if="copied" class="w-3.5 h-3.5" />
      <Copy v-else class="w-3.5 h-3.5" />
    </span>
  </button>
</template>
