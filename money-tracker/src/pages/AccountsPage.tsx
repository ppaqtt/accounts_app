import { useState } from 'react';
import { Plus, MoreHorizontal, X, Check } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { DynamicIcon } from '../components/DynamicIcon';
import { formatMoney, generateId } from '../utils/format';
import type { Account } from '../types';

const accountIcons = ['Wallet', 'Banknote', 'CreditCard', 'Smartphone', 'PiggyBank', 'Briefcase', 'Gift', 'Star'];
const accountColors = ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4', '#f97316'];

export const AccountsPage = () => {
  const { accounts, addAccount, updateAccount, deleteAccount, getTotalBalance, transactions } = useAppStore();
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingAccount, setEditingAccount] = useState<Account | null>(null);
  const [showActionSheet, setShowActionSheet] = useState<string | null>(null);

  const [name, setName] = useState('');
  const [balance, setBalance] = useState('');
  const [icon, setIcon] = useState('Wallet');
  const [color, setColor] = useState(accountColors[0]);

  const totalBalance = getTotalBalance();

  const handleOpenAdd = () => {
    setEditingAccount(null);
    setName('');
    setBalance('');
    setIcon('Wallet');
    setColor(accountColors[0]);
    setShowAddModal(true);
  };

  const handleOpenEdit = (account: Account) => {
    setEditingAccount(account);
    setName(account.name);
    setBalance(String(account.balance));
    setIcon(account.icon);
    setColor(account.color);
    setShowAddModal(true);
    setShowActionSheet(null);
  };

  const handleSave = async () => {
    if (!name.trim()) return;

    if (editingAccount) {
      const newBalance = parseFloat(balance) || 0;
      await updateAccount({
        ...editingAccount,
        name: name.trim(),
        balance: newBalance,
        icon,
        color,
      });
    } else {
      const newAccount: Account = {
        id: generateId(),
        name: name.trim(),
        icon,
        balance: parseFloat(balance) || 0,
        color,
        type: 'other',
        createdAt: new Date().toISOString(),
      };
      await addAccount(newAccount);
    }

    setShowAddModal(false);
  };

  const handleDelete = async (id: string) => {
    const accountTxs = transactions.filter(t => t.accountId === id);
    if (accountTxs.length > 0) {
      alert('该账户下有交易记录，无法删除');
      setShowActionSheet(null);
      return;
    }
    if (confirm('确定要删除这个账户吗？')) {
      await deleteAccount(id);
    }
    setShowActionSheet(null);
  };

  return (
    <div className="pb-24">
      <div className="bg-gradient-to-br from-cute-blue to-cute-purple px-5 pt-12 pb-16 rounded-b-[40px] text-white">
        <h2 className="text-xl font-bold mb-1">我的账户</h2>
        <p className="text-white/80 text-sm mb-6">管理你的所有资金账户</p>
        
        <div className="bg-white/20 backdrop-blur-sm rounded-2xl p-4">
          <p className="text-white/80 text-sm mb-1">总余额</p>
          <p className="text-3xl font-bold">¥{formatMoney(totalBalance)}</p>
        </div>
      </div>

      <div className="px-5 -mt-8 relative z-10">
        <button
          onClick={handleOpenAdd}
          className="w-full cute-card p-4 flex items-center justify-center gap-2 text-primary-500 font-medium mb-4 active:scale-[0.98] transition-transform"
        >
          <Plus size={20} />
          添加新账户
        </button>

        <div className="space-y-3">
          {accounts.map((account) => {
            const accountTxs = transactions.filter(t => t.accountId === account.id).length;
            
            return (
              <div key={account.id} className="cute-card p-4">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div
                      className="w-12 h-12 rounded-xl flex items-center justify-center"
                      style={{ backgroundColor: account.color + '20' }}
                    >
                      <DynamicIcon name={account.icon} size={24} color={account.color} />
                    </div>
                    <div>
                      <p className="font-semibold text-gray-800">{account.name}</p>
                      <p className="text-xs text-gray-400">{accountTxs} 笔交易</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="text-right">
                      <p className="font-bold text-gray-800">¥{formatMoney(account.balance)}</p>
                    </div>
                    <button
                      onClick={() => setShowActionSheet(showActionSheet === account.id ? null : account.id)}
                      className="w-8 h-8 flex items-center justify-center text-gray-400"
                    >
                      <MoreHorizontal size={20} />
                    </button>
                  </div>
                </div>
                
                {showActionSheet === account.id && (
                  <div className="mt-3 pt-3 border-t border-gray-100 flex gap-2">
                    <button
                      onClick={() => handleOpenEdit(account)}
                      className="flex-1 py-2 bg-gray-50 rounded-xl text-sm font-medium text-gray-600 active:bg-gray-100"
                    >
                      编辑
                    </button>
                    <button
                      onClick={() => handleDelete(account.id)}
                      className="flex-1 py-2 bg-red-50 rounded-xl text-sm font-medium text-red-500 active:bg-red-100"
                    >
                      删除
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      </div>

      {showAddModal && (
        <div
          className="fixed inset-0 z-50 bg-black/40 flex items-end"
          onClick={() => setShowAddModal(false)}
        >
          <div
            className="w-full max-w-md mx-auto bg-white rounded-t-[30px] p-5 safe-area-bottom"
            onClick={e => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-6">
              <button onClick={() => setShowAddModal(false)}>
                <X size={22} className="text-gray-500" />
              </button>
              <h3 className="font-semibold text-lg">
                {editingAccount ? '编辑账户' : '添加账户'}
              </h3>
              <button
                onClick={handleSave}
                disabled={!name.trim()}
                className={`font-medium ${
                  name.trim() ? 'text-primary-500' : 'text-gray-300'
                }`}
              >
                保存
              </button>
            </div>

            <div className="space-y-5">
              <div>
                <label className="text-sm text-gray-500 mb-2 block">账户名称</label>
                <input
                  type="text"
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="输入账户名称"
                  className="w-full cute-input"
                  maxLength={20}
                />
              </div>

              <div>
                <label className="text-sm text-gray-500 mb-2 block">初始余额</label>
                <input
                  type="number"
                  value={balance}
                  onChange={e => setBalance(e.target.value)}
                  placeholder="0.00"
                  className="w-full cute-input"
                />
              </div>

              <div>
                <label className="text-sm text-gray-500 mb-2 block">选择图标</label>
                <div className="grid grid-cols-4 gap-3">
                  {accountIcons.map((iconName) => (
                    <button
                      key={iconName}
                      onClick={() => setIcon(iconName)}
                      className={`h-14 rounded-xl flex items-center justify-center transition-all ${
                        icon === iconName
                          ? 'bg-primary-50 ring-2 ring-primary-400'
                          : 'bg-gray-50 active:bg-gray-100'
                      }`}
                    >
                      <DynamicIcon name={iconName} size={24} color={icon === iconName ? color : '#9ca3af'} />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-500 mb-2 block">选择颜色</label>
                <div className="flex gap-3 flex-wrap">
                  {accountColors.map((c) => (
                    <button
                      key={c}
                      onClick={() => setColor(c)}
                      className={`w-10 h-10 rounded-full transition-all flex items-center justify-center ${
                        color === c ? 'ring-2 ring-offset-2' : ''
                      }`}
                      style={{ backgroundColor: c, ['--tw-ring-color' as never]: c }}
                    >
                      {color === c && <Check size={18} className="text-white" />}
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
