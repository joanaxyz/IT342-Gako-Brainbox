import {
  useCallback,
  useMemo,
  useState,
} from 'react';
import SettingsModal from '../../home/shared/components/SettingsModal';
import { SettingsModalContext } from './SettingsModalContextValue';

export const SettingsModalProvider = ({ children }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [initialTab, setInitialTab] = useState(undefined);
  const [modalInstance, setModalInstance] = useState(0);

  const openSettings = useCallback((tab) => {
    setInitialTab(tab);
    setModalInstance((instance) => instance + 1);
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
      <SettingsModal
        key={modalInstance}
        isOpen={isOpen}
        onClose={closeSettings}
        initialTab={initialTab}
      />
    </SettingsModalContext.Provider>
  );
};
