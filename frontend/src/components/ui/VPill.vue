<script setup>
defineProps({
  tone: { type: String, default: 'neutral' },
})

const tones = {
  neutral: { background: 'var(--surface-2)',        color: 'var(--ink-2)' },
  accent:  { background: 'var(--accent-soft)',       color: 'var(--accent)' },
  success: { background: 'rgba(31,107,67,.12)',      color: 'var(--credit)' },
  danger:  { background: 'rgba(155,44,44,.12)',      color: 'var(--debit)' },
  warn:    { background: 'rgba(180,83,9,.12)',       color: '#92400E' },
}
</script>

<template>
  <span
    class="inline-flex items-center gap-1 px-2 h-6 rounded-full text-[11px] font-medium"
    :style="tones[tone]"
  >
    <slot />
  </span>
</template>
