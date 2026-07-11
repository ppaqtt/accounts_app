import type { Account, Transaction, Budget } from '../types';
import { defaultAccounts } from '../data/defaultAccounts';

const STORAGE_KEYS = {
  accounts: 'money-tracker-accounts',
  transactions: 'money-tracker-transactions',
  budgets: 'money-tracker-budgets',
  initialized: 'money-tracker-initialized',
};

const getFromStorage = <T>(key: string, defaultValue: T): T => {
  try {
    const data = localStorage.getItem(key);
    return data ? JSON.parse(data) : defaultValue;
  } catch {
    return defaultValue;
  }
};

const setToStorage = <T>(key: string, value: T): void => {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (error) {
    console.error('Failed to save to localStorage:', error);
  }
};

export const initDB = async (): Promise<void> => {
  const initialized = localStorage.getItem(STORAGE_KEYS.initialized);
  if (!initialized) {
    setToStorage(STORAGE_KEYS.accounts, defaultAccounts);
    setToStorage(STORAGE_KEYS.transactions, []);
    setToStorage(STORAGE_KEYS.budgets, []);
    localStorage.setItem(STORAGE_KEYS.initialized, 'true');
  }
};

export const accountDB = {
  async getAll(): Promise<Account[]> {
    return getFromStorage<Account[]>(STORAGE_KEYS.accounts, []);
  },

  async getById(id: string): Promise<Account | undefined> {
    const accounts = await this.getAll();
    return accounts.find(a => a.id === id);
  },

  async add(account: Account): Promise<void> {
    const accounts = await this.getAll();
    accounts.push(account);
    setToStorage(STORAGE_KEYS.accounts, accounts);
  },

  async update(account: Account): Promise<void> {
    const accounts = await this.getAll();
    const index = accounts.findIndex(a => a.id === account.id);
    if (index !== -1) {
      accounts[index] = account;
      setToStorage(STORAGE_KEYS.accounts, accounts);
    }
  },

  async delete(id: string): Promise<void> {
    const accounts = await this.getAll();
    const filtered = accounts.filter(a => a.id !== id);
    setToStorage(STORAGE_KEYS.accounts, filtered);
  },

  async updateBalance(accountId: string, amount: number): Promise<void> {
    const accounts = await this.getAll();
    const account = accounts.find(a => a.id === accountId);
    if (account) {
      account.balance += amount;
      setToStorage(STORAGE_KEYS.accounts, accounts);
    }
  },
};

export const transactionDB = {
  async getAll(): Promise<Transaction[]> {
    const txs = getFromStorage<Transaction[]>(STORAGE_KEYS.transactions, []);
    return txs.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
  },

  async getByDateRange(startDate: string, endDate: string): Promise<Transaction[]> {
    const all = await this.getAll();
    return all.filter(t => t.date >= startDate && t.date <= endDate);
  },

  async getByAccount(accountId: string): Promise<Transaction[]> {
    const all = await this.getAll();
    return all.filter(t => t.accountId === accountId);
  },

  async add(transaction: Transaction): Promise<void> {
    const txs = getFromStorage<Transaction[]>(STORAGE_KEYS.transactions, []);
    const accounts = getFromStorage<Account[]>(STORAGE_KEYS.accounts, []);
    
    const account = accounts.find(a => a.id === transaction.accountId);
    if (account) {
      const amountChange = transaction.type === 'income' ? transaction.amount : -transaction.amount;
      account.balance += amountChange;
    }
    
    txs.push(transaction);
    setToStorage(STORAGE_KEYS.transactions, txs);
    setToStorage(STORAGE_KEYS.accounts, accounts);
  },

  async update(transaction: Transaction, oldTransaction: Transaction): Promise<void> {
    const txs = getFromStorage<Transaction[]>(STORAGE_KEYS.transactions, []);
    const accounts = getFromStorage<Account[]>(STORAGE_KEYS.accounts, []);
    
    const oldAmountChange = oldTransaction.type === 'income' ? -oldTransaction.amount : oldTransaction.amount;
    const newAmountChange = transaction.type === 'income' ? transaction.amount : -transaction.amount;
    
    if (oldTransaction.accountId === transaction.accountId) {
      const account = accounts.find(a => a.id === transaction.accountId);
      if (account) {
        account.balance += oldAmountChange + newAmountChange;
      }
    } else {
      const oldAccount = accounts.find(a => a.id === oldTransaction.accountId);
      if (oldAccount) {
        oldAccount.balance += oldAmountChange;
      }
      const newAccount = accounts.find(a => a.id === transaction.accountId);
      if (newAccount) {
        newAccount.balance += newAmountChange;
      }
    }
    
    const index = txs.findIndex(t => t.id === transaction.id);
    if (index !== -1) {
      txs[index] = transaction;
    }
    
    setToStorage(STORAGE_KEYS.transactions, txs);
    setToStorage(STORAGE_KEYS.accounts, accounts);
  },

  async delete(id: string): Promise<void> {
    const txs = getFromStorage<Transaction[]>(STORAGE_KEYS.transactions, []);
    const accounts = getFromStorage<Account[]>(STORAGE_KEYS.accounts, []);
    
    const transaction = txs.find(t => t.id === id);
    if (transaction) {
      const amountChange = transaction.type === 'income' ? -transaction.amount : transaction.amount;
      const account = accounts.find(a => a.id === transaction.accountId);
      if (account) {
        account.balance += amountChange;
      }
    }
    
    const filtered = txs.filter(t => t.id !== id);
    setToStorage(STORAGE_KEYS.transactions, filtered);
    setToStorage(STORAGE_KEYS.accounts, accounts);
  },
};

export const budgetDB = {
  async getAll(): Promise<Budget[]> {
    return getFromStorage<Budget[]>(STORAGE_KEYS.budgets, []);
  },

  async add(budget: Budget): Promise<void> {
    const budgets = await this.getAll();
    budgets.push(budget);
    setToStorage(STORAGE_KEYS.budgets, budgets);
  },

  async update(budget: Budget): Promise<void> {
    const budgets = await this.getAll();
    const index = budgets.findIndex(b => b.id === budget.id);
    if (index !== -1) {
      budgets[index] = budget;
      setToStorage(STORAGE_KEYS.budgets, budgets);
    }
  },

  async delete(id: string): Promise<void> {
    const budgets = await this.getAll();
    const filtered = budgets.filter(b => b.id !== id);
    setToStorage(STORAGE_KEYS.budgets, filtered);
  },
};
