import api from './api'

export const transfer = (data) => api.post('/transactions', data)
export const atmDeposit = (data) => api.post('/transactions/atm/deposit', data)
export const atmWithdraw = (data) => api.post('/transactions/atm/withdraw', data)
export const getHistory = (iban, params) => api.get(`/transactions/${iban}`, { params })
export const getAll = (params) => api.get('/transactions', { params })
