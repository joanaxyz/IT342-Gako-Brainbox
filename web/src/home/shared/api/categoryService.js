import { apiCall } from '../../../common/api/httpClient';

export const categoryAPI = {
  getAllCategories: () =>
    apiCall('/categories', 'GET'),

  getCategory: (categoryId) =>
    apiCall(`/categories/${categoryId}`, 'GET'),

  createCategory: (name) =>
    apiCall('/categories', 'POST', { name }),

  updateCategory: (categoryId, name) =>
    apiCall(`/categories/${categoryId}`, 'PUT', { name }),

  deleteCategory: (categoryId, options = {}) =>
    apiCall(`/categories/${categoryId}`, 'DELETE', options),
};
