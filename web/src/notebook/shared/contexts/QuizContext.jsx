import { useCallback, useMemo } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { quizAPI } from '../../../common/utils/api';
import { QuizContext } from './QuizContextValue';
import { useLoading } from '../../../common/hooks/hooks';
import { useAuth } from '../../../auth/shared/hooks/useAuth';
import { unwrapApiResponse, toApiResponse } from '../../../common/query/apiQuery';
import { queryKeys } from '../../../common/query/queryKeys';
import { broadcastResourceInvalidation } from '../../../common/query/resourceInvalidation';

const getQuizzesData = () => unwrapApiResponse(() => quizAPI.getQuizzes());
const EMPTY_QUIZZES = [];

export const QuizProvider = ({ children }) => {
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

  const quizzesQuery = useQuery({
    queryKey: queryKeys.quizzes.all,
    queryFn: getQuizzesData,
    enabled: isAuthenticated,
  });

  const quizzes = quizzesQuery.data ?? EMPTY_QUIZZES;

  const fetchQuizzes = useCallback((showSpinner = true, forceRefresh = false) => withLoading(
    async () => {
      if (forceRefresh) {
        await queryClient.invalidateQueries({ queryKey: queryKeys.quizzes.all });
      }

      return toApiResponse(() => queryClient.fetchQuery({
        queryKey: queryKeys.quizzes.all,
        queryFn: getQuizzesData,
      }));
    },
    showSpinner
  ), [queryClient, withLoading]);

  const upsertQuiz = useCallback((quiz) => {
    queryClient.setQueryData(queryKeys.quizzes.all, (currentQuizzes = []) => {
      const filteredQuizzes = currentQuizzes.filter((currentQuiz) => currentQuiz.uuid !== quiz.uuid);
      return [quiz, ...filteredQuizzes];
    });
  }, [queryClient]);

  const createQuiz = useCallback((quiz, showSpinner = true) => withLoading(
    async () => {
      const response = await quizAPI.createQuiz(quiz);
      if (!response.success) {
        return response;
      }

      upsertQuiz(response.data);
      broadcastResourceInvalidation(['quizzes']);
      return response;
    },
    showSpinner
  ), [upsertQuiz, withLoading]);

  const updateQuiz = useCallback((uuid, quiz, showSpinner = true) => withLoading(
    async () => {
      const response = await quizAPI.updateQuiz(uuid, quiz);
      if (!response.success) {
        return response;
      }

      upsertQuiz(response.data);
      broadcastResourceInvalidation(['quizzes']);
      return response;
    },
    showSpinner
  ), [upsertQuiz, withLoading]);

  const deleteQuiz = useCallback((uuid, showSpinner = true) => withLoading(
    async () => {
      const response = await quizAPI.deleteQuiz(uuid);
      if (!response.success) {
        return response;
      }

      queryClient.setQueryData(queryKeys.quizzes.all, (currentQuizzes = []) => (
        currentQuizzes.filter((quiz) => quiz.uuid !== uuid)
      ));
      broadcastResourceInvalidation(['quizzes']);
      return response;
    },
    showSpinner
  ), [queryClient, withLoading]);

  const recordAttempt = useCallback((uuid, score, showSpinner = false) => withLoading(
    async () => {
      const response = await quizAPI.recordAttempt(uuid, score);
      if (!response.success) {
        return response;
      }

      upsertQuiz(response.data);
      broadcastResourceInvalidation(['quizzes']);
      return response;
    },
    showSpinner
  ), [upsertQuiz, withLoading]);

  const value = useMemo(() => ({
    quizzes,
    quizzesLoading: quizzes.length === 0 && (quizzesQuery.isLoading || quizzesQuery.isFetching),
    fetchQuizzes,
    createQuiz,
    updateQuiz,
    deleteQuiz,
    recordAttempt,
  }), [
    quizzes,
    quizzesQuery.isFetching,
    quizzesQuery.isLoading,
    fetchQuizzes,
    createQuiz,
    updateQuiz,
    deleteQuiz,
    recordAttempt,
  ]);

  return (
    <QuizContext.Provider value={value}>
      {children}
    </QuizContext.Provider>
  );
};
