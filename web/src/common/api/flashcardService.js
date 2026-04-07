import { apiCall } from './httpClient';

export const flashcardAPI = {
  getFlashcards: () =>
    apiCall('/flashcards', 'GET'),

  getFlashcard: (uuid) =>
    apiCall(`/flashcards/${uuid}`, 'GET'),

  createFlashcard: (flashcard) =>
    apiCall('/flashcards', 'POST', flashcard),

  updateFlashcard: (uuid, flashcard) =>
    apiCall(`/flashcards/${uuid}`, 'PUT', flashcard),

  deleteFlashcard: (uuid) =>
    apiCall(`/flashcards/${uuid}`, 'DELETE'),

  recordAttempt: (uuid, mastery) =>
    apiCall(`/flashcards/${uuid}/attempts`, 'POST', { mastery }),
};
