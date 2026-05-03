import { ref, onMounted, onUnmounted } from 'vue'

const COLORS = [
  '#7B61FF', '#00D9A3', '#FF5E5B', '#F59E0B',
  '#EC4899', '#06B6D4', '#A855F7', '#F97316',
  '#84CC16', '#EF4444',
]
const SIZES = [320, 240, 400, 210, 370, 280, 350, 190, 300, 260]

function rand(min, max) { return Math.random() * (max - min) + min }

export function useBubbles() {
  const blobs = ref([])
  let rafId = null

  onMounted(() => {
    const W = window.innerWidth
    const H = window.innerHeight

    blobs.value = COLORS.map((color, i) => {
      const size = SIZES[i]
      return {
        color, size,
        x: rand(0, W - size),
        y: rand(0, H - size),
        vx: rand(0.35, 0.85) * (Math.random() < 0.5 ? 1 : -1),
        vy: rand(0.35, 0.85) * (Math.random() < 0.5 ? 1 : -1),
      }
    })

    function resolveCollisions(bs) {
      for (let i = 0; i < bs.length; i++) {
        for (let j = i + 1; j < bs.length; j++) {
          const a = bs[i], b = bs[j]
          const ra = a.size / 2, rb = b.size / 2
          const cax = a.x + ra, cay = a.y + ra
          const cbx = b.x + rb, cby = b.y + rb
          const dx = cbx - cax, dy = cby - cay
          const dist = Math.sqrt(dx * dx + dy * dy)
          const minDist = ra + rb
          if (dist < minDist && dist > 0) {
            // Normal vector
            const nx = dx / dist, ny = dy / dist
            // Relative velocity along normal
            const dvn = (a.vx - b.vx) * nx + (a.vy - b.vy) * ny
            // Only resolve if moving toward each other
            if (dvn > 0) {
              a.vx -= dvn * nx
              a.vy -= dvn * ny
              b.vx += dvn * nx
              b.vy += dvn * ny
            }
            // Push apart so they don't stick
            const overlap = (minDist - dist) / 2
            a.x -= nx * overlap
            a.y -= ny * overlap
            b.x += nx * overlap
            b.y += ny * overlap
          }
        }
      }
    }

    function tick() {
      const W = window.innerWidth
      const H = window.innerHeight
      const bs = blobs.value.map(b => ({ ...b }))

      for (const b of bs) {
        b.x += b.vx
        b.y += b.vy
        if (b.x <= 0 || b.x + b.size >= W) {
          b.vx = -b.vx
          b.x = Math.max(0, Math.min(W - b.size, b.x))
        }
        if (b.y <= 0 || b.y + b.size >= H) {
          b.vy = -b.vy
          b.y = Math.max(0, Math.min(H - b.size, b.y))
        }
      }

      resolveCollisions(bs)
      blobs.value = bs
      rafId = requestAnimationFrame(tick)
    }

    rafId = requestAnimationFrame(tick)
  })

  onUnmounted(() => { if (rafId) cancelAnimationFrame(rafId) })

  return { blobs }
}
