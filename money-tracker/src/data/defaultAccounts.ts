import type { Account } from '../types';

export const defaultAccounts: Account[] = [
  {
    id: 'cash',
    name: '现金',
    icon: 'Banknote',
    balance: 0,
    color: '#22c55e',
    type: 'cash',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'wechat',
    name: '微信钱包',
    icon: 'MessageCircle',
    balance: 0,
    color: '#22c55e',
    type: 'e-wallet',
    createdAt: new Date().toISOString(),
  },
  {
    id: 'alipay',
    name: '支付宝',
    icon: 'Smartphone',
    balance: 0,
    color: '#3b82f6',
    type: 'e-wallet',
    createdAt: new Date().toISOString(),
  },
];
