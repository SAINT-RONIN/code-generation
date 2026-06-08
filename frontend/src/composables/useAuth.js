import { useRouter } from 'vue-router'

export const ROLES = { CUSTOMER: 'CUSTOMER', EMPLOYEE: 'EMPLOYEE' }

// localStorage keys for JWT persistence across page refreshes
const TOKEN_KEY = 'jwt_token'
const ROLE_KEY  = 'jwt_role'

// Retrieves the JWT token from localStorage (returns null if not logged in)
export function getToken() { return localStorage.getItem(TOKEN_KEY) }

// Retrieves the user role (CUSTOMER or EMPLOYEE) from localStorage
export function getRole()  { return localStorage.getItem(ROLE_KEY) }

// Stores the JWT token and role in localStorage after a successful login
export function setAuth(token, role) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(ROLE_KEY, role)
}

// Removes the JWT token and role from localStorage on logout
export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
}

// Composable that provides a logout function for use inside Vue components
export function useAuth() {
  const router = useRouter()

  // Clears auth state and redirects the user to the login page
  function logout(redirect = '/login') {
    clearAuth()
    router.push(redirect)
  }

  return { logout }
}
