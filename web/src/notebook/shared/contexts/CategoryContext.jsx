import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { categoryAPI } from '../../../common/utils/api';
import { CategoryContext } from './CategoryContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const getCategoriesData = () => unwrapApiResponse(() => categoryAPI.getAllCategories());
const EMPTY_CATEGORIES = [];

export const CategoryProvider = ({ children }) => {
  const queryClient = useQueryClient();
  const { isAuthenticated } = useAuth();
  const { activate: showLoading, deactivate: hideLoading } = useLoading();

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
      const response = await categoryAPI.createCategory(name);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
        [...currentCategories, response.data].sort((leftCategory, rightCategory) => (
          leftCategory.name.localeCompare(rightCategory.name)
        ))
      ));
      broadcastResourceInvalidation(['categories']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const deleteCategory = useCallback((categoryId, options = {}, showSpinner = true) => withLoading(
    async () => {
      const response = await categoryAPI.deleteCategory(categoryId, options);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.categories.all, (currentCategories = []) => (
        currentCategories.filter((category) => category.id !== categoryId)
      ));
      void queryClient.invalidateQueries({ queryKey: queryKeys.notebooks.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.playlists.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
      void queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
      broadcastResourceInvalidation(['categories', 'notebook-derived']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

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
