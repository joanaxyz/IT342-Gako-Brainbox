import { QueryClient } from '@tanstack/react-query';

const shouldRetryQuery = (failureCount, error) => {
  const status = error?.response?.status;

  if (status && status !== 0 && status < 500) {
    return false;
  }

  return failureCount < 2;
};

export const appQueryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      gcTime: 5 * 60_000,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      retry: shouldRetryQuery,
    },
    mutations: {
      retry: 0,
    },
  },
});
