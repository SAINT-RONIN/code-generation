export function fmtEUR(n, opts = {}) {
  const { sign = false, frac = 2 } = opts
  const formatter = new Intl.NumberFormat('nl-NL', { minimumFractionDigits: frac, maximumFractionDigits: frac })
  const s = formatter.format(Math.abs(n))
  return (sign ? (n >= 0 ? '+' : '−') : '') + '€ ' + s
}

export function a11yEUR(n) {
  const whole = Math.floor(Math.abs(n))
  const cents = Math.round((Math.abs(n) - whole) * 100)
  return `${n < 0 ? 'minus ' : ''}${whole} euros and ${cents} cents`
}
