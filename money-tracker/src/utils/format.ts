import { format, parseISO, startOfMonth, endOfMonth, eachDayOfInterval } from 'date-fns';
import { zhCN } from 'date-fns/locale';

export const formatMoney = (amount: number, showSign = false): string => {
  const formatted = Math.abs(amount).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  if (showSign && amount !== 0) {
    return amount > 0 ? `+${formatted}` : `-${formatted}`;
  }
  return formatted;
};

export const formatDate = (dateStr: string, pattern = 'yyyy-MM-dd'): string => {
  try {
    return format(parseISO(dateStr), pattern, { locale: zhCN });
  } catch {
    return dateStr;
  }
};

export const formatDateCN = (dateStr: string): string => {
  try {
    return format(parseISO(dateStr), 'M月d日 EEEE', { locale: zhCN });
  } catch {
    return dateStr;
  }
};

export const getCurrentMonth = (): string => {
  return format(new Date(), 'yyyy-MM');
};

export const getMonthDays = (yearMonth: string) => {
  const [year, month] = yearMonth.split('-').map(Number);
  const start = startOfMonth(new Date(year, month - 1));
  const end = endOfMonth(new Date(year, month - 1));
  return eachDayOfInterval({ start, end });
};

export const isToday = (dateStr: string): boolean => {
  const today = format(new Date(), 'yyyy-MM-dd');
  return dateStr === today;
};

export const getMonthName = (yearMonth: string): string => {
  const [year, month] = yearMonth.split('-').map(Number);
  return format(new Date(year, month - 1), 'yyyy年M月', { locale: zhCN });
};

export const generateId = (): string => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2);
};
