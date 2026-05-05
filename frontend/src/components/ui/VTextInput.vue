<script setup>
defineProps({
  prefix: String,
  suffix: String,
  error: Boolean,
  modelValue: [String, Number],
  type: { type: String, default: 'text' },
  placeholder: String,
  disabled: Boolean,
})

defineEmits(['update:modelValue'])
</script>

<template>
  <div class="relative">
    <span
      v-if="prefix"
      class="absolute left-3 top-1/2 -translate-y-1/2 text-sm pointer-events-none"
      :style="{ color: 'var(--ink-3)' }"
    >{{ prefix }}</span>
    <input
      :type="type"
      :value="modelValue"
      :placeholder="placeholder"
      :disabled="disabled"
      class="w-full h-10 rounded-lg text-sm"
      :class="[prefix ? 'pl-8' : 'pl-3', suffix ? 'pr-9' : 'pr-3']"
      :style="{
        background: 'var(--surface)',
        color: 'var(--ink)',
        border: `1px solid ${error ? 'var(--debit)' : 'var(--line-2)'}`,
      }"
      @input="$emit('update:modelValue', $event.target.value)"
    />
    <span
      v-if="suffix"
      class="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none"
      :style="{ color: 'var(--ink-3)' }"
    >{{ suffix }}</span>
  </div>
</template>
