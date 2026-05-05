<script setup>
defineProps({
  eyebrow: String,
  title: String,
  sub: String,
})
</script>

<template>
  <div class="flex items-end justify-between mb-8">
    <div>
      <div v-if="eyebrow" class="text-[11px] uppercase tracking-[.16em]" :style="{ color: 'var(--ink-3)' }">{{ eyebrow }}</div>
      <h1
        class="font-display mt-1"
        :style="{ color: 'var(--ink)', fontSize: '36px', lineHeight: 1.05, fontWeight: 400 }"
      >{{ title }}</h1>
      <p v-if="sub" class="text-sm mt-2" :style="{ color: 'var(--ink-2)' }">{{ sub }}</p>
    </div>
    <slot name="right" />
  </div>
</template>
