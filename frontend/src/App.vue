<script setup>
import { ref, onMounted } from 'vue'
import VToast from './components/ui/VToast.vue'

const palette = ref('vault')
onMounted(() => {
  document.documentElement.setAttribute('data-palette', palette.value)
})
</script>

<template>
  <RouterView />
  <VToast />
</template>
