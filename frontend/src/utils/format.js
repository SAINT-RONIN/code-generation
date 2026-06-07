export function eur(val) {
  return new Intl.NumberFormat('nl-NL', { style: 'currency', currency: 'EUR' }).format(Number(val) || 0)
}
