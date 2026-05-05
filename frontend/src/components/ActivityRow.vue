<script setup>
import { computed } from 'vue'
import { ArrowLeftRight, Send, Banknote } from 'lucide-vue-next'

const props = defineProps({
  tx: { type: Object, required: true },
  myIbans: { type: Array, default: () => [] },
  masked: { type: Boolean, default: false },
})

function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}

const isCredit = computed(() => props.myIbans.some(iban => iban === props.tx.toIban))

const TxIcon = computed(() => {
  const t = (props.tx.transactionType ?? '').toUpperCase()
  if (t.includes('ATM')) return Banknote
  if (t === 'TRANSFER') return ArrowLeftRight
  return Send
})

const counterparty = computed(() => isCredit.value ? props.tx.fromIban : props.tx.toIban)

const description = computed(() =>
  props.tx.description || (props.tx.transactionType ?? '').toLowerCase().replace(/_/g, ' ') || '—'
)

const date = computed(() => {
  if (!props.tx.timestamp) return '—'
  return new Date(props.tx.timestamp).toLocaleString('en-GB', {
    day: 'numeric', month: 'short', hour: '2-digit', minute: '2-digit',
  })
})

const amountStr = computed(() => {
  if (props.masked) return '€ ••••'
  return (isCredit.value ? '+ ' : '− ') + eur(props.tx.amount)
})
</script>

<template>
  <div class="row grid grid-cols-12 items-center gap-2 px-4 py-3 rounded-xl cursor-pointer">
    <div class="col-span-7 flex items-center gap-3 min-w-0">
      <span
        class="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
        :style="{ background: isCredit ? 'var(--accent-soft)' : 'var(--surface-2)', color: isCredit ? 'var(--accent)' : 'var(--ink-2)' }"
      >
        <component :is="TxIcon" class="w-4 h-4" />
      </span>
      <div class="min-w-0">
        <div class="text-xs font-mono truncate" :style="{ color: 'var(--ink)' }">{{ counterparty }}</div>
        <div class="text-xs truncate" :style="{ color: 'var(--ink-3)' }">{{ description }}</div>
      </div>
    </div>
    <div class="col-span-2 hidden md:block text-xs" :style="{ color: 'var(--ink-3)' }">{{ date }}</div>
    <div
      class="col-span-3 text-right text-sm font-medium tabnum"
      :style="{ color: isCredit ? 'var(--credit)' : 'var(--debit)' }"
    >{{ amountStr }}</div>
  </div>
</template>
