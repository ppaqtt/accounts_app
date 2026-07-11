import { Home, PlusCircle, PieChart, Wallet, Target } from 'lucide-react';

interface BottomNavProps {
  activeTab: string;
  onTabChange: (tab: string) => void;
  onAddClick: () => void;
}

export const BottomNav = ({ activeTab, onTabChange, onAddClick }: BottomNavProps) => {
  const tabs = [
    { id: 'home', icon: Home, label: '首页' },
    { id: 'stats', icon: PieChart, label: '统计' },
    { id: 'budget', icon: Target, label: '预算' },
    { id: 'accounts', icon: Wallet, label: '账户' },
  ];

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white/90 backdrop-blur-lg border-t border-gray-100 safe-area-bottom z-50">
      <div className="flex items-center justify-around h-16 max-w-md mx-auto relative">
        {tabs.slice(0, 2).map((tab) => (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={`flex flex-col items-center justify-center w-16 h-full transition-all ${
              activeTab === tab.id
                ? 'text-primary-500'
                : 'text-gray-400 hover:text-gray-600'
            }`}
          >
            <tab.icon size={22} strokeWidth={activeTab === tab.id ? 2.5 : 2} />
            <span className="text-xs mt-1 font-medium">{tab.label}</span>
          </button>
        ))}

        <button
          onClick={onAddClick}
          className="absolute left-1/2 -translate-x-1/2 -top-6 w-14 h-14 bg-gradient-to-br from-primary-400 to-primary-600 rounded-full flex items-center justify-center shadow-lg shadow-primary-300/50 active:scale-95 transition-transform"
        >
          <PlusCircle size={32} className="text-white" strokeWidth={2.5} />
        </button>

        {tabs.slice(2).map((tab) => (
          <button
            key={tab.id}
            onClick={() => onTabChange(tab.id)}
            className={`flex flex-col items-center justify-center w-16 h-full transition-all ${
              activeTab === tab.id
                ? 'text-primary-500'
                : 'text-gray-400 hover:text-gray-600'
            }`}
          >
            <tab.icon size={22} strokeWidth={activeTab === tab.id ? 2.5 : 2} />
            <span className="text-xs mt-1 font-medium">{tab.label}</span>
          </button>
        ))}
      </div>
    </div>
  );
};
