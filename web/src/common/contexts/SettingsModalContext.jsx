import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from 'react';
import SettingsModal from '../../home/shared/components/SettingsModal';

const SettingsModalContext = createContext(null);

export const SettingsModalProvider = ({ children }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [initialTab, setInitialTab] = useState(undefined);

  const openSettings = useCallback((tab) => {
    setInitialTab(tab);
    setIsOpen(true);
  }, []);

  const closeSettings = useCallback(() => {
    setIsOpen(false);
  }, []);

  const value = useMemo(
    () => ({ openSettings, closeSettings }),
    [openSettings, closeSettings],
  );

  return (
    <SettingsModalContext.Provider value={value}>
      {children}
      <SettingsModal isOpen={isOpen} onClose={closeSettings} initialTab={initialTab} />
    </SettingsModalContext.Provider>
  );
};

export const useSettingsModal = () => {
  const ctx = useContext(SettingsModalContext);
  if (!ctx) {
    throw new Error('useSettingsModal must be used within SettingsModalProvider');
  }
  return ctx;
};
