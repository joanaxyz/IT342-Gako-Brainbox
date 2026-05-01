import { useCallback, useMemo, useRef } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { categoryAPI } from '../../../home/shared/api/categoryService';
import { CategoryContext } from './CategoryContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';
import {
  addCategory,
  applyCategoryDeleteToNotebooks,
  applyCategoryDeleteToPlaylists,
  captureQuerySnapshot,
  removeCategory,
  replaceCategory,
  restoreQuerySnapshot,
} from '../../../common/query/optimisticUpdates';

const getCategoriesData = () => unwrapApiResponse(() => categoryAPI.getAllCategories());
const EMPTY_CATEGORIES = [];

export const CategoryProvider = ({ children }) => {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();
  const temporaryCategoryIdRef = useRef(-1);

  const withLoading = useCallback(async (operation, showSpinner = true) => {
    if (showSpinner) {
      showLoading();
    }

    try {
      return await operation();
    } finally {
      if (showSpinner) {
        hideLoading();
      }
    }
  }, [hideLoading, showLoading]);

  const categoriesQuery = useQuery({
    queryKey: queryKeys.categories.all,
    queryFn: getCategoriesData,
    enabled: isAuthenticated,
  });

  const categories = categoriesQuery.data ?? EMPTY_CATEGORIES;

  const getAffectedNotebookUuids = useCallback((categoryId) => {
    const lists = [
      queryClient.getQueryData(queryKeys.notebooks.list) ?? [],
      queryClient.getQueryData(queryKeys.notebooks.recentEdited) ?? [],
      queryClient.getQueryData(queryKeys.notebooks.recentReviewed) ?? [],
    ];
    return [...new Set(lists.flatMap((items) => (
      items
        .filter((notebook) => notebook.categoryId === categoryId)
        .map((notebook) => notebook.uuid)
    )))];
  }, [queryClient]);

  const getCategoryMutationSnapshot = useCallback((categoryId) => {
    const affectedDetailKeys = getAffectedNotebookUuids(categoryId).map(queryKeys.notebooks.detail);
    return captureQuerySnapshot(queryClient, [
      queryKeys.categories.all,
      queryKeys.notebooks.list,
      queryKeys.notebooks.recentEdited,
      queryKeys.notebooks.recentReviewed,
      queryKeys.playlists.all,
      ...affectedDetailKeys,
    ]);
  }, [getAffectedNotebookUuids, queryClient]);

  const applyOptimisticCategoryDelete = useCallback((categoryId, options = {}) => {
    const affectedNotebookUuids = getAffectedNotebookUuids(categoryId);

    queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
      removeCategory(currentCategories, categoryId)
    ));
    [
      queryKeys.notebooks.list,
      queryKeys.notebooks.recentEdited,
      queryKeys.notebooks.recentReviewed,
    ].forEach((queryKey) => {
      queryClient.setQueryData(queryKey, (currentNotebooks = []) => (
        applyCategoryDeleteToNotebooks(currentNotebooks, categoryId, options)
      ));
    });
    affectedNotebookUuids.forEach((uuid) => {
      queryClient.setQueryData(queryKeys.notebooks.detail(uuid), (currentNotebook) => {
        if (!currentNotebook?.uuid) {
          return currentNotebook;
        }

        const nextNotebooks = applyCategoryDeleteToNotebooks([currentNotebook], categoryId, options);
        return nextNotebooks[0];
      });
    });
    queryClient.setQueryData(queryKeys.playlists.all, (currentPlaylists = []) => (
      applyCategoryDeleteToPlaylists(currentPlaylists, categoryId, options)
    ));
  }, [getAffectedNotebookUuids, queryClient]);

  const fetchCategories = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.categories.all });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.categories.all,
        queryFn: getCategoriesData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const createCategory = useCallback((name, showSpinner = true) => withLoading(
    async () => {
      const temporaryCategory = {
        id: temporaryCategoryIdRef.current,
        name,
        optimistic: true,
      };
      temporaryCategoryIdRef.current -= 1;
      const snapshot = captureQuerySnapshot(queryClient, [queryKeys.categories.all]);
      queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
        addCategory(currentCategories, temporaryCategory)
      ));

      const response = await categoryAPI.createCategory(name);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
        replaceCategory(currentCategories, temporaryCategory.id, response.data)
      ));
      broadcastResourceInvalidation(['categories']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const deleteCategory = useCallback((categoryId, options = {}, showSpinner = true) => withLoading(
    async () => {
      const snapshot = getCategoryMutationSnapshot(categoryId);
      applyOptimisticCategoryDelete(categoryId, options);

      const response = await categoryAPI.deleteCategory(categoryId, options);
      if (!response.success) {
        restoreQuerySnapshot(queryClient, snapshot);
        return response;
      }

      void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
      broadcastResourceInvalidation(['categories', 'notebook-derived']);
      return response;
    },
    showSpinner
  ), [
    applyOptimisticCategoryDelete,
    getCategoryMutationSnapshot,
    queryClient,
    withLoading,
  ]);

  const setCategories = useCallback((updater) => {
    queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
      typeof updater === 'function' ? updater(currentCategories) : updater
    ));
  }, [queryClient]);

  const value = useMemo(() => ({
    categories,
    fetchCategories,
    createCategory,
    deleteCategory,
    setCategories,
  }), [categories, fetchCategories, createCategory, deleteCategory, setCategories]);

  return (
    <CategoryContext.Provider value={value}>
      {children}
    </CategoryContext.Provider>
  );
};
