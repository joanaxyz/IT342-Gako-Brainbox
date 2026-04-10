import { Outlet } from 'react-router-dom';
import AppShell from './app/AppShell';

function App() {
  return (
    <AppShell>
      <Outlet />
    </AppShell>
  );
}

export default App;
