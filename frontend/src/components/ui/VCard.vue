<script setup>
defineProps({
  class: String,
})
</script>

<template>
  <div
    class="rounded-2xl border"
    :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
  >
    <slot />
  </div>
</template>
