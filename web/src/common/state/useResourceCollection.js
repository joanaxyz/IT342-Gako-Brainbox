import { useCallback, useEffect, useRef, useState } from 'react';
import { useAuth } from '../../auth/shared/hooks/useAuth';
import { useLoading } from '../hooks/hooks';

const defaultGetItemId = (item) => item?.uuid ?? item?.id;

export const useResourceCollection = ({
  fetchCollection,
  initialItems = [],
  getItemId = defaultGetItemId,
  useGlobalLoading = false,
  resetOnUserChange = false,
}) => {
  const [items, setItemsState] = useState(initialItems);
  const [loading, setLoading] = useState(false);
  const itemsRef = useRef(initialItems);
  const fetchedRef = useRef(false);
  const inFlightFetchRef = useRef(null);
  const previousUserIdRef = useRef(null);
  const { isAuthenticated, user } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();

  useEffect(() => {
    itemsRef.current = items;
  }, [items]);

  const setItems = useCallback((updater, { markFetched = true } = {}) => {
    setItemsState((currentItems) => {
      const nextItems = typeof updater === 'function' ? updater(currentItems) : updater;
      itemsRef.current = nextItems;
      return nextItems;
    });

    if (markFetched) {
      fetchedRef.current = true;
    }
  }, []);

  const clearCache = useCallback(() => {
    fetchedRef.current = false;
    inFlightFetchRef.current = null;
    itemsRef.current = initialItems;
    setLoading(false);
    setItemsState(initialItems);
  }, [initialItems]);

  useEffect(() => {
    if (!isAuthenticated) {
      clearCache();
      previousUserIdRef.current = null;
    }
  }, [clearCache, isAuthenticated]);

  useEffect(() => {
    if (!resetOnUserChange) {
      return;
    }

    const nextUserId = user?.id ?? null;

    if (previousUserIdRef.current !== null && nextUserId !== previousUserIdRef.current) {
      clearCache();
    }

    previousUserIdRef.current = nextUserId;
  }, [clearCache, resetOnUserChange, user?.id]);

  const runRequest = useCallback(async (request, {
    showSpinner = true,
    trackLoading = false,
  } = {}) => {
    if (trackLoading) {
      setLoading(true);
    }

    if (useGlobalLoading && showSpinner) {
      showLoading();
    }

    try {
      return await request();
    } finally {
      if (useGlobalLoading && showSpinner) {
        hideLoading();
      }

      if (trackLoading) {
        setLoading(false);
      }
    }
  }, [hideLoading, showLoading, useGlobalLoading]);

  const fetchItems = useCallback(async (showSpinner = true, forceRefresh = false) => {
    if (!forceRefresh && fetchedRef.current) {
      return { success: true, data: itemsRef.current };
    }

    if (!forceRefresh && inFlightFetchRef.current) {
      return inFlightFetchRef.current;
    }

    const requestPromise = runRequest(fetchCollection, {
      showSpinner,
      trackLoading: true,
    }).then((response) => {
      if (response.success) {
        setItems(response.data || initialItems);
      }

      return response;
    }).finally(() => {
      if (inFlightFetchRef.current === requestPromise) {
        inFlightFetchRef.current = null;
      }
    });

    if (!forceRefresh) {
      inFlightFetchRef.current = requestPromise;
    }

    return requestPromise;
  }, [fetchCollection, initialItems, runRequest, setItems]);

  const prependItem = useCallback((item) => {
    setItems((currentItems) => [item, ...currentItems]);
  }, [setItems]);

  const appendItem = useCallback((item) => {
    setItems((currentItems) => [...currentItems, item]);
  }, [setItems]);

  const replaceItem = useCallback((item, explicitId = null) => {
    const itemId = explicitId ?? getItemId(item);

    setItems((currentItems) => currentItems.map((currentItem) => (
      getItemId(currentItem) === itemId ? item : currentItem
    )));
  }, [getItemId, setItems]);

  const mergeItem = useCallback((item, explicitId = null) => {
    const itemId = explicitId ?? getItemId(item);

    setItems((currentItems) => currentItems.map((currentItem) => (
      getItemId(currentItem) === itemId ? { ...currentItem, ...item } : currentItem
    )));
  }, [getItemId, setItems]);

  const removeItem = useCallback((itemId) => {
    setItems((currentItems) => currentItems.filter((currentItem) => getItemId(currentItem) !== itemId));
  }, [getItemId, setItems]);

  return {
    items,
    itemsRef,
    loading,
    fetchedRef,
    setItems,
    clearCache,
    runRequest,
    fetchItems,
    prependItem,
    appendItem,
    replaceItem,
    mergeItem,
    removeItem,
  };
};
