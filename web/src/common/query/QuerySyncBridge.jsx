import { useEffect } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { invalidateResource, subscribeToResourceInvalidation } from './resourceInvalidation';

export const QuerySyncBridge = () => {
  const queryClient = useQueryClient();

  useEffect(() => subscribeToResourceInvalidation((resources, payload) => {
    resources.forEach((resource) => {
      invalidateResource(queryClient, resource, payload);
    });
  }), [queryClient]);

  return null;
};
