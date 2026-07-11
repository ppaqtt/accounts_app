import { useState } from 'react';
import { Plus, X, Target, AlertTriangle, Check } from 'lucide-react';
import { useAppStore } from '../store/useAppStore';
import { DynamicIcon } from '../components/DynamicIcon';
import { expenseCategories } from '../data/categories';
import { formatMoney, generateId, getCurrentMonth } from '../utils/format';
import type { Budget } from '../types';

export const BudgetPage = () => {
  const { budgets, addBudget, updateBudget, deleteBudget, getCategoryExpense } = useAppStore();
  const currentMonth = getCurrentMonth();
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingBudget, setEditingBudget] = useState<Budget | null>(null);
  const [selectedCategoryId, setSelectedCategoryId] = useState('');
  const [amount, setAmount] = useState('');

  const monthBudgets = budgets.filter(b => b.month === currentMonth);

  const totalBudget = monthBudgets.reduce((sum, b) => sum + b.amount, 0);
  const totalSpent = monthBudgets.reduce((sum, b) => sum + getCategoryExpense(b.categoryId, currentMonth), 0);
  const totalRemaining = totalBudget - totalSpent;

  const handleOpenAdd = () => {
    setEditingBudget(null);
    setSelectedCategoryId('');
    setAmount('');
    setShowAddModal(true);
  };

  const handleOpenEdit = (budget: Budget) => {
    setEditingBudget(budget);
    setSelectedCategoryId(budget.categoryId);
    setAmount(String(budget.amount));
    setShowAddModal(true);
  };

  const handleSave = async () => {
    if (!selectedCategoryId || !amount || parseFloat(amount) <= 0) return;

    if (editingBudget) {
      await updateBudget({
        ...editingBudget,
        categoryId: selectedCategoryId,
        amount: parseFloat(amount),
      });
    } else {
      const existingBudget = monthBudgets.find(b => b.categoryId === selectedCategoryId);
      if (existingBudget) {
        alert('该分类已有预算，请编辑现有预算');
        return;
      }

      const newBudget: Budget = {
        id: generateId(),
        categoryId: selectedCategoryId,
        amount: parseFloat(amount),
        period: 'monthly',
        month: currentMonth,
        createdAt: new Date().toISOString(),
      };
      await addBudget(newBudget);
    }

    setShowAddModal(false);
  };

  const handleDelete = async (id: string) => {
    if (confirm('确定要删除这个预算吗？')) {
      await deleteBudget(id);
    }
  };

  const availableCategories = expenseCategories.filter(
    c => !monthBudgets.some(b => b.categoryId === c.id && b.id !== editingBudget?.id)
  );

  return (
    <div className="pb-24">
      <div className="bg-gradient-to-br from-cute-mint to-cute-blue px-5 pt-12 pb-16 rounded-b-[40px] text-white">
        <h2 className="text-xl font-bold mb-1">预算管理</h2>
        <p className="text-white/80 text-sm mb-6">合理规划，掌控消费</p>
        
        <div className="bg-white/20 backdrop-blur-sm rounded-2xl p-4">
          <div className="flex items-center justify-between mb-3">
            <span className="text-white/80 text-sm">本月总预算</span>
            <span className="text-xl font-bold">¥{formatMoney(totalBudget)}</span>
          </div>
          <div className="h-2 bg-white/20 rounded-full overflow-hidden mb-3">
            <div
              className="h-full bg-white rounded-full transition-all"
              style={{ width: `${totalBudget > 0 ? Math.min((totalSpent / totalBudget) * 100, 100) : 0}%` }}
            />
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-white/80">已花: ¥{formatMoney(totalSpent)}</span>
            <span className={totalRemaining >= 0 ? 'text-green-200' : 'text-red-200'}>
              {totalRemaining >= 0 ? '剩余' : '超支'}: ¥{formatMoney(Math.abs(totalRemaining))}
            </span>
          </div>
        </div>
      </div>

      <div className="px-5 -mt-8 relative z-10">
        <button
          onClick={handleOpenAdd}
          className="w-full cute-card p-4 flex items-center justify-center gap-2 text-primary-500 font-medium mb-4 active:scale-[0.98] transition-transform"
        >
          <Plus size={20} />
          添加预算
        </button>

        {monthBudgets.length === 0 ? (
          <div className="cute-card p-10 text-center">
            <div className="w-16 h-16 mx-auto mb-3 bg-cute-mint/30 rounded-full flex items-center justify-center">
              <Target size={28} className="text-teal-500" />
            </div>
            <p className="text-gray-500 text-sm mb-4">还没有设置预算~</p>
            <p className="text-gray-400 text-xs">设置预算帮助你更好地控制消费</p>
          </div>
        ) : (
          <div className="space-y-3">
            {monthBudgets.map((budget) => {
              const category = expenseCategories.find(c => c.id === budget.categoryId);
              const spent = getCategoryExpense(budget.categoryId, currentMonth);
              const remaining = budget.amount - spent;
              const percentage = budget.amount > 0 ? (spent / budget.amount) * 100 : 0;
              const isOverBudget = percentage > 100;
              const isWarning = percentage > 80 && !isOverBudget;

              return (
                <div key={budget.id} className="cute-card p-4">
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-3">
                      <div
                        className="w-10 h-10 rounded-xl flex items-center justify-center"
                        style={{ backgroundColor: (category?.color || '#ccc') + '20' }}
                      >
                        <DynamicIcon
                          name={category?.icon || 'HelpCircle'}
                          size={20}
                          color={category?.color}
                        />
                      </div>
                      <div>
                        <p className="font-semibold text-gray-800">{category?.name || '其他'}</p>
                        <p className="text-xs text-gray-400">预算 ¥{formatMoney(budget.amount)}</p>
                      </div>
                    </div>
                    {isOverBudget && (
                      <div className="flex items-center gap-1 text-red-500 text-xs">
                        <AlertTriangle size={14} />
                        已超支
                      </div>
                    )}
                    {isWarning && (
                      <div className="flex items-center gap-1 text-yellow-500 text-xs">
                        <AlertTriangle size={14} />
                        快超了
                      </div>
                    )}
                  </div>
                  
                  <div className="h-2.5 bg-gray-100 rounded-full overflow-hidden mb-2">
                    <div
                      className={`h-full rounded-full transition-all duration-500 ${
                        isOverBudget ? 'bg-red-500' : isWarning ? 'bg-yellow-500' : 'bg-green-500'
                      }`}
                      style={{ width: `${Math.min(percentage, 100)}%` }}
                    />
                  </div>
                  
                  <div className="flex items-center justify-between text-xs">
                    <span className="text-gray-500">
                      已花 ¥{formatMoney(spent)} ({percentage.toFixed(0)}%)
                    </span>
                    <span className={remaining >= 0 ? 'text-green-500' : 'text-red-500'}>
                      {remaining >= 0 ? '剩' : '超'} ¥{formatMoney(Math.abs(remaining))}
                    </span>
                  </div>

                  <div className="mt-3 pt-3 border-t border-gray-100 flex gap-2">
                    <button
                      onClick={() => handleOpenEdit(budget)}
                      className="flex-1 py-2 bg-gray-50 rounded-xl text-sm font-medium text-gray-600 active:bg-gray-100"
                    >
                      编辑
                    </button>
                    <button
                      onClick={() => handleDelete(budget.id)}
                      className="flex-1 py-2 bg-red-50 rounded-xl text-sm font-medium text-red-500 active:bg-red-100"
                    >
                      删除
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
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
                {editingBudget ? '编辑预算' : '添加预算'}
              </h3>
              <button
                onClick={handleSave}
                disabled={!selectedCategoryId || !amount || parseFloat(amount) <= 0}
                className={`font-medium ${
                  selectedCategoryId && amount && parseFloat(amount) > 0
                    ? 'text-primary-500'
                    : 'text-gray-300'
                }`}
              >
                保存
              </button>
            </div>

            <div className="space-y-5">
              <div>
                <label className="text-sm text-gray-500 mb-2 block">选择分类</label>
                <div className="grid grid-cols-4 gap-2 max-h-48 overflow-y-auto">
                  {availableCategories.map((category) => (
                    <button
                      key={category.id}
                      onClick={() => setSelectedCategoryId(category.id)}
                      className={`flex flex-col items-center gap-1 p-2 rounded-xl transition-all ${
                        selectedCategoryId === category.id
                          ? 'bg-primary-50 ring-2 ring-primary-400'
                          : 'bg-gray-50 active:bg-gray-100'
                      }`}
                    >
                      <div
                        className="w-9 h-9 rounded-lg flex items-center justify-center"
                        style={{ backgroundColor: category.color + '20' }}
                      >
                        <DynamicIcon name={category.icon} size={18} color={category.color} />
                      </div>
                      <span className="text-xs text-gray-600">{category.name}</span>
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-500 mb-2 block">预算金额</label>
                <div className="relative">
                  <span className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 text-lg">¥</span>
                  <input
                    type="number"
                    value={amount}
                    onChange={e => setAmount(e.target.value)}
                    placeholder="0.00"
                    className="w-full cute-input pl-8 text-lg font-semibold"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
