import api from './api'

export const getMyAccounts = () => api.get('/accounts/me')
export const searchByName = (firstName, lastName, iban = '') =>
  api.get('/accounts/search', { params: { firstName, lastName, iban } })
