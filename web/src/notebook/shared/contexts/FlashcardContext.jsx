import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { flashcardAPI } from '../../../common/utils/api';
import { FlashcardContext } from './FlashcardContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const getFlashcardsData = () => unwrapApiResponse(() => flashcardAPI.getFlashcards());
const EMPTY_FLASHCARDS = [];

export const FlashcardProvider = ({ children }) => {
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

  const flashcardsQuery = useQuery({
    queryKey: queryKeys.flashcards.all,
    queryFn: getFlashcardsData,
    enabled: isAuthenticated,
  });

  const flashcards = flashcardsQuery.data ?? EMPTY_FLASHCARDS;

  const fetchFlashcards = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.flashcards.all });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.flashcards.all,
        queryFn: getFlashcardsData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const upsertFlashcard = useCallback((flashcard) => {
    queryClient.setQueryData(queryKeys.flashcards.all, (currentFlashcards = []) => {
      const filteredFlashcards = currentFlashcards.filter((currentFlashcard) => currentFlashcard.uuid !== flashcard.uuid);
      return [flashcard, ...filteredFlashcards];
    });
  }, [queryClient]);

  const createFlashcard = useCallback((flashcard, showSpinner = true) => withLoading(
    async () => {
      const response = await flashcardAPI.createFlashcard(flashcard);
      if (!response.success) {
        return response;
      }

      upsertFlashcard(response.data);
      broadcastResourceInvalidation(['flashcards']);
      return response;
    },
    showSpinner
  ), [upsertFlashcard, withLoading]);

  const updateFlashcard = useCallback((uuid, flashcard, showSpinner = true) => withLoading(
    async () => {
      const response = await flashcardAPI.updateFlashcard(uuid, flashcard);
      if (!response.success) {
        return response;
      }

      upsertFlashcard(response.data);
      broadcastResourceInvalidation(['flashcards']);
      return response;
    },
    showSpinner
  ), [upsertFlashcard, withLoading]);

  const deleteFlashcard = useCallback((uuid, showSpinner = true) => withLoading(
    async () => {
      const response = await flashcardAPI.deleteFlashcard(uuid);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.flashcards.all, (currentFlashcards = []) => (
        currentFlashcards.filter((flashcard) => flashcard.uuid !== uuid)
      ));
      broadcastResourceInvalidation(['flashcards']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const recordAttempt = useCallback((uuid, mastery, showSpinner = false) => withLoading(
    async () => {
      const response = await flashcardAPI.recordAttempt(uuid, mastery);
      if (!response.success) {
        return response;
      }

      upsertFlashcard(response.data);
      broadcastResourceInvalidation(['flashcards']);
      return response;
    },
    showSpinner
  ), [upsertFlashcard, withLoading]);

  const value = useMemo(() => ({
    flashcards,
    flashcardsLoading: flashcards.length === 0 && (flashcardsQuery.isLoading || flashcardsQuery.isFetching),
    fetchFlashcards,
    createFlashcard,
    updateFlashcard,
    deleteFlashcard,
    recordAttempt,
  }), [
    flashcards,
    flashcardsQuery.isFetching,
    flashcardsQuery.isLoading,
    fetchFlashcards,
    createFlashcard,
    updateFlashcard,
    deleteFlashcard,
    recordAttempt,
  ]);

  return (
    <FlashcardContext.Provider value={value}>
      {children}
    </FlashcardContext.Provider>
  );
};
