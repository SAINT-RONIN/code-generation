import { create } from 'zustand';
import { v4 as uuidv4 } from 'uuid';

export type Role = 'customer' | 'employee' | 'atm';
export type AccountType = 'checking' | 'savings';
export type TransactionType = 'transfer' | 'deposit' | 'withdrawal' | 'salary' | 'payment';

export interface User {
  id: string;
  role: Role;
  email: string;
  name: string;
  bsn?: string;
  phone?: string;
  status: 'pending' | 'approved';
  createdAt: string;
}

export interface Account {
  id: string;
  userId: string;
  type: AccountType;
  iban: string;
  balance: number;
  dailyLimit: number;
  absoluteLimit: number;
}

export interface Transaction {
  id: string;
  fromAccountId?: string;
  toAccountId?: string;
  amount: number;
  type: TransactionType;
  timestamp: string;
  initiator: 'customer' | 'employee' | 'atm';
  description: string;
}

interface BankState {
  users: User[];
  accounts: Account[];
  transactions: Transaction[];
  currentUser: User | null;
  
  // Actions
  login: (email: string) => User | null;
  logout: () => void;
  register: (data: Omit<User, 'id' | 'role' | 'status' | 'createdAt'>) => void;
  
  // Customer Actions
  transfer: (fromId: string, toIban: string, amount: number, description: string) => boolean;
  
  // Employee Actions
  approveCustomer: (userId: string, limits: { daily: number; absolute: number }) => void;
  updateLimits: (accountId: string, dailyLimit: number, absoluteLimit: number) => void;
  closeAccount: (accountId: string) => void;
  
  // ATM Actions
  withdraw: (accountId: string, amount: number) => boolean;
  deposit: (accountId: string, amount: number) => void;
}

const mockUsers: User[] = [
  { id: 'u1', role: 'customer', email: 'john@example.com', name: 'John Doe', status: 'approved', createdAt: new Date().toISOString() },
  { id: 'u2', role: 'employee', email: 'staff@bank.nl', name: 'Sarah Admin', status: 'approved', createdAt: new Date().toISOString() },
  { id: 'u3', role: 'atm', email: 'atm@bank.nl', name: 'ATM Terminal 1', status: 'approved', createdAt: new Date().toISOString() },
  { id: 'u4', role: 'customer', email: 'pending@example.com', name: 'New User', status: 'pending', createdAt: new Date().toISOString() },
];

const mockAccounts: Account[] = [
  { id: 'a1', userId: 'u1', type: 'checking', iban: 'NL01INHO0123456789', balance: 5432.10, dailyLimit: 2000, absoluteLimit: 0 },
  { id: 'a2', userId: 'u1', type: 'savings', iban: 'NL02INHO0987654321', balance: 12500.00, dailyLimit: 5000, absoluteLimit: 0 },
];

const mockTransactions: Transaction[] = [
  { id: 't1', toAccountId: 'a1', amount: 3500, type: 'salary', timestamp: new Date(Date.now() - 86400000 * 2).toISOString(), initiator: 'employee', description: 'Tech Corp B.V. Salary' },
  { id: 't2', fromAccountId: 'a1', amount: 45.50, type: 'payment', timestamp: new Date(Date.now() - 86400000 * 1).toISOString(), initiator: 'customer', description: 'Albert Heijn Grocery' },
];

const generateIban = () => {
  const randomStr = Math.floor(Math.random() * 1000000000).toString().padStart(9, '0');
  const check = Math.floor(Math.random() * 90 + 10);
  return `NL${check}INHO0${randomStr}`;
};

export const useBankStore = create<BankState>((set, get) => ({
  users: mockUsers,
  accounts: mockAccounts,
  transactions: mockTransactions,
  currentUser: null,
  
  login: (email) => {
    const user = get().users.find(u => u.email === email);
    if (user) {
      set({ currentUser: user });
      return user;
    }
    return null;
  },
  
  logout: () => set({ currentUser: null }),
  
  register: (data) => {
    const newUser: User = {
      ...data,
      id: Math.random().toString(36).substr(2, 9),
      role: 'customer',
      status: 'pending',
      createdAt: new Date().toISOString()
    };
    set(state => ({ users: [...state.users, newUser] }));
  },
  
  transfer: (fromId, toIban, amount, description) => {
    const { accounts, transactions, currentUser } = get();
    const fromAccount = accounts.find(a => a.id === fromId);
    const toAccount = accounts.find(a => a.iban.replace(/\s/g, '') === toIban.replace(/\s/g, ''));
    
    if (!fromAccount || !toAccount) return false;
    
    // Check limits (simplified)
    if (fromAccount.balance - amount < fromAccount.absoluteLimit) return false;
    
    const newTx: Transaction = {
      id: Math.random().toString(),
      fromAccountId: fromAccount.id,
      toAccountId: toAccount.id,
      amount,
      type: 'transfer',
      timestamp: new Date().toISOString(),
      initiator: currentUser?.role || 'customer',
      description
    };
    
    set(state => ({
      transactions: [newTx, ...state.transactions],
      accounts: state.accounts.map(a => {
        if (a.id === fromAccount.id) return { ...a, balance: a.balance - amount };
        if (a.id === toAccount.id) return { ...a, balance: a.balance + amount };
        return a;
      })
    }));
    return true;
  },
  
  approveCustomer: (userId, limits) => {
    set(state => {
      const users = state.users.map(u => u.id === userId ? { ...u, status: 'approved' as const } : u);
      const newChecking: Account = {
        id: Math.random().toString(),
        userId,
        type: 'checking',
        iban: generateIban(),
        balance: 0,
        dailyLimit: limits.daily,
        absoluteLimit: limits.absolute
      };
      const newSavings: Account = {
        id: Math.random().toString(),
        userId,
        type: 'savings',
        iban: generateIban(),
        balance: 0,
        dailyLimit: limits.daily,
        absoluteLimit: limits.absolute
      };
      return { users, accounts: [...state.accounts, newChecking, newSavings] };
    });
  },
  
  updateLimits: (accountId, dailyLimit, absoluteLimit) => {
    set(state => ({
      accounts: state.accounts.map(a => a.id === accountId ? { ...a, dailyLimit, absoluteLimit } : a)
    }));
  },
  
  closeAccount: (accountId) => {
    set(state => ({
      accounts: state.accounts.filter(a => a.id !== accountId)
    }));
  },
  
  withdraw: (accountId, amount) => {
    const { accounts, transactions } = get();
    const account = accounts.find(a => a.id === accountId);
    if (!account) return false;
    
    if (account.balance - amount < account.absoluteLimit) return false;
    
    const newTx: Transaction = {
      id: Math.random().toString(),
      fromAccountId: account.id,
      amount,
      type: 'withdrawal',
      timestamp: new Date().toISOString(),
      initiator: 'atm',
      description: 'ATM Withdrawal'
    };
    
    set(state => ({
      transactions: [newTx, ...state.transactions],
      accounts: state.accounts.map(a => a.id === accountId ? { ...a, balance: a.balance - amount } : a)
    }));
    
    return true;
  },
  
  deposit: (accountId, amount) => {
    const newTx: Transaction = {
      id: Math.random().toString(),
      toAccountId: accountId,
      amount,
      type: 'deposit',
      timestamp: new Date().toISOString(),
      initiator: 'atm',
      description: 'ATM Deposit'
    };
    
    set(state => ({
      transactions: [newTx, ...state.transactions],
      accounts: state.accounts.map(a => a.id === accountId ? { ...a, balance: a.balance + amount } : a)
    }));
  }
}));