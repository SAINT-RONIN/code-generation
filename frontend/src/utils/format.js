export function fmtEUR(n, opts = {}) {
  const { sign = false, frac = 2 } = opts
  const formatter = new Intl.NumberFormat('nl-NL', { minimumFractionDigits: frac, maximumFractionDigits: frac })
  const s = formatter.format(Math.abs(n))
  return (sign ? (n >= 0 ? '+' : '−') : '') + '€ ' + s
}

export function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}
