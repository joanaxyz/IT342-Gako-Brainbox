import AppProviders from './AppProviders';
import { NotificationContainer } from '../common/components/Notification';
import LoadingOverlay from '../common/components/LoadingOverlay';
import { useLoading } from '../common/hooks/hooks';

export const AppViewport = ({ children }) => {
  const { active: isLoading } = useLoading();

  return (
    <>
      <LoadingOverlay isActive={isLoading} />
      <NotificationContainer />
      {children}
    </>
  );
};

const AppShell = ({ children }) => (
  <AppProviders>
    <AppViewport>
      {children}
    </AppViewport>
  </AppProviders>
);

export default AppShell;
