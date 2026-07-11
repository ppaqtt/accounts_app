import * as LucideIcons from 'lucide-react';

const iconMap = LucideIcons as unknown as Record<string, React.ComponentType<{ size?: number; color?: string; className?: string }>>;

interface DynamicIconProps {
  name: string;
  size?: number;
  color?: string;
  className?: string;
}

export const DynamicIcon = ({ name, size = 20, color, className }: DynamicIconProps) => {
  const IconComponent = iconMap[name];
  if (!IconComponent) {
    return <LucideIcons.HelpCircle size={size} color={color} className={className} />;
  }
  return <IconComponent size={size} color={color} className={className} />;
};
