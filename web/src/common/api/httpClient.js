import { deleteCookie, getCookie, setCookie } from './cookies';

let isRefreshing = false;
let refreshQueue = [];

const processQueue = (error, token = null) => {
  refreshQueue.forEach((request) => {
    if (error) {
      request.reject(error);
      return;
    }

    request.resolve(token);
  });

  refreshQueue = [];
};

const parseApiResponse = (httpStatus, data) => {
  if (data && typeof data === 'object' && 'success' in data) {
    return {
      success: data.success,
      status: httpStatus,
      data: data.data ?? null,
      error: data.error ?? null,
      timestamp: data.timestamp ?? null,
      message: data.error?.message ?? null,
    };
  }

  return {
    success: httpStatus >= 200 && httpStatus < 300,
    status: httpStatus,
    data: typeof data === 'object' ? data : null,
    error: null,
    timestamp: null,
    message: typeof data === 'string' ? data : (data?.message || null),
  };
};

const normalizeBaseUrl = (value) => {
  if (typeof value !== 'string') {
    return '';
  }

  return value.trim().replace(/\/+$/, '');
};

const getFallbackBaseUrl = () => {
  if (typeof window === 'undefined' || !window.location?.origin) {
    return 'http://localhost:8080/api';
  }

  try {
    const currentLocation = new URL(window.location.origin);
    const isLocalhost = currentLocation.hostname === 'localhost' || currentLocation.hostname === '127.0.0.1';

    if (isLocalhost) {
      return 'http://localhost:8080/api';
    }

    return `${currentLocation.origin}/api`;
  } catch {
    return 'http://localhost:8080/api';
  }
};

const getBaseUrl = () => normalizeBaseUrl(import.meta.env.VITE_BRAINBOX_API_URL) || getFallbackBaseUrl();

const createNetworkErrorResponse = (error) => ({
  success: false,
  status: 0,
  data: null,
  error: null,
  timestamp: null,
  message: error.message || 'Network error',
});

const refreshAccessToken = async (baseUrl, requestOptions, endpoint, method, body, headers) => {
  const refreshToken = getCookie('refreshToken');

  if (!refreshToken) {
    return null;
  }

  if (isRefreshing) {
    return new Promise((resolve) => {
      refreshQueue.push({
        resolve: (token) => {
          resolve(apiCall(endpoint, method, body, headers, true, requestOptions, token));
        },
        reject: () => {
          resolve({
            success: false,
            status: 401,
            data: null,
            error: null,
            timestamp: null,
            message: 'Session expired',
          });
        },
      });
    });
  }

  isRefreshing = true;

  try {
    const refreshUrl = `${baseUrl}/auth/refresh-token?refreshToken=${refreshToken}`;
    const refreshResponse = await fetch(refreshUrl, { method: 'POST' });

    if (!refreshResponse.ok) {
      processQueue(new Error('Refresh failed'));
      deleteCookie('accessToken');
      deleteCookie('refreshToken');
      return {
        success: false,
        status: 401,
        data: null,
        error: null,
        timestamp: null,
        message: 'Session expired',
      };
    }

    const refreshBody = await refreshResponse.json();
    const nextAccessToken = refreshBody.data?.accessToken;
    const nextRefreshToken = refreshBody.data?.refreshToken;

    setCookie('accessToken', nextAccessToken, 1);
    setCookie('refreshToken', nextRefreshToken, 7);
    processQueue(null, nextAccessToken);

    return apiCall(endpoint, method, body, headers, true, requestOptions, nextAccessToken);
  } catch (error) {
    processQueue(error);
    return {
      success: false,
      status: 401,
      data: null,
      error: null,
      timestamp: null,
      message: 'Session expired',
    };
  } finally {
    isRefreshing = false;
  }
};

export const apiCall = async (
  endpoint,
  method = 'GET',
  body = null,
  headers = {},
  isRefreshAttempt = false,
  requestOptions = {},
  accessTokenOverride = null,
) => {
  const baseUrl = getBaseUrl();
  const url = `${baseUrl}${endpoint}`;
  const accessToken = accessTokenOverride ?? getCookie('accessToken');
  const normalizedMethod = method.toUpperCase();
  const isFormDataBody = typeof FormData !== 'undefined' && body instanceof FormData;
  const {
    skipAuthRefresh = false,
    ...fetchOptions
  } = requestOptions;

  const options = {
    ...fetchOptions,
    ...(normalizedMethod === 'GET' && accessToken && fetchOptions.cache === undefined ? { cache: 'no-store' } : {}),
    method: normalizedMethod,
    headers: {
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...(fetchOptions.headers || {}),
      ...headers,
    },
  };

  if (!isFormDataBody) {
    options.headers = {
      'Content-Type': 'application/json',
      ...options.headers,
    };
  }

  if (body) {
    options.body = isFormDataBody ? body : JSON.stringify(body);
  }

  try {
    const response = await fetch(url, options);

    if (response.status === 401 && !isRefreshAttempt && !skipAuthRefresh) {
      const refreshedResponse = await refreshAccessToken(baseUrl, requestOptions, endpoint, normalizedMethod, body, headers);
      if (refreshedResponse) {
        return refreshedResponse;
      }
    }

    const contentType = response.headers.get('content-type');
    const data = contentType && contentType.includes('application/json')
      ? await response.json()
      : await response.text();

    return parseApiResponse(response.status, data);
  } catch (error) {
    console.error(`API call to ${endpoint} failed (${url}):`, error);
    return createNetworkErrorResponse(error);
  }
};

export default apiCall;
