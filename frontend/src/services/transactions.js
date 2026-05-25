import api from './api'

export const transfer = (data) => api.post('/transactions', data)
export const atmDeposit = (data) => api.post('/transactions/deposits', data)
export const atmWithdraw = (data) => api.post('/transactions/withdrawals', data)
export const getTransactions = (params) => api.get('/transactions', { params })
