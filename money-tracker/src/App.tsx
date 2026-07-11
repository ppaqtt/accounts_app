import { useState, useEffect } from 'react';
import { BottomNav } from './components/BottomNav';
import { AddTransactionModal } from './components/AddTransactionModal';
import { HomePage } from './pages/HomePage';
import { StatsPage } from './pages/StatsPage';
import { BudgetPage } from './pages/BudgetPage';
import { AccountsPage } from './pages/AccountsPage';
import { useAppStore } from './store/useAppStore';
import { Loader2 } from 'lucide-react';

function App() {
  const [activeTab, setActiveTab] = useState('home');
  const [showAddModal, setShowAddModal] = useState(false);
  const { isLoading, isInitialized, initApp } = useAppStore();

  useEffect(() => {
    initApp();
  }, [initApp]);

  if (isLoading || !isInitialized) {
    return (
      <div className="h-screen w-full flex items-center justify-center bg-gradient-to-br from-primary-100 via-cute-pink/30 to-cute-mint/30">
        <div className="text-center">
          <div className="w-16 h-16 mx-auto mb-4 bg-gradient-to-br from-primary-400 to-primary-600 rounded-2xl flex items-center justify-center animate-pulse">
            <span className="text-white text-2xl font-bold">💰</span>
          </div>
          <div className="flex items-center justify-center gap-2 text-primary-500">
            <Loader2 size={18} className="animate-spin" />
            <span className="font-medium">加载中...</span>
          </div>
        </div>
      </div>
    );
  }

  const renderPage = () => {
    switch (activeTab) {
      case 'home':
        return <HomePage onAddClick={() => setShowAddModal(true)} />;
      case 'stats':
        return <StatsPage />;
      case 'budget':
        return <BudgetPage />;
      case 'accounts':
        return <AccountsPage />;
      default:
        return <HomePage onAddClick={() => setShowAddModal(true)} />;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-md mx-auto min-h-screen bg-gradient-to-b from-orange-50/50 to-white relative">
        {renderPage()}
        <BottomNav
          activeTab={activeTab}
          onTabChange={setActiveTab}
          onAddClick={() => setShowAddModal(true)}
        />
        <AddTransactionModal
          isOpen={showAddModal}
          onClose={() => setShowAddModal(false)}
        />
      </div>
    </div>
  );
}

export default App;
