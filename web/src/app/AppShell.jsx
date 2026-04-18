import AppProviders from './AppProviders';
import { NotificationContainer } from '../common/components/Notification';
import LoadingOverlay from '../common/components/LoadingOverlay';
import { useLoading } from '../common/hooks/hooks';

export const AppViewport = ({ children, showGlobalLoadingOverlay = true }) => {
  const { active: isLoading } = useLoading();

  return (
    <>
      {showGlobalLoadingOverlay && <LoadingOverlay isActive={isLoading} />}
      <NotificationContainer />
      {children}
    </>
  );
};

const AppShell = ({ children, showGlobalLoadingOverlay = true }) => (
  <AppProviders>
    <AppViewport showGlobalLoadingOverlay={showGlobalLoadingOverlay}>
      {children}
    </AppViewport>
  </AppProviders>
);

export default AppShell;
