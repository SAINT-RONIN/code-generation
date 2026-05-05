import { ref, watch, onUnmounted } from 'vue'

export function useCountUp(source, duration = 1200) {
  const display = ref(0)
  let raf = null

  function animateTo(to) {
    const from = display.value
    let start = null
    cancelAnimationFrame(raf)
    const tick = (t) => {
      if (!start) start = t
      const p = Math.min(1, (t - start) / duration)
      const eased = 1 - Math.pow(1 - p, 3)
      display.value = from + (to - from) * eased
      if (p < 1) raf = requestAnimationFrame(tick)
    }
    raf = requestAnimationFrame(tick)
  }

  watch(
    () => (typeof source === 'function' ? source() : (source?.value !== undefined ? source.value : source)),
    animateTo,
    { immediate: true }
  )

  onUnmounted(() => cancelAnimationFrame(raf))

  return display
}
