import type { Category } from '../types';

export const expenseCategories: Category[] = [
  { id: 'food', name: '餐饮', icon: 'Utensils', type: 'expense', color: '#FF9ECD' },
  { id: 'transport', name: '交通', icon: 'Car', type: 'expense', color: '#A7C7E7' },
  { id: 'shopping', name: '购物', icon: 'ShoppingBag', type: 'expense', color: '#C9A7EB' },
  { id: 'entertainment', name: '娱乐', icon: 'Gamepad2', type: 'expense', color: '#FFEAA7' },
  { id: 'housing', name: '居家', icon: 'Home', type: 'expense', color: '#B5EAD7' },
  { id: 'medical', name: '医疗', icon: 'Heart', type: 'expense', color: '#FFB3B3' },
  { id: 'education', name: '学习', icon: 'BookOpen', type: 'expense', color: '#FFCBA4' },
  { id: 'travel', name: '旅行', icon: 'Plane', type: 'expense', color: '#98D8C8' },
  { id: 'bill', name: '账单', icon: 'Receipt', type: 'expense', color: '#DDA0DD' },
  { id: 'gift', name: '礼物', icon: 'Gift', type: 'expense', color: '#FFB6C1' },
  { id: 'pet', name: '宠物', icon: 'PawPrint', type: 'expense', color: '#F0E68C' },
  { id: 'other-expense', name: '其他', icon: 'MoreHorizontal', type: 'expense', color: '#D3D3D3' },
];

export const incomeCategories: Category[] = [
  { id: 'salary', name: '工资', icon: 'Wallet', type: 'income', color: '#22c55e' },
  { id: 'bonus', name: '奖金', icon: 'Trophy', type: 'income', color: '#fbbf24' },
  { id: 'investment', name: '理财', icon: 'TrendingUp', type: 'income', color: '#3b82f6' },
  { id: 'part-time', name: '兼职', icon: 'Briefcase', type: 'income', color: '#8b5cf6' },
  { id: 'red-packet', name: '红包', icon: 'Heart', type: 'income', color: '#ef4444' },
  { id: 'refund', name: '退款', icon: 'ArrowLeftCircle', type: 'income', color: '#06b6d4' },
  { id: 'other-income', name: '其他', icon: 'MoreHorizontal', type: 'income', color: '#6b7280' },
];

export const allCategories: Category[] = [...expenseCategories, ...incomeCategories];

export const getCategoryById = (id: string): Category | undefined => {
  return allCategories.find(c => c.id === id);
};
