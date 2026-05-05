<script setup>
defineProps({
  open: { type: Boolean, required: true },
  maxW: { type: String, default: 'max-w-lg' },
})

const emit = defineEmits(['close'])
</script>

<template>
  <Teleport to="body">
    <div
      v-if="open"
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      style="background: rgba(15,15,15,.45); backdrop-filter: blur(6px);"
      @click="emit('close')"
    >
      <div
        class="w-full rounded-2xl border overflow-hidden"
        :class="maxW"
        :style="{ background: 'var(--surface)', borderColor: 'var(--line)' }"
        @click.stop
      >
        <slot />
      </div>
    </div>
  </Teleport>
</template>
