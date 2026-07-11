import { create } from 'zustand';
import type { Account, Transaction, Budget } from '../types';
import { accountDB, transactionDB, budgetDB, initDB } from '../db';

interface AppState {
  accounts: Account[];
  transactions: Transaction[];
  budgets: Budget[];
  isLoading: boolean;
  isInitialized: boolean;

  initApp: () => Promise<void>;
  refreshData: () => Promise<void>;

  addAccount: (account: Account) => Promise<void>;
  updateAccount: (account: Account) => Promise<void>;
  deleteAccount: (id: string) => Promise<void>;

  addTransaction: (transaction: Transaction) => Promise<void>;
  updateTransaction: (transaction: Transaction, oldTransaction: Transaction) => Promise<void>;
  deleteTransaction: (id: string) => Promise<void>;

  addBudget: (budget: Budget) => Promise<void>;
  updateBudget: (budget: Budget) => Promise<void>;
  deleteBudget: (id: string) => Promise<void>;

  getTotalBalance: () => number;
  getMonthlySummary: (month: string) => { income: number; expense: number };
  getCategoryExpense: (categoryId: string, month: string) => number;
}

export const useAppStore = create<AppState>((set, get) => ({
  accounts: [],
  transactions: [],
  budgets: [],
  isLoading: true,
  isInitialized: false,

  initApp: async () => {
    try {
      await initDB();
      await get().refreshData();
      set({ isInitialized: true, isLoading: false });
    } catch (error) {
      console.error('Failed to initialize app:', error);
      set({ isLoading: false });
    }
  },

  refreshData: async () => {
    const [accounts, transactions, budgets] = await Promise.all([
      accountDB.getAll(),
      transactionDB.getAll(),
      budgetDB.getAll(),
    ]);
    set({ accounts, transactions, budgets });
  },

  addAccount: async (account) => {
    await accountDB.add(account);
    await get().refreshData();
  },

  updateAccount: async (account) => {
    await accountDB.update(account);
    await get().refreshData();
  },

  deleteAccount: async (id) => {
    await accountDB.delete(id);
    await get().refreshData();
  },

  addTransaction: async (transaction) => {
    await transactionDB.add(transaction);
    await get().refreshData();
  },

  updateTransaction: async (transaction, oldTransaction) => {
    await transactionDB.update(transaction, oldTransaction);
    await get().refreshData();
  },

  deleteTransaction: async (id) => {
    await transactionDB.delete(id);
    await get().refreshData();
  },

  addBudget: async (budget) => {
    await budgetDB.add(budget);
    await get().refreshData();
  },

  updateBudget: async (budget) => {
    await budgetDB.update(budget);
    await get().refreshData();
  },

  deleteBudget: async (id) => {
    await budgetDB.delete(id);
    await get().refreshData();
  },

  getTotalBalance: () => {
    return get().accounts.reduce((sum, acc) => sum + acc.balance, 0);
  },

  getMonthlySummary: (month) => {
    const { transactions } = get();
    const monthTxs = transactions.filter(t => t.date.startsWith(month));
    
    const income = monthTxs
      .filter(t => t.type === 'income')
      .reduce((sum, t) => sum + t.amount, 0);
    
    const expense = monthTxs
      .filter(t => t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);
    
    return { income, expense };
  },

  getCategoryExpense: (categoryId, month) => {
    const { transactions } = get();
    return transactions
      .filter(t => t.date.startsWith(month) && t.categoryId === categoryId && t.type === 'expense')
      .reduce((sum, t) => sum + t.amount, 0);
  },
}));
