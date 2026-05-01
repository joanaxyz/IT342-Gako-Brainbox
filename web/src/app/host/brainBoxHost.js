const ANDROID_HOST = 'android';
const PDF_EXPORT_KEY = '__BRAINBOX_PENDING_PDF_EXPORT__';
const HOST_AUDIO_STATE_EVENT = 'brainbox-host-audio-state';

const getWindow = () => (typeof window === 'undefined' ? null : window);

const getSearchParams = () => {
  const currentWindow = getWindow();

  if (!currentWindow) {
    return new URLSearchParams();
  }

  return new URLSearchParams(currentWindow.location.search);
};

const getBridge = () => {
  const currentWindow = getWindow();
  return currentWindow?.BrainBoxHost ?? null;
};

export const isAndroidHost = () => {
  const currentWindow = getWindow();

  if (!currentWindow) {
    return false;
  }

  return getSearchParams().get('host') === ANDROID_HOST || Boolean(currentWindow.BrainBoxHost);
};

export const getEmbeddedNotebookId = () => getSearchParams().get('notebookId') || '';

export const getHostApiBaseUrl = () => getSearchParams().get('apiBaseUrl') || '';

export const closeHostEditor = () => {
  const bridge = getBridge();

  if (!bridge?.closeEditor) {
    return false;
  }

  bridge.closeEditor();
  return true;
};

export const persistHostSession = (accessToken = '', refreshToken = '') => {
  const bridge = getBridge();

  if (!bridge?.persistSession) {
    return false;
  }

  bridge.persistSession(accessToken, refreshToken);
  return true;
};

export const clearHostSession = () => {
  const bridge = getBridge();

  if (!bridge?.clearSession) {
    return false;
  }

  bridge.clearSession();
  return true;
};

export const reportHostReady = () => {
  const bridge = getBridge();

  if (!bridge?.reportReady) {
    return false;
  }

  bridge.reportReady();
  return true;
};

export const reportHostError = (message) => {
  const bridge = getBridge();

  if (!bridge?.reportError) {
    return false;
  }

  bridge.reportError(String(message || 'Unknown embedded editor error'));
  return true;
};

const blobToBase64 = (blob) => new Promise((resolve, reject) => {
  const reader = new FileReader();

  reader.onload = () => {
    const result = typeof reader.result === 'string' ? reader.result : '';
    const base64 = result.includes(',') ? result.split(',')[1] : result;
    resolve(base64);
  };
  reader.onerror = () => reject(reader.error || new Error('Unable to encode export file.'));
  reader.readAsDataURL(blob);
});

export const exportFileThroughHost = async ({ filename, mimeType, blob }) => {
  const bridge = getBridge();

  if (!bridge?.exportFile) {
    return false;
  }

  const base64 = await blobToBase64(blob);
  bridge.exportFile({
    filename,
    mimeType,
    base64,
  });
  return true;
};

export const storePendingPdfExport = (payload) => {
  const currentWindow = getWindow();

  if (!currentWindow) {
    return;
  }

  currentWindow[PDF_EXPORT_KEY] = {
    ...payload,
    requestedAt: Date.now(),
  };
};

export const clearPendingPdfExport = () => {
  const currentWindow = getWindow();

  if (!currentWindow) {
    return;
  }

  delete currentWindow[PDF_EXPORT_KEY];
};

export const requestHostPdfExport = () => {
  const bridge = getBridge();

  if (!bridge?.requestPdfExport) {
    return false;
  }

  bridge.requestPdfExport();
  return true;
};

export const openHostQuiz = (uuid) => {
  const bridge = getBridge();

  if (!bridge?.openQuiz) {
    return false;
  }

  bridge.openQuiz(String(uuid || ''));
  return true;
};

export const openHostFlashcardDeck = (uuid) => {
  const bridge = getBridge();

  if (!bridge?.openFlashcardDeck) {
    return false;
  }

  bridge.openFlashcardDeck(String(uuid || ''));
  return true;
};

export const canUseHostAudio = () => {
  const bridge = getBridge();
  return Boolean(
    bridge?.playNotebookAudio
    && bridge?.pauseAudio
    && bridge?.resumeAudio
    && bridge?.stopAudio
    && bridge?.setAudioSpeechRate
  );
};

export const subscribeHostAudioState = (callback) => {
  const currentWindow = getWindow();

  if (!currentWindow || typeof callback !== 'function') {
    return () => {};
  }

  const handleHostAudioState = (event) => {
    callback(event?.detail || null);
  };

  currentWindow.addEventListener(HOST_AUDIO_STATE_EVENT, handleHostAudioState);
  return () => {
    currentWindow.removeEventListener(HOST_AUDIO_STATE_EVENT, handleHostAudioState);
  };
};

export const playHostNotebookAudio = ({
  notebookUuid,
  notebookTitle,
  content,
  fullText,
  chunks,
  speechRate = 1,
  startCharOffset = 0,
} = {}) => {
  const bridge = getBridge();

  if (!bridge?.playNotebookAudio) {
    return false;
  }

  bridge.playNotebookAudio(JSON.stringify({
    notebookUuid,
    notebookTitle,
    content,
    fullText,
    chunks,
    speechRate,
    startCharOffset,
  }));
  return true;
};

export const pauseHostAudio = () => {
  const bridge = getBridge();

  if (!bridge?.pauseAudio) {
    return false;
  }

  bridge.pauseAudio();
  return true;
};

export const resumeHostAudio = () => {
  const bridge = getBridge();

  if (!bridge?.resumeAudio) {
    return false;
  }

  bridge.resumeAudio();
  return true;
};

export const stopHostAudio = () => {
  const bridge = getBridge();

  if (!bridge?.stopAudio) {
    return false;
  }

  bridge.stopAudio();
  return true;
};

export const setHostAudioSpeechRate = (speechRate = 1) => {
  const bridge = getBridge();

  if (!bridge?.setAudioSpeechRate) {
    return false;
  }

  bridge.setAudioSpeechRate(String(speechRate));
  return true;
};
