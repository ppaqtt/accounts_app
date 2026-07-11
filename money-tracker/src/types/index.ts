export type TransactionType = 'income' | 'expense';

export interface Category {
  id: string;
  name: string;
  icon: string;
  type: TransactionType;
  color: string;
}

export interface Account {
  id: string;
  name: string;
  icon: string;
  balance: number;
  color: string;
  type: 'cash' | 'bank' | 'e-wallet' | 'credit-card' | 'other';
  createdAt: string;
}

export interface Transaction {
  id: string;
  type: TransactionType;
  amount: number;
  categoryId: string;
  accountId: string;
  note?: string;
  date: string;
  createdAt: string;
}

export interface Budget {
  id: string;
  categoryId: string;
  amount: number;
  period: 'monthly' | 'weekly';
  month: string;
  createdAt: string;
}

export interface MonthlySummary {
  month: string;
  totalIncome: number;
  totalExpense: number;
  balance: number;
}
