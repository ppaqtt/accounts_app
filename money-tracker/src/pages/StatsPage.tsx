import { useState, useMemo } from 'react';
import { ChevronLeft, ChevronRight, TrendingUp, TrendingDown } from 'lucide-react';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, Tooltip } from 'recharts';
import { useAppStore } from '../store/useAppStore';
import { DynamicIcon } from '../components/DynamicIcon';
import { getCategoryById, expenseCategories, incomeCategories } from '../data/categories';
import { formatMoney, getMonthName } from '../utils/format';
import type { TransactionType } from '../types';

export const StatsPage = () => {
  const { transactions } = useAppStore();
  const [currentMonth, setCurrentMonth] = useState(() => {
    const now = new Date();
    return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
  });
  const [activeTab, setActiveTab] = useState<TransactionType>('expense');

  const monthTransactions = useMemo(() => {
    return transactions.filter(t => t.date.startsWith(currentMonth) && t.type === activeTab);
  }, [transactions, currentMonth, activeTab]);

  const categoryStats = useMemo(() => {
    const stats: Record<string, number> = {};
    monthTransactions.forEach(tx => {
      stats[tx.categoryId] = (stats[tx.categoryId] || 0) + tx.amount;
    });
    
    const categories = activeTab === 'expense' ? expenseCategories : incomeCategories;
    return categories
      .filter(c => stats[c.id] > 0)
      .map(c => ({
        ...c,
        amount: stats[c.id],
        percentage: 0,
      }))
      .sort((a, b) => b.amount - a.amount);
  }, [monthTransactions, activeTab]);

  const totalAmount = categoryStats.reduce((sum, c) => sum + c.amount, 0);

  const pieData = categoryStats.map(c => ({
    name: c.name,
    value: c.amount,
    color: c.color,
  }));

  const changeMonth = (delta: number) => {
    const [year, month] = currentMonth.split('-').map(Number);
    const date = new Date(year, month - 1 + delta, 1);
    setCurrentMonth(`${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`);
  };

  return (
    <div className="pb-24">
      <div className="bg-gradient-to-br from-cute-purple to-cute-pink px-5 pt-12 pb-8 rounded-b-[40px] text-white">
        <h2 className="text-xl font-bold mb-4">收支统计</h2>
        
        <div className="flex items-center justify-center gap-4 mb-4">
          <button
            onClick={() => changeMonth(-1)}
            className="w-8 h-8 flex items-center justify-center bg-white/20 rounded-full"
          >
            <ChevronLeft size={18} />
          </button>
          <span className="font-semibold text-lg">{getMonthName(currentMonth)}</span>
          <button
            onClick={() => changeMonth(1)}
            className="w-8 h-8 flex items-center justify-center bg-white/20 rounded-full"
          >
            <ChevronRight size={18} />
          </button>
        </div>

        <div className="flex justify-center gap-2">
          <button
            onClick={() => setActiveTab('expense')}
            className={`px-5 py-2 rounded-full text-sm font-medium transition-all ${
              activeTab === 'expense'
                ? 'bg-white text-pink-500'
                : 'bg-white/20 text-white'
            }`}
          >
            支出
          </button>
          <button
            onClick={() => setActiveTab('income')}
            className={`px-5 py-2 rounded-full text-sm font-medium transition-all ${
              activeTab === 'income'
                ? 'bg-white text-green-500'
                : 'bg-white/20 text-white'
            }`}
          >
            收入
          </button>
        </div>
      </div>

      <div className="px-5 -mt-4 relative z-10">
        <div className="cute-card p-4 mb-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-gray-500 text-sm">
              {activeTab === 'expense' ? '总支出' : '总收入'}
            </span>
            <span className={`text-2xl font-bold ${
              activeTab === 'expense' ? 'text-red-500' : 'text-green-500'
            }`}>
              ¥{formatMoney(totalAmount)}
            </span>
          </div>
          <p className="text-xs text-gray-400">共 {monthTransactions.length} 笔记录</p>
        </div>

        {categoryStats.length > 0 ? (
          <>
            <div className="cute-card p-4 mb-4">
              <h3 className="font-semibold text-gray-800 mb-3">分类占比</h3>
              <div className="h-52">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      innerRadius={50}
                      outerRadius={80}
                      paddingAngle={2}
                      dataKey="value"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip
                      formatter={(value: number) => `¥${formatMoney(value)}`}
                    />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>

            <div className="cute-card p-4">
              <h3 className="font-semibold text-gray-800 mb-3">分类排行</h3>
              <div className="space-y-3">
                {categoryStats.map((category, index) => {
                  const percentage = totalAmount > 0 ? (category.amount / totalAmount * 100).toFixed(1) : '0';
                  return (
                    <div key={category.id}>
                      <div className="flex items-center justify-between mb-1.5">
                        <div className="flex items-center gap-2">
                          <span className="text-xs text-gray-400 w-4">{index + 1}</span>
                          <div
                            className="w-8 h-8 rounded-lg flex items-center justify-center"
                            style={{ backgroundColor: category.color + '20' }}
                          >
                            <DynamicIcon name={category.icon} size={16} color={category.color} />
                          </div>
                          <span className="text-sm text-gray-700 font-medium">{category.name}</span>
                        </div>
                        <div className="text-right">
                          <span className="text-sm font-semibold text-gray-800">
                            ¥{formatMoney(category.amount)}
                          </span>
                          <span className="text-xs text-gray-400 ml-2">{percentage}%</span>
                        </div>
                      </div>
                      <div className="h-2 bg-gray-100 rounded-full overflow-hidden">
                        <div
                          className="h-full rounded-full transition-all duration-500"
                          style={{
                            width: `${percentage}%`,
                            backgroundColor: category.color,
                          }}
                        />
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          </>
        ) : (
          <div className="cute-card p-10 text-center">
            <div className="w-16 h-16 mx-auto mb-3 bg-gray-100 rounded-full flex items-center justify-center">
              {activeTab === 'expense' ? (
                <TrendingDown size={28} className="text-gray-400" />
              ) : (
                <TrendingUp size={28} className="text-gray-400" />
              )}
            </div>
            <p className="text-gray-500 text-sm">
              本月还没有{activeTab === 'expense' ? '支出' : '收入'}记录
            </p>
          </div>
        )}
      </div>
    </div>
  );
};
