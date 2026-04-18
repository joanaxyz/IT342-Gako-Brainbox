import { StrictMode, useEffect } from 'react';
import { createRoot } from 'react-dom/client';
import {
  createMemoryRouter,
  createRoutesFromElements,
  Outlet,
  Route,
  RouterProvider,
} from 'react-router-dom';
import './index.css';
import AppShell from './app/AppShell';
import RouteFallback from './app/RouteFallback';
import { useAuth } from './auth/shared/hooks/useAuth';
import NoteEditor from './notebook/editor/NoteEditor';
import {
  clearHostSession,
  getEmbeddedNotebookId,
  isAndroidHost,
  reportHostError,
} from './app/host/brainBoxHost';

const EmbeddedRoot = () => <Outlet />;
const embeddedAndroidHost = isAndroidHost();
const EmbeddedRouteFallback = () => (embeddedAndroidHost ? null : <RouteFallback />);

const EmbeddedSessionGuard = () => {
  const { isAuthReady, isAuthenticated } = useAuth();

  useEffect(() => {
    if (!isAuthReady || isAuthenticated) {
      return;
    }

    reportHostError('Your session expired. Sign in again to continue.');
    clearHostSession();
  }, [isAuthReady, isAuthenticated]);

  if (!isAuthReady) {
    return <EmbeddedRouteFallback />;
  }

  if (!isAuthenticated) {
    return <EmbeddedRouteFallback />;
  }

  return <Outlet />;
};

const MissingNotebookScreen = () => {
  useEffect(() => {
    reportHostError('No notebook was provided for the embedded editor.');
  }, []);

  return <EmbeddedRouteFallback />;
};

const setupEmbeddedErrorReporting = () => {
  const handleWindowError = (event) => {
    const message = event?.error?.message || event?.message || 'Unexpected embedded editor error';
    reportHostError(message);
  };

  const handleUnhandledRejection = (event) => {
    const reason = event?.reason;
    const message = reason?.message || String(reason || 'Unhandled embedded editor promise rejection');
    reportHostError(message);
  };

  window.addEventListener('error', handleWindowError);
  window.addEventListener('unhandledrejection', handleUnhandledRejection);
};

setupEmbeddedErrorReporting();

const notebookId = getEmbeddedNotebookId();
const initialEntries = [notebookId ? `/notebook/${encodeURIComponent(notebookId)}` : '/missing'];

const embeddedRouter = createMemoryRouter(
  createRoutesFromElements(
    <Route
      element={(
        <AppShell showGlobalLoadingOverlay={!embeddedAndroidHost}>
          <EmbeddedRoot />
        </AppShell>
      )}
    >
      <Route element={<EmbeddedSessionGuard />}>
        <Route path="/notebook/:id" element={<NoteEditor />} />
      </Route>
      <Route path="/missing" element={<MissingNotebookScreen />} />
    </Route>
  ),
  { initialEntries }
);

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <RouterProvider router={embeddedRouter} />
  </StrictMode>
);
