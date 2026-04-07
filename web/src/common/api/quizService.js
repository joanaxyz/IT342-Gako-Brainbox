import { apiCall } from './httpClient';

export const quizAPI = {
  getQuizzes: () =>
    apiCall('/quizzes', 'GET'),

  getQuiz: (uuid) =>
    apiCall(`/quizzes/${uuid}`, 'GET'),

  createQuiz: (quiz) =>
    apiCall('/quizzes', 'POST', quiz),

  updateQuiz: (uuid, quiz) =>
    apiCall(`/quizzes/${uuid}`, 'PUT', quiz),

  deleteQuiz: (uuid) =>
    apiCall(`/quizzes/${uuid}`, 'DELETE'),

  recordAttempt: (uuid, score) =>
    apiCall(`/quizzes/${uuid}/attempts`, 'POST', { score }),
};
