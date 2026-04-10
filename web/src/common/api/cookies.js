import { clearHostSession, persistHostSession } from '../../app/host/brainBoxHost';

let hostSessionSyncScheduled = false;

const scheduleHostSessionSync = () => {
  if (hostSessionSyncScheduled || typeof window === 'undefined') {
    return;
  }

  hostSessionSyncScheduled = true;
  Promise.resolve().then(() => {
    hostSessionSyncScheduled = false;

    const accessToken = getCookie('accessToken');
    const refreshToken = getCookie('refreshToken');

    if (!accessToken && !refreshToken) {
      clearHostSession();
      return;
    }

    persistHostSession(accessToken || '', refreshToken || '');
  });
};

export const getCookie = (name) => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);

  if (parts.length === 2) {
    return parts.pop().split(';').shift();
  }

  return undefined;
};

export const setCookie = (name, value, days) => {
  let expires = '';

  if (days) {
    const date = new Date();
    date.setTime(date.getTime() + days * 24 * 60 * 60 * 1000);
    expires = `; expires=${date.toUTCString()}`;
  }

  document.cookie = `${name}=${value || ''}${expires}; path=/; SameSite=Lax`;
  scheduleHostSessionSync();
};

export const deleteCookie = (name) => {
  document.cookie = `${name}=; Max-Age=-99999999; path=/;`;
  scheduleHostSessionSync();
};

export const getAuthHeaders = () => {
  const accessToken = getCookie('accessToken');
  return accessToken ? { Authorization: `Bearer ${accessToken}` } : {};
};
