import { isRouteErrorResponse, useRouteError } from 'react-router-dom';

const getRouteErrorMessage = (error) => {
  if (isRouteErrorResponse(error)) {
    return error.statusText || error.data?.message || `Request failed with ${error.status}`;
  }

  const message = error?.message || String(error || '');
  if (/dynamically imported module|Failed to fetch dynamically imported module/i.test(message)) {
    return 'A page module failed to load. Refresh the app to reconnect to the local dev server.';
  }

  return message || 'Something went wrong while loading this page.';
};

const RouteErrorFallback = () => {
  const error = useRouteError();
  const message = getRouteErrorMessage(error);

  return (
    <main className="route-error-shell" role="alert">
      <section className="route-error-panel">
        <p className="route-error-eyebrow">Page load failed</p>
        <h1>Reload BrainBox</h1>
        <p>{message}</p>
        <button type="button" className="btn btn-primary" onClick={() => window.location.reload()}>
          Reload
        </button>
      </section>
    </main>
  );
};

export default RouteErrorFallback;
