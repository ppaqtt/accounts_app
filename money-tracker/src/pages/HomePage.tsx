import { useAppStore } from '../store/useAppStore';
import { formatMoney, getCurrentMonth, getMonthName, formatDateCN } from '../utils/format';
import { DynamicIcon } from '../components/DynamicIcon';
import { getCategoryById } from '../data/categories';
import { ArrowUpRight, ArrowDownRight, Bell, Sparkles } from 'lucide-react';

interface HomePageProps {
  onAddClick: () => void;
}

export const HomePage = ({ onAddClick }: HomePageProps) => {
  const { accounts, transactions, getTotalBalance, getMonthlySummary } = useAppStore();
  const currentMonth = getCurrentMonth();
  const { income, expense } = getMonthlySummary(currentMonth);
  const totalBalance = getTotalBalance();

  const recentTransactions = transactions.slice(0, 10);

  const groupedTransactions = recentTransactions.reduce((groups, tx) => {
    const date = tx.date;
    if (!groups[date]) {
      groups[date] = [];
    }
    groups[date].push(tx);
    return groups;
  }, {} as Record<string, typeof transactions>);

  return (
    <div className="pb-24">
      <div className="bg-gradient-to-br from-primary-400 via-primary-500 to-primary-600 text-white px-5 pt-12 pb-20 rounded-b-[40px] relative overflow-hidden">
        <div className="absolute -top-20 -right-20 w-60 h-60 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-10 -left-10 w-40 h-40 bg-white/10 rounded-full blur-2xl" />
        
        <div className="relative z-10">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2">
              <div className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm">
                <Sparkles size={20} />
              </div>
              <div>
                <p className="text-white/80 text-xs">你好呀~</p>
                <p className="font-semibold">小萌记账</p>
              </div>
            </div>
            <button className="w-10 h-10 bg-white/20 rounded-xl flex items-center justify-center backdrop-blur-sm active:scale-95 transition-transform">
              <Bell size={20} />
            </button>
          </div>

          <div className="text-center mb-6">
            <p className="text-white/80 text-sm mb-1">总资产 (元)</p>
            <p className="text-4xl font-bold tracking-tight">
              {formatMoney(totalBalance)}
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="bg-white/20 backdrop-blur-sm rounded-2xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-7 h-7 bg-green-400/30 rounded-lg flex items-center justify-center">
                  <ArrowDownRight size={16} className="text-green-100" />
                </div>
                <span className="text-white/80 text-sm">本月收入</span>
              </div>
              <p className="text-xl font-bold">+{formatMoney(income)}</p>
            </div>
            <div className="bg-white/20 backdrop-blur-sm rounded-2xl p-4">
              <div className="flex items-center gap-2 mb-2">
                <div className="w-7 h-7 bg-red-400/30 rounded-lg flex items-center justify-center">
                  <ArrowUpRight size={16} className="text-red-100" />
                </div>
                <span className="text-white/80 text-sm">本月支出</span>
              </div>
              <p className="text-xl font-bold">-{formatMoney(expense)}</p>
            </div>
          </div>
        </div>
      </div>

      <div className="px-5 -mt-12 relative z-20">
        <div className="cute-card p-4 mb-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-semibold text-gray-800">我的账户</h3>
            <span className="text-xs text-gray-400">共 {accounts.length} 个</span>
          </div>
          <div className="flex gap-3 overflow-x-auto scrollbar-hide -mx-1 px-1">
            {accounts.map((account) => (
              <div
                key={account.id}
                className="flex-shrink-0 w-28 p-3 rounded-2xl bg-gradient-to-br from-gray-50 to-gray-100 border border-gray-100"
              >
                <div
                  className="w-9 h-9 rounded-xl flex items-center justify-center mb-2"
                  style={{ backgroundColor: account.color + '20' }}
                >
                  <DynamicIcon name={account.icon} size={18} color={account.color} />
                </div>
                <p className="text-xs text-gray-500 truncate">{account.name}</p>
                <p className="font-semibold text-gray-800 text-sm truncate">
                  ¥{formatMoney(account.balance)}
                </p>
              </div>
            ))}
          </div>
        </div>

        <div className="mb-5">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-semibold text-gray-800">
              {getMonthName(currentMonth)}账单
            </h3>
            <span className="text-xs text-primary-500 font-medium">查看全部 →</span>
          </div>

          {Object.keys(groupedTransactions).length === 0 ? (
            <div className="cute-card p-10 text-center">
              <div className="w-16 h-16 mx-auto mb-3 bg-cute-yellow/30 rounded-full flex items-center justify-center">
                <Sparkles size={28} className="text-yellow-500" />
              </div>
              <p className="text-gray-500 text-sm mb-4">还没有记账记录~</p>
              <button
                onClick={onAddClick}
                className="px-6 py-2 bg-primary-500 text-white rounded-full text-sm font-medium active:scale-95 transition-transform"
              >
                记一笔
              </button>
            </div>
          ) : (
            <div className="space-y-4">
              {Object.entries(groupedTransactions).map(([date, txs]) => {
                const dayIncome = txs.filter(t => t.type === 'income').reduce((s, t) => s + t.amount, 0);
                const dayExpense = txs.filter(t => t.type === 'expense').reduce((s, t) => s + t.amount, 0);
                
                return (
                  <div key={date} className="cute-card overflow-hidden">
                    <div className="flex items-center justify-between px-4 py-2 bg-gray-50/80">
                      <span className="text-sm font-medium text-gray-700">
                        {formatDateCN(date)}
                      </span>
                      <div className="flex gap-3 text-xs">
                        {dayIncome > 0 && (
                          <span className="text-green-500">收入 +{formatMoney(dayIncome)}</span>
                        )}
                        {dayExpense > 0 && (
                          <span className="text-red-500">支出 -{formatMoney(dayExpense)}</span>
                        )}
                      </div>
                    </div>
                    <div className="divide-y divide-gray-50">
                      {txs.map((tx) => {
                        const category = getCategoryById(tx.categoryId);
                        return (
                          <div
                            key={tx.id}
                            className="flex items-center justify-between px-4 py-3 active:bg-gray-50 transition-colors"
                          >
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
                                <p className="font-medium text-gray-800 text-sm">
                                  {category?.name || '其他'}
                                </p>
                                {tx.note && (
                                  <p className="text-xs text-gray-400 truncate max-w-[150px]">
                                    {tx.note}
                                  </p>
                                )}
                              </div>
                            </div>
                            <span
                              className={`font-semibold ${
                                tx.type === 'income' ? 'text-green-500' : 'text-gray-800'
                              }`}
                            >
                              {tx.type === 'income' ? '+' : '-'}
                              {formatMoney(tx.amount)}
                            </span>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
