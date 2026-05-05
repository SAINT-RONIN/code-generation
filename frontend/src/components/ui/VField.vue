<script setup>
defineProps({
  label: String,
  hint: String,
  error: String,
  id: String,
})
</script>

<template>
  <label :for="id" class="block">
    <span v-if="label" class="block text-xs font-medium mb-1.5" :style="{ color: 'var(--ink-2)' }">{{ label }}</span>
    <slot />
    <span v-if="error" class="block text-xs mt-1.5 flex items-center gap-1" :style="{ color: 'var(--debit)' }">{{ error }}</span>
    <span v-else-if="hint" class="block text-xs mt-1.5" :style="{ color: 'var(--ink-3)' }">{{ hint }}</span>
  </label>
</template>
