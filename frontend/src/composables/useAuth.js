import { useRouter } from 'vue-router'

export const ROLES = { CUSTOMER: 'CUSTOMER', EMPLOYEE: 'EMPLOYEE' }

export function getToken() { return localStorage.getItem('token') }
export function getRole() { return localStorage.getItem('role') }

export function setAuth(token, role) {
  localStorage.setItem('token', token)
  localStorage.setItem('role', role)
}

export function clearAuth() {
  localStorage.removeItem('token')
  localStorage.removeItem('role')
}

export function useAuth() {
  const router = useRouter()

  function logout(redirect = '/login') {
    clearAuth()
    router.push(redirect)
  }

  return { logout }
}
