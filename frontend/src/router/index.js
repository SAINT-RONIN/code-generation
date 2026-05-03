import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/register', component: () => import('../views/RegisterView.vue'), meta: { public: true } },
  { path: '/pending-approval', component: () => import('../views/PendingApprovalView.vue') },

  { path: '/customer', component: () => import('../views/customer/DashboardView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/customer/accounts', component: () => import('../views/customer/AccountsView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/customer/transfer', component: () => import('../views/customer/TransferView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/customer/transactions', component: () => import('../views/customer/TransactionsView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/customer/find', component: () => import('../views/customer/FindIbanView.vue'), meta: { role: 'CUSTOMER' } },

  { path: '/employee', component: () => import('../views/employee/DashboardView.vue'), meta: { role: 'EMPLOYEE' } },
  { path: '/employee/approvals', component: () => import('../views/employee/ApprovalsView.vue'), meta: { role: 'EMPLOYEE' } },
  { path: '/employee/customers', component: () => import('../views/employee/CustomersView.vue'), meta: { role: 'EMPLOYEE' } },
  { path: '/employee/transfer', component: () => import('../views/employee/TransferView.vue'), meta: { role: 'EMPLOYEE' } },
  { path: '/employee/transactions', component: () => import('../views/employee/TransactionsView.vue'), meta: { role: 'EMPLOYEE' } },

  { path: '/atm', component: () => import('../views/atm/AtmMenuView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/atm/deposit', component: () => import('../views/atm/AtmDepositView.vue'), meta: { role: 'CUSTOMER' } },
  { path: '/atm/withdraw', component: () => import('../views/atm/AtmWithdrawView.vue'), meta: { role: 'CUSTOMER' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.public) return true
  if (!token) return '/login'
  if (to.meta.role && to.meta.role !== role) return role === 'EMPLOYEE' ? '/employee' : '/customer'
  return true
})

export default router
