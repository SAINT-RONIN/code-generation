import api from './api'

export const transfer = (data) => api.post('/transactions', data)
export const atmDeposit = (data) => api.post('/transactions', { toIban: data.iban, amount: data.amount, description: data.description })
export const atmWithdraw = (data) => api.post('/transactions', { fromIban: data.iban, amount: data.amount, description: data.description })
export const getTransactions = (params) => api.get('/transactions', { params })
