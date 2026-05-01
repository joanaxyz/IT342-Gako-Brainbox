import { Suspense } from 'react';
import { Outlet } from 'react-router-dom';
import RouteFallback from './RouteFallback';

const SuspenseLayout = () => (
  <Suspense fallback={<RouteFallback />}>
    <Outlet />
  </Suspense>
);

export default SuspenseLayout;
