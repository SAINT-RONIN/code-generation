<script setup>
import { onMounted } from 'vue'
import VToast from './components/ui/VToast.vue'

onMounted(() => {
  document.documentElement.setAttribute('data-palette', 'vault')
})
</script>

<template>
  <RouterView />
  <VToast />
</template>
