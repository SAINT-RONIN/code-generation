import api from './api'

export const getCustomers = (params) => api.get('/customers', { params })
export const updateCustomer = (id, data) => api.put(`/customers/${id}`, data)
export const getAllAccounts = (params) => api.get('/accounts', { params })
export const createAccount = (data) => api.post('/accounts', data)
export const updateAccount = (iban, data) => api.put(`/accounts/${iban}`, data)
export const transferBetweenCustomers = (data) => api.post('/transactions', data)
