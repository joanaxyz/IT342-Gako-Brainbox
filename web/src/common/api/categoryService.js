import { apiCall } from './httpClient';

export const categoryAPI = {
  getAllCategories: () =>
    apiCall('/categories', 'GET'),

  getCategory: (categoryId) =>
    apiCall(`/categories/${categoryId}`, 'GET'),

  createCategory: (name) =>
    apiCall('/categories', 'POST', { name }),

  deleteCategory: (categoryId, options = {}) =>
    apiCall(`/categories/${categoryId}`, 'DELETE', options),
};
