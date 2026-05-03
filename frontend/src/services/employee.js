import api from './api'

export const getPendingCustomers = () => api.get('/employees/customers/pending')
export const approveCustomer = (id, data) => api.post(`/employees/customers/${id}/approve`, data)
export const getAllAccounts = (params) => api.get('/employees/accounts', { params })
export const updateLimits = (iban, data) => api.put(`/employees/accounts/${iban}/limits`, data)
export const closeAccount = (iban) => api.delete(`/employees/accounts/${iban}`)
export const transferBetweenCustomers = (data) => api.post('/employees/transactions', data)
