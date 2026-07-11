import { useState } from 'react';
import { X, Calendar, ChevronDown, Check } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { DynamicIcon } from '../components/DynamicIcon';
import { expenseCategories, incomeCategories, getCategoryById } from '../data/categories';
import { formatMoney, generateId, formatDate } from '../utils/format';
import type { TransactionType, Transaction } from '../types';

interface AddTransactionModalProps {
  isOpen: boolean;
  onClose: () => void;
  editTransaction?: Transaction | null;
}

export const AddTransactionModal = ({ isOpen, onClose, editTransaction }: AddTransactionModalProps) => {
  const { accounts, addTransaction, updateTransaction } = useAppStore();
  const [type, setType] = useState<TransactionType>(editTransaction?.type || 'expense');
  const [amount, setAmount] = useState(editTransaction ? String(editTransaction.amount) : '');
  const [categoryId, setCategoryId] = useState(editTransaction?.categoryId || '');
  const [accountId, setAccountId] = useState(editTransaction?.accountId || (accounts[0]?.id || ''));
  const [note, setNote] = useState(editTransaction?.note || '');
  const [date, setDate] = useState(editTransaction?.date || new Date().toISOString().split('T')[0]);
  const [showAccountPicker, setShowAccountPicker] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);

  const categories = type === 'expense' ? expenseCategories : incomeCategories;

  if (!isOpen) return null;

  const handleAmountClick = (num: string) => {
    if (num === 'del') {
      setAmount(prev => prev.slice(0, -1));
    } else if (num === '.') {
      if (!amount.includes('.')) {
        setAmount(prev => prev + '.');
      }
    } else {
      if (amount.includes('.') && amount.split('.')[1]?.length >= 2) {
        return;
      }
      if (amount === '0' && num !== '.') {
        setAmount(num);
      } else {
        setAmount(prev => prev + num);
      }
    }
  };

  const handleSave = async () => {
    if (!amount || parseFloat(amount) <= 0 || !categoryId || !accountId) {
      return;
    }

    const transaction: Transaction = {
      id: editTransaction?.id || generateId(),
      type,
      amount: parseFloat(amount),
      categoryId,
      accountId,
      note: note || undefined,
      date,
      createdAt: editTransaction?.createdAt || new Date().toISOString(),
    };

    if (editTransaction) {
      await updateTransaction(transaction, editTransaction);
    } else {
      await addTransaction(transaction);
    }
    
    handleClose();
  };

  const handleClose = () => {
    setType('expense');
    setAmount('');
    setCategoryId('');
    setNote('');
    setDate(new Date().toISOString().split('T')[0]);
    onClose();
  };

  const selectedAccount = accounts.find(a => a.id === accountId);
  const selectedCategory = getCategoryById(categoryId);

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center bg-black/40" onClick={handleClose}>
      <div
        className="w-full max-w-md bg-white rounded-t-[30px] max-h-[90vh] flex flex-col animate-slide-up"
        onClick={e => e.stopPropagation()}
      >
        <div className="flex items-center justify-between px-5 py-4 border-b border-gray-100">
          <button onClick={handleClose} className="w-8 h-8 flex items-center justify-center">
            <X size={22} className="text-gray-500" />
          </button>
          <h3 className="font-semibold text-lg text-gray-800">
            {editTransaction ? '编辑记录' : '记一笔'}
          </h3>
          <button
            onClick={handleSave}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-all ${
              amount && categoryId
                ? 'bg-primary-500 text-white active:scale-95'
                : 'bg-gray-100 text-gray-400'
            }`}
          >
            保存
          </button>
        </div>

        <div className="flex justify-center gap-2 py-3 bg-gray-50/50">
          <button
            onClick={() => { setType('expense'); setCategoryId(''); }}
            className={`px-6 py-2 rounded-full text-sm font-medium transition-all ${
              type === 'expense'
                ? 'bg-red-500 text-white shadow-md shadow-red-200'
                : 'bg-white text-gray-600'
            }`}
          >
            支出
          </button>
          <button
            onClick={() => { setType('income'); setCategoryId(''); }}
            className={`px-6 py-2 rounded-full text-sm font-medium transition-all ${
              type === 'income'
                ? 'bg-green-500 text-white shadow-md shadow-green-200'
                : 'bg-white text-gray-600'
            }`}
          >
            收入
          </button>
        </div>

        <div className="px-5 py-4 text-center border-b border-gray-100">
          <span className={`text-4xl font-bold ${type === 'income' ? 'text-green-500' : 'text-gray-800'}`}>
            ¥{amount || '0'}
          </span>
        </div>

        <div className="flex-1 overflow-y-auto">
          <div className="px-5 py-4">
            <p className="text-sm text-gray-500 mb-3">选择分类</p>
            <div className="grid grid-cols-4 gap-3">
              {categories.map((category) => (
                <button
                  key={category.id}
                  onClick={() => setCategoryId(category.id)}
                  className={`flex flex-col items-center gap-1.5 p-2 rounded-2xl transition-all ${
                    categoryId === category.id
                      ? 'bg-primary-50 ring-2 ring-primary-400'
                      : 'active:bg-gray-50'
                  }`}
                >
                  <div
                    className="w-12 h-12 rounded-xl flex items-center justify-center transition-transform active:scale-95"
                    style={{ backgroundColor: category.color + '20' }}
                  >
                    <DynamicIcon name={category.icon} size={24} color={category.color} />
                  </div>
                  <span className="text-xs text-gray-600 font-medium">{category.name}</span>
                </button>
              ))}
            </div>
          </div>

          <div className="px-5 py-3 space-y-3">
            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-2xl">
              <span className="text-gray-600">账户</span>
              <button
                onClick={() => setShowAccountPicker(true)}
                className="flex items-center gap-2 text-gray-800 font-medium"
              >
                <div
                  className="w-6 h-6 rounded-lg flex items-center justify-center"
                  style={{ backgroundColor: (selectedAccount?.color || '#ccc') + '30' }}
                >
                  <DynamicIcon name={selectedAccount?.icon || 'Wallet'} size={14} color={selectedAccount?.color} />
                </div>
                {selectedAccount?.name || '选择账户'}
                <ChevronDown size={16} className="text-gray-400" />
              </button>
            </div>

            <div className="flex items-center justify-between p-4 bg-gray-50 rounded-2xl">
              <span className="text-gray-600">日期</span>
              <button
                onClick={() => setShowDatePicker(true)}
                className="flex items-center gap-2 text-gray-800 font-medium"
              >
                <Calendar size={16} className="text-primary-500" />
                {formatDate(date)}
              </button>
            </div>

            <div className="p-4 bg-gray-50 rounded-2xl">
              <input
                type="text"
                value={note}
                onChange={e => setNote(e.target.value)}
                placeholder="添加备注..."
                className="w-full bg-transparent outline-none text-gray-800 placeholder-gray-400"
                maxLength={50}
              />
            </div>
          </div>
        </div>

        <div className="p-4 bg-gray-50 border-t border-gray-100 safe-area-bottom">
          <div className="grid grid-cols-4 gap-2">
            {['1', '2', '3', 'del', '4', '5', '6', '.', '7', '8', '9', '0'].map((key, idx) => (
              <button
                key={idx}
                onClick={() => handleAmountClick(key)}
                className="h-12 bg-white rounded-xl font-semibold text-lg text-gray-800 active:bg-gray-100 transition-colors"
              >
                {key === 'del' ? '⌫' : key}
              </button>
            ))}
          </div>
        </div>

        {showAccountPicker && (
          <div
            className="absolute inset-0 bg-black/30 flex items-end"
            onClick={() => setShowAccountPicker(false)}
          >
            <div
              className="w-full bg-white rounded-t-[30px] p-5"
              onClick={e => e.stopPropagation()}
            >
              <h4 className="font-semibold text-lg mb-4 text-center">选择账户</h4>
              <div className="space-y-2 max-h-64 overflow-y-auto">
                {accounts.map((account) => (
                  <button
                    key={account.id}
                    onClick={() => {
                      setAccountId(account.id);
                      setShowAccountPicker(false);
                    }}
                    className={`w-full flex items-center justify-between p-4 rounded-2xl transition-colors ${
                      accountId === account.id
                        ? 'bg-primary-50 border-2 border-primary-400'
                        : 'bg-gray-50 active:bg-gray-100'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center"
                        style={{ backgroundColor: account.color + '20' }}
                      >
                        <DynamicIcon name={account.icon} size={20} color={account.color} />
                      </div>
                      <div className="text-left">
                        <p className="font-medium text-gray-800">{account.name}</p>
                        <p className="text-xs text-gray-400">余额: ¥{formatMoney(account.balance)}</p>
                      </div>
                    </div>
                    {accountId === account.id && (
                      <Check size={20} className="text-primary-500" />
                    )}
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}

        {showDatePicker && (
          <div
            className="absolute inset-0 bg-black/30 flex items-end"
            onClick={() => setShowDatePicker(false)}
          >
            <div
              className="w-full bg-white rounded-t-[30px] p-5"
              onClick={e => e.stopPropagation()}
            >
              <h4 className="font-semibold text-lg mb-4 text-center">选择日期</h4>
              <input
                type="date"
                value={date}
                onChange={e => setDate(e.target.value)}
                className="w-full p-4 bg-gray-50 rounded-2xl text-lg text-center outline-none focus:ring-2 focus:ring-primary-400"
              />
              <button
                onClick={() => setShowDatePicker(false)}
                className="w-full mt-4 py-3 bg-primary-500 text-white rounded-2xl font-medium active:scale-95 transition-transform"
              >
                确定
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
