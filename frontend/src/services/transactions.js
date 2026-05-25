import api from './api'

export const transfer = (data) => api.post('/transactions', { ...data, type: 'TRANSFER' })
export const atmDeposit = (data) => api.post('/transactions', { toIban: data.iban, amount: data.amount, description: data.description, type: 'DEPOSIT' })
export const atmWithdraw = (data) => api.post('/transactions', { fromIban: data.iban, amount: data.amount, description: data.description, type: 'WITHDRAWAL' })
export const getTransactions = (params) => api.get('/transactions', { params })
