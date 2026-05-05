<script setup>
import { computed } from 'vue'

const props = defineProps({
  data: { type: Array, required: true },
})

const w = 520, h = 120, pad = 8

const points = computed(() => {
  const min = Math.min(...props.data)
  const max = Math.max(...props.data)
  const range = max - min || 1
  const stepX = (w - pad * 2) / (props.data.length - 1)
  return props.data.map((v, i) => [
    pad + i * stepX,
    h - pad - ((v - min) / range) * (h - pad * 2),
  ])
})

const linePath = computed(() =>
  points.value.map((p, i) => (i === 0 ? `M ${p[0]} ${p[1]}` : `L ${p[0]} ${p[1]}`)).join(' ')
)

const areaPath = computed(() => {
  const pts = points.value
  return `${linePath.value} L ${pts[pts.length - 1][0]} ${h - pad} L ${pad} ${h - pad} Z`
})

const lastPoint = computed(() => points.value[points.value.length - 1])
</script>

<template>
  <svg :viewBox="`0 0 ${w} ${h}`" class="w-full h-[120px]" aria-hidden="true">
    <defs>
      <linearGradient id="sparkFill" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" stop-color="var(--accent)" stop-opacity="0.16" />
        <stop offset="100%" stop-color="var(--accent)" stop-opacity="0" />
      </linearGradient>
    </defs>
    <path :d="areaPath" fill="url(#sparkFill)" />
    <path
      :d="linePath"
      class="spark-path"
      stroke="var(--accent)"
      stroke-width="1.6"
      fill="none"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <circle :cx="lastPoint[0]" :cy="lastPoint[1]" r="6" fill="var(--bg)" />
    <circle :cx="lastPoint[0]" :cy="lastPoint[1]" r="3.2" fill="var(--accent)" />
  </svg>
</template>
