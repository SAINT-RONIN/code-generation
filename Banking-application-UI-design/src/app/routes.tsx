import { createBrowserRouter, Navigate } from 'react-router';
import { useBankStore } from '../store';
import AppLayout from './layout/AppLayout';
import AuthLayout from './layout/AuthLayout';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import PendingApproval from './pages/auth/PendingApproval';

import CustomerDashboard from './pages/customer/Dashboard';
import CustomerTransfer from './pages/customer/Transfer';

import EmployeeDashboard from './pages/employee/Dashboard';
import EmployeeApprovals from './pages/employee/Approvals';
import EmployeeCustomers from './pages/employee/Customers';
import EmployeeTransactions from './pages/employee/Transactions';
import EmployeeTransfer from './pages/employee/Transfer';

import AtmMenu from './pages/atm/Menu';
import AtmWithdraw from './pages/atm/Withdraw';
import AtmDeposit from './pages/atm/Deposit';
import AtmBalance from './pages/atm/Balance';

import CustomerAccounts from './pages/customer/Accounts';
import CustomerTransactions from './pages/customer/Transactions';
import CustomerProfile from './pages/customer/Profile';
import CustomerSendMoney from './pages/customer/SendMoney';
import CustomerFindIban from './pages/customer/FindIban';

const ProtectedRoute = ({ children, allowedRoles }: { children: React.ReactNode, allowedRoles?: string[] }) => {
  const { currentUser } = useBankStore();
  
  if (!currentUser) return <Navigate to="/login" replace />;
  if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
    return <Navigate to="/" replace />;
  }
  if (currentUser.role === 'customer' && currentUser.status === 'pending' && !window.location.pathname.includes('pending')) {
    return <Navigate to="/pending-approval" replace />;
  }
  
  return children;
};

// Placeholder for unbuilt pages
const Placeholder = ({ title }: { title: string }) => (
  <div className="flex h-full items-center justify-center text-gray-500">
    <div className="text-center">
      <h2 className="text-xl font-bold text-white mb-2">{title}</h2>
      <p>This page is under construction.</p>
    </div>
  </div>
);

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    children: [
      {
        index: true,
        element: <Navigate to="/login" replace />,
      },
      {
        path: 'login',
        element: <AuthLayout><Login /></AuthLayout>,
      },
      {
        path: 'register',
        element: <AuthLayout><Register /></AuthLayout>,
      },
      {
        path: 'pending-approval',
        element: <ProtectedRoute allowedRoles={['customer']}><AuthLayout><PendingApproval /></AuthLayout></ProtectedRoute>,
      },
      // Customer Routes
      {
        path: 'customer',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerDashboard /></ProtectedRoute>,
      },
      {
        path: 'customer/transfer',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerTransfer /></ProtectedRoute>,
      },
      {
        path: 'customer/accounts',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerAccounts /></ProtectedRoute>,
      },
      {
        path: 'customer/transactions',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerTransactions /></ProtectedRoute>,
      },
      {
        path: 'customer/profile',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerProfile /></ProtectedRoute>,
      },
      {
        path: 'customer/send',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerSendMoney /></ProtectedRoute>,
      },
      {
        path: 'customer/find',
        element: <ProtectedRoute allowedRoles={['customer']}><CustomerFindIban /></ProtectedRoute>,
      },
      // Employee Routes
      {
        path: 'employee',
        element: <ProtectedRoute allowedRoles={['employee']}><EmployeeDashboard /></ProtectedRoute>,
      },
      {
        path: 'employee/customers',
        element: <ProtectedRoute allowedRoles={['employee']}><EmployeeCustomers /></ProtectedRoute>,
      },
      {
        path: 'employee/approvals',
        element: <ProtectedRoute allowedRoles={['employee']}><EmployeeApprovals /></ProtectedRoute>,
      },
      {
        path: 'employee/transactions',
        element: <ProtectedRoute allowedRoles={['employee']}><EmployeeTransactions /></ProtectedRoute>,
      },
      {
        path: 'employee/transfer',
        element: <ProtectedRoute allowedRoles={['employee']}><EmployeeTransfer /></ProtectedRoute>,
      },
      // ATM Routes
      {
        path: 'atm',
        element: <ProtectedRoute allowedRoles={['atm']}><AtmMenu /></ProtectedRoute>,
      },
      {
        path: 'atm/withdraw',
        element: <ProtectedRoute allowedRoles={['atm']}><AtmWithdraw /></ProtectedRoute>,
      },
      {
        path: 'atm/deposit',
        element: <ProtectedRoute allowedRoles={['atm']}><AtmDeposit /></ProtectedRoute>,
      },
      {
        path: 'atm/balance',
        element: <ProtectedRoute allowedRoles={['atm']}><AtmBalance /></ProtectedRoute>,
      }
    ],
  },
]);