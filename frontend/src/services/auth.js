import api from './api'

export const login = (email, password) => api.post('/auth/login', { email, password })
export const register = (data) => api.post('/auth/register', data)
export const verifyPin = (pin) => api.post('/auth/verify-pin', { pin })
