<script setup>
defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  disabled: { type: Boolean, default: false },
})

const sizeClasses = {
  sm: 'h-8 px-3 text-xs rounded-md',
  md: 'h-10 px-4 text-sm rounded-lg',
  lg: 'h-12 px-5 text-sm rounded-lg',
}

const variantStyles = {
  primary:     { background: 'var(--accent)',      color: 'var(--accent-ink)' },
  secondary:   { background: 'transparent',        color: 'var(--ink)',       border: '1px solid var(--line-2)' },
  ghost:       { background: 'transparent',        color: 'var(--ink-2)' },
  destructive: { background: 'var(--debit)',        color: '#fff' },
  softAccent:  { background: 'var(--accent-soft)', color: 'var(--accent)' },
}
</script>

<template>
  <button
    :disabled="disabled"
    class="inline-flex items-center justify-center gap-1.5 font-medium lift cursor-pointer"
    :class="[sizeClasses[size], disabled ? 'opacity-50 cursor-not-allowed' : '']"
    :style="variantStyles[variant]"
  >
    <slot />
  </button>
</template>
