import { useContext } from 'react';
import { SettingsModalContext } from '../contexts/SettingsModalContextValue';

export const useSettingsModal = () => {
  const ctx = useContext(SettingsModalContext);
  if (!ctx) {
    throw new Error('useSettingsModal must be used within SettingsModalProvider');
  }
  return ctx;
};
