import { useRouter } from 'vue-router'

export const ROLES = { CUSTOMER: 'CUSTOMER', EMPLOYEE: 'EMPLOYEE' }

let _token = null
let _role = null

export function getToken() { return _token }
export function getRole() { return _role }

export function setAuth(token, role) {
  _token = token
  _role = role
}

export function clearAuth() {
  _token = null
  _role = null
}

export function useAuth() {
  const router = useRouter()

  function logout(redirect = '/login') {
    clearAuth()
    router.push(redirect)
  }

  return { logout }
}
