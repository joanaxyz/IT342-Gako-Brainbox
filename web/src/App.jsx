import { Outlet } from 'react-router-dom';
import AppProviders from './app/AppProviders';
import { NotificationContainer } from './common/components/Notification';
import LoadingOverlay from './common/components/LoadingOverlay';
import { useLoading } from './common/hooks/hooks';

const AppContent = () => {
  const { active: isLoading } = useLoading();

  return (
    <>
      <LoadingOverlay isActive={isLoading} />
      <NotificationContainer />
      <Outlet />
    </>
  );
};

function App() {
  return (
    <AppProviders>
      <AppContent />
    </AppProviders>
  );
}

export default App;
