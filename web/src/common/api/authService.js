import { apiCall } from './httpClient';

export const authAPI = {
  register: (username, email, password) =>
    apiCall('/auth/register', 'POST', { username, email, password }),

  login: (username, password) =>
    apiCall('/auth/login', 'POST', { username, password }),

  forgotPassword: (email) =>
    apiCall('/auth/forgot-password', 'POST', { email }),

  verifyCode: (email, code) =>
    apiCall('/auth/verify-code', 'POST', { email, code }),

  resetPassword: (token, newPassword) =>
    apiCall('/auth/reset-password', 'POST', { token, newPassword }),

  logout: (refreshToken) =>
    apiCall('/auth/logout', 'POST', { refreshToken }),

  refreshToken: (refreshToken) =>
    apiCall(`/auth/refresh-token?refreshToken=${refreshToken}`, 'POST'),

  getMe: () =>
    apiCall('/user/me', 'GET'),

  updateProfile: (data) =>
    apiCall('/user/me', 'PUT', data),

  changePassword: (data) =>
    apiCall('/user/me/change-password', 'POST', data),

  googleLogin: (accessToken) =>
    apiCall('/auth/google', 'POST', { accessToken }),
};
