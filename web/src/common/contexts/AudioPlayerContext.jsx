import { useState, useCallback, useRef, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { notebookAPI } from '../utils/api';
import { useNotebook } from '../../notebook/shared/hooks/hooks';
import {
  buildPlaybackModel,
  findRangeIndexForOffset,
} from '../audio/playbackModel';
import { AudioPlayerContext } from './AudioPlayerContextValue';

const clamp = (value, minimum, maximum) => Math.min(Math.max(value, minimum), maximum);
const CHARS_PER_SEC = 15;
const KEEP_ALIVE_INTERVAL_MS = 5000;
const STALL_TIMEOUT_MS = 15000;
const SPEAK_START_DELAY_MS = 60;
const TARGET_CHUNK_DURATION_SEC = 8;
const MIN_CHUNK_CHARS = 80;
const MAX_CHUNK_CHARS = 180;
const MAX_RECOVERY_ATTEMPTS = 3;
const getResumeStorageKey = (notebookUuid) => `nb_ts_${notebookUuid}`;
const getChunkCharLimit = (rate) => clamp(
  Math.round(CHARS_PER_SEC * clamp(rate, 0.5, 2) * TARGET_CHUNK_DURATION_SEC),
  MIN_CHUNK_CHARS,
  MAX_CHUNK_CHARS,
);

const normalizePlayOptions = (forceOrOptions, maybeCharOffset) => {
  if (typeof forceOrOptions === 'object' && forceOrOptions !== null) {
    return forceOrOptions;
  }

  return {
    force: Boolean(forceOrOptions),
    charOffset: maybeCharOffset,
  };
};

export const AudioPlayerProvider = ({ children }) => {
  const { markNotebookReviewed } = useNotebook();
  const [currentNotebook, setCurrentNotebook] = useState(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isPreparing, setIsPreparing] = useState(false);
  const [progress, setProgress] = useState(0);
  const [queue, setQueue] = useState([]);
  const [isMinimized, setIsMinimized] = useState(true);
  const [showQueue, setShowQueue] = useState(false);
  const [volume, setVolumeState] = useState(1);
  const [rate, setRateState] = useState(1);
  const [loop, setLoopState] = useState(false);
  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const [durationSec, setDurationSec] = useState(0);
  const [currentCharOffset, setCurrentCharOffset] = useState(0);
  const [currentFullText, setCurrentFullText] = useState('');

  const synthRef = useRef(typeof window !== 'undefined' ? window.speechSynthesis : null);
  const utteranceRef = useRef(null);
  const queueRef = useRef(queue);
  const playRef = useRef(null);
  const volumeRef = useRef(1);
  const rateRef = useRef(1);
  const loopRef = useRef(false);
  const isPlayingRef = useRef(false);
  const isPreparingRef = useRef(false);
  const currentNotebookRef = useRef(null);
  const currentModelRef = useRef(null);
  const currentOffsetRef = useRef(0);
  const currentChunkIndexRef = useRef(-1);
  const utteranceIdRef = useRef(0);
  const sourceCacheRef = useRef({});
  const isPausedRef = useRef(false);
  const pausedAtCharRef = useRef(0);
  const durationSecRef = useRef(0);
  const keepAliveRef = useRef(null);
  const hasBoundaryProgressRef = useRef(false);
  const lastBoundaryAtRef = useRef(0);
  const recoveryAttemptsRef = useRef(0);

  const location = useLocation();

  useEffect(() => {
    queueRef.current = queue;
  }, [queue]);

  useEffect(() => {
    isPlayingRef.current = isPlaying;
  }, [isPlaying]);

  useEffect(() => {
    isPreparingRef.current = isPreparing;
  }, [isPreparing]);

  useEffect(() => {
    currentNotebookRef.current = currentNotebook;
  }, [currentNotebook]);

  const resetBoundaryTracking = useCallback(() => {
    hasBoundaryProgressRef.current = false;
    lastBoundaryAtRef.current = 0;
  }, []);

  const resetRecoveryState = useCallback(() => {
    recoveryAttemptsRef.current = 0;
    resetBoundaryTracking();
  }, [resetBoundaryTracking]);

  const noteBoundaryProgress = useCallback(() => {
    hasBoundaryProgressRef.current = true;
    lastBoundaryAtRef.current = Date.now();
    recoveryAttemptsRef.current = 0;
  }, []);

  const stopKeepAlive = useCallback(() => {
    if (keepAliveRef.current) {
      clearInterval(keepAliveRef.current);
      keepAliveRef.current = null;
    }
  }, []);

  const waitForSynthToSettle = useCallback(async () => {
    if (typeof window === 'undefined' || !synthRef.current) {
      return;
    }

    await new Promise((resolve) => {
      window.setTimeout(resolve, SPEAK_START_DELAY_MS);
    });
  }, []);

  const recoverPlayback = useCallback((notebook, offset = 0) => {
    if (!notebook?.uuid) {
      return;
    }

    if (recoveryAttemptsRef.current >= MAX_RECOVERY_ATTEMPTS) {
      stopKeepAlive();
      synthRef.current?.cancel();
      utteranceRef.current = null;
      setIsPreparing(false);
      setIsPlaying(false);
      return;
    }

    recoveryAttemptsRef.current += 1;
    resetBoundaryTracking();

    void playRef.current?.(notebook, undefined, {
      force: true,
      charOffset: Math.max(0, Math.floor(offset ?? 0)),
      resumeFromSaved: false,
      markReviewed: false,
      preserveRecoveryState: true,
    });
  }, [resetBoundaryTracking, stopKeepAlive]);

  const startKeepAlive = useCallback(() => {
    stopKeepAlive();

    if (!synthRef.current) {
      return;
    }

    keepAliveRef.current = setInterval(() => {
      if (!synthRef.current) {
        return;
      }

      if (synthRef.current.speaking && !synthRef.current.paused) {
        const stalled = hasBoundaryProgressRef.current
          && lastBoundaryAtRef.current > 0
          && (Date.now() - lastBoundaryAtRef.current) > STALL_TIMEOUT_MS;

        if (stalled && currentNotebookRef.current) {
          // Only recover if we're confident the synth is actually stuck,
          // not just mid-utterance on a long word/phrase.
          if (recoveryAttemptsRef.current < MAX_RECOVERY_ATTEMPTS) {
            recoverPlayback(currentNotebookRef.current, currentOffsetRef.current);
          }
          return;
        }

        // Avoid calling pause()/resume() here — it can cut speech mid-utterance
        // in some browsers/voices. The stalled check above will trigger recovery
        // when appropriate; otherwise allow the synthesizer to continue.
        return;
      }

      if (
        isPlayingRef.current
        && !isPausedRef.current
        && !synthRef.current.speaking
        && !synthRef.current.paused
        && currentNotebookRef.current
      ) {
        recoverPlayback(currentNotebookRef.current, currentOffsetRef.current);
      }
    }, KEEP_ALIVE_INTERVAL_MS);
  }, [recoverPlayback, stopKeepAlive]);

  useEffect(() => () => {
    stopKeepAlive();
  }, [stopKeepAlive]);

  const clearResumeMarker = useCallback((notebookUuid) => {
    if (!notebookUuid || typeof window === 'undefined') {
      return;
    }

    localStorage.removeItem(getResumeStorageKey(notebookUuid));
  }, []);

  const readResumeMarker = useCallback((notebookUuid) => {
    if (!notebookUuid || typeof window === 'undefined') {
      return null;
    }

    const savedOffset = localStorage.getItem(getResumeStorageKey(notebookUuid));
    if (!savedOffset) {
      return null;
    }

    const parsedOffset = Number.parseInt(savedOffset, 10);
    return Number.isFinite(parsedOffset) ? parsedOffset : null;
  }, []);

  const updatePlaybackPosition = useCallback((offset, { persist = true } = {}) => {
    const model = currentModelRef.current;
    const totalChars = model?.fullText?.length ?? 0;
    const clampedOffset = clamp(offset, 0, totalChars);
    const fraction = totalChars > 0 ? clampedOffset / totalChars : 0;

    currentOffsetRef.current = clampedOffset;
    setCurrentCharOffset(clampedOffset);
    setProgress(fraction * 100);
    setCurrentTimeSec(durationSecRef.current * fraction);

    if (!persist || !currentNotebookRef.current?.uuid || typeof window === 'undefined') {
      return;
    }

    const storageKey = getResumeStorageKey(currentNotebookRef.current.uuid);
    if (clampedOffset <= 0 || clampedOffset >= totalChars) {
      localStorage.removeItem(storageKey);
      return;
    }

    localStorage.setItem(storageKey, String(clampedOffset));
  }, []);

  const stopPlayback = useCallback(() => {
    utteranceIdRef.current += 1;
    isPausedRef.current = false;
    pausedAtCharRef.current = 0;
    currentChunkIndexRef.current = -1;
    resetRecoveryState();
    stopKeepAlive();
    synthRef.current?.cancel();
    utteranceRef.current = null;
    setIsPreparing(false);
    setIsPlaying(false);
    updatePlaybackPosition(0, { persist: false });
  }, [resetRecoveryState, stopKeepAlive, updatePlaybackPosition]);

  const handleEditorRouteEnter = useCallback(() => {
    utteranceIdRef.current += 1;
    isPausedRef.current = false;
    pausedAtCharRef.current = 0;
    currentChunkIndexRef.current = -1;
    resetRecoveryState();
    stopKeepAlive();
    synthRef.current?.cancel();
    utteranceRef.current = null;
    setIsPreparing(false);
    setIsPlaying(false);
    updatePlaybackPosition(0, { persist: false });
  }, [resetRecoveryState, stopKeepAlive, updatePlaybackPosition]);

  const editorPathRef = useRef(null);

  useEffect(() => {
    const isEditorRoute = location.pathname.startsWith('/notebook/');
    if (!isEditorRoute) {
      editorPathRef.current = null;
      return undefined;
    }

    // Only stop playback when entering a *different* editor route,
    // not on every re-render while on the same route.
    if (editorPathRef.current === location.pathname) {
      return undefined;
    }

    editorPathRef.current = location.pathname;

    const animationFrameId = window.requestAnimationFrame(() => {
      handleEditorRouteEnter();
    });

    return () => window.cancelAnimationFrame(animationFrameId);
  }, [handleEditorRouteEnter, location.pathname]);

  const fetchNotebookPlaybackContent = useCallback(async (notebookUuid) => {
    const notebookResponse = await notebookAPI.getNotebook(notebookUuid);
    if (!notebookResponse.success) {
      return '';
    }

    return notebookResponse.data?.content || '';
  }, []);

  const resolvePlaybackSource = useCallback(async (notebook, contentOverride) => {
    if (contentOverride !== undefined && contentOverride !== null) {
      sourceCacheRef.current[notebook.uuid] = contentOverride;
      return contentOverride;
    }

    if (sourceCacheRef.current[notebook.uuid] !== undefined) {
      return sourceCacheRef.current[notebook.uuid];
    }

    const remoteContent = await fetchNotebookPlaybackContent(notebook.uuid);
    if (remoteContent) {
      sourceCacheRef.current[notebook.uuid] = remoteContent;
      return remoteContent;
    }

    sourceCacheRef.current[notebook.uuid] = notebook.content || notebook.title || '';
    return sourceCacheRef.current[notebook.uuid];
  }, [fetchNotebookPlaybackContent]);

  const finalizePlayback = useCallback(async (sessionId, notebook, playbackModel) => {
    if (sessionId !== utteranceIdRef.current) {
      return;
    }

    resetRecoveryState();
    stopKeepAlive();
    utteranceRef.current = null;
    currentChunkIndexRef.current = -1;
    setIsPreparing(false);
    updatePlaybackPosition(playbackModel.fullText.length, { persist: false });
    clearResumeMarker(notebook.uuid);

    if (loopRef.current) {
      await playRef.current?.(notebook, undefined, {
        force: true,
        charOffset: 0,
        resumeFromSaved: false,
        markReviewed: false,
      });
      return;
    }

    const currentQueue = queueRef.current;
    if (currentQueue.length > 0) {
      const nextNotebook = currentQueue[0];
      setQueue((previousQueue) => previousQueue.slice(1));
      await playRef.current?.(nextNotebook, undefined, { force: true });
      return;
    }

    setIsPlaying(false);
  }, [clearResumeMarker, resetRecoveryState, stopKeepAlive, updatePlaybackPosition]);

  const speakChunk = useCallback(function playChunk(chunkIndex, absoluteOffset, sessionId, notebook, playbackModel) {
    if (sessionId !== utteranceIdRef.current || !synthRef.current) {
      return;
    }

    if (!playbackModel?.chunks?.length || chunkIndex >= playbackModel.chunks.length) {
      void finalizePlayback(sessionId, notebook, playbackModel);
      return;
    }

    const chunk = playbackModel.chunks[chunkIndex];
    const offsetWithinChunk = clamp(absoluteOffset - chunk.start, 0, chunk.text.length);
    const textToSpeak = chunk.text.slice(offsetWithinChunk);

    if (!textToSpeak) {
      currentChunkIndexRef.current = chunkIndex + 1;
      updatePlaybackPosition(chunk.end);
      playChunk(chunkIndex + 1, chunk.end, sessionId, notebook, playbackModel);
      return;
    }

    resetBoundaryTracking();
    const utterance = new SpeechSynthesisUtterance(textToSpeak);
    utterance.volume = volumeRef.current;
    utterance.rate = rateRef.current;

    utterance.onstart = () => {
      if (sessionId !== utteranceIdRef.current) {
        return;
      }

      setIsPreparing(false);
      setIsPlaying(true);
      startKeepAlive();
    };

    utterance.onboundary = (event) => {
      if (sessionId !== utteranceIdRef.current) {
        return;
      }

      const nextOffset = clamp(
        chunk.start + offsetWithinChunk + (event.charIndex ?? 0),
        chunk.start,
        playbackModel.fullText.length,
      );
      currentChunkIndexRef.current = chunkIndex;
      updatePlaybackPosition(nextOffset);
      noteBoundaryProgress();
    };

    utterance.onend = () => {
      if (sessionId !== utteranceIdRef.current) {
        return;
      }

      stopKeepAlive();
      currentChunkIndexRef.current = chunkIndex + 1;
      recoveryAttemptsRef.current = 0;
      updatePlaybackPosition(chunk.end, { persist: chunk.end < playbackModel.fullText.length });
      // Small delay ensures the synthesizer is ready for the next utterance;
      // without this, some browsers silently drop the next speak() call.
      setTimeout(() => {
        if (sessionId !== utteranceIdRef.current) return;
        playChunk(chunkIndex + 1, chunk.end, sessionId, notebook, playbackModel);
      }, 50);
    };

    utterance.onerror = (event) => {
      if (sessionId !== utteranceIdRef.current) {
        return;
      }

      if (event.error === 'interrupted' || event.error === 'canceled') {
        return;
      }

      stopKeepAlive();
      setIsPreparing(false);
      recoverPlayback(notebook, currentOffsetRef.current);
    };

    utteranceRef.current = utterance;
    if (synthRef.current.paused) {
      synthRef.current.resume();
    }
    synthRef.current.speak(utterance);
  }, [
    finalizePlayback,
    noteBoundaryProgress,
    recoverPlayback,
    resetBoundaryTracking,
    startKeepAlive,
    stopKeepAlive,
    updatePlaybackPosition,
  ]);

  const play = useCallback(async (notebook, contentOverride, forceOrOptions = false, maybeCharOffset = undefined) => {
    if (!notebook?.uuid) {
      return;
    }

    const playOptions = normalizePlayOptions(forceOrOptions, maybeCharOffset);
    const force = Boolean(playOptions.force);

    if (!force && currentNotebookRef.current?.uuid === notebook.uuid && isPlayingRef.current) {
      return;
    }

    let resolvedOptions = { ...playOptions };
    if (
      !force
      && currentNotebookRef.current?.uuid === notebook.uuid
      && isPausedRef.current
      && resolvedOptions.charOffset === undefined
    ) {
      resolvedOptions = {
        ...resolvedOptions,
        charOffset: pausedAtCharRef.current,
        resumeFromSaved: false,
        markReviewed: false,
      };
    }

    utteranceIdRef.current += 1;
    const sessionId = utteranceIdRef.current;
    stopKeepAlive();
    synthRef.current?.cancel();
    utteranceRef.current = null;
    setCurrentNotebook(notebook);
    currentNotebookRef.current = notebook;
    setIsPreparing(true);
    setIsPlaying(false);
    isPausedRef.current = false;
    resetBoundaryTracking();

    if (resolvedOptions.preserveRecoveryState !== true) {
      recoveryAttemptsRef.current = 0;
    }

    await waitForSynthToSettle();
    if (sessionId !== utteranceIdRef.current) {
      return;
    }

    const source = await resolvePlaybackSource(notebook, contentOverride);
    if (sessionId !== utteranceIdRef.current) {
      return;
    }

    const playbackModel = buildPlaybackModel(source || notebook.title || '', {
      maxChunkChars: getChunkCharLimit(rateRef.current),
    });
    currentModelRef.current = playbackModel;
    setCurrentFullText(playbackModel.fullText);

    const totalChars = playbackModel.fullText.length;
    durationSecRef.current = totalChars > 0 ? totalChars / (CHARS_PER_SEC * rateRef.current) : 0;
    setDurationSec(durationSecRef.current);

    let resolvedOffset = resolvedOptions.charOffset;
    const allowSavedResume = resolvedOptions.resumeFromSaved !== false;

    if (resolvedOffset === undefined || resolvedOffset === null) {
      if (allowSavedResume) {
        resolvedOffset = readResumeMarker(notebook.uuid);
      }
      if (resolvedOffset === undefined || resolvedOffset === null) {
        resolvedOffset = 0;
      }
    }

    const safeOffset = totalChars > 0
      ? clamp(resolvedOffset, 0, Math.max(0, totalChars - 1))
      : 0;

    pausedAtCharRef.current = safeOffset;
    currentChunkIndexRef.current = findRangeIndexForOffset(playbackModel.chunks, safeOffset);

    if (safeOffset === 0 && !allowSavedResume) {
      clearResumeMarker(notebook.uuid);
    }

    updatePlaybackPosition(safeOffset, { persist: allowSavedResume || safeOffset > 0 });

    if (!playbackModel.fullText || playbackModel.chunks.length === 0) {
      setIsPreparing(false);
      setIsPlaying(false);
      return;
    }

    if (resolvedOptions.markReviewed !== false) {
      markNotebookReviewed(notebook.uuid).catch(() => {});
    }

    speakChunk(
      Math.max(0, currentChunkIndexRef.current),
      safeOffset,
      sessionId,
      notebook,
      playbackModel,
    );
  }, [
    clearResumeMarker,
    markNotebookReviewed,
    readResumeMarker,
    resolvePlaybackSource,
    speakChunk,
    stopKeepAlive,
    updatePlaybackPosition,
    waitForSynthToSettle,
  ]);

  useEffect(() => {
    playRef.current = play;
  }, [play]);

  const pause = useCallback(() => {
    if (!currentNotebookRef.current) {
      return;
    }

    isPausedRef.current = true;
    pausedAtCharRef.current = currentOffsetRef.current;
    utteranceIdRef.current += 1;
    resetRecoveryState();
    stopKeepAlive();
    synthRef.current?.cancel();
    utteranceRef.current = null;
    setIsPreparing(false);
    setIsPlaying(false);
    updatePlaybackPosition(pausedAtCharRef.current);
  }, [resetRecoveryState, stopKeepAlive, updatePlaybackPosition]);

  const togglePlay = useCallback(async (notebook, contentOverride) => {
    const targetNotebook = notebook || currentNotebookRef.current;
    if (!targetNotebook?.uuid) {
      return;
    }

    // Currently playing the same notebook → pause
    if (currentNotebookRef.current?.uuid === targetNotebook.uuid && (isPlayingRef.current || isPreparingRef.current)) {
      pause();
      return;
    }

    // Paused on the same notebook → resume from where we left off
    if (currentNotebookRef.current?.uuid === targetNotebook.uuid && isPausedRef.current) {
      const resumeOffset = pausedAtCharRef.current;
      // play() will clear isPausedRef internally; don't clear it early
      // to avoid a window where both isPaused and isPlaying are false.
      await play(targetNotebook, contentOverride, {
        force: true,
        charOffset: resumeOffset,
        resumeFromSaved: false,
        markReviewed: false,
      });
      return;
    }

    // Different notebook or fresh start
    await play(targetNotebook, contentOverride);
  }, [pause, play]);

  const setVolume = useCallback((nextVolume) => {
    volumeRef.current = nextVolume;
    setVolumeState(nextVolume);

    if (isPlayingRef.current && currentNotebookRef.current) {
      void playRef.current?.(currentNotebookRef.current, undefined, {
        force: true,
        charOffset: currentOffsetRef.current,
        resumeFromSaved: false,
        markReviewed: false,
      });
    }
  }, []);

  const setRate = useCallback((nextRate) => {
    rateRef.current = nextRate;
    setRateState(nextRate);

    if (isPlayingRef.current && currentNotebookRef.current) {
      void playRef.current?.(currentNotebookRef.current, undefined, {
        force: true,
        charOffset: currentOffsetRef.current,
        resumeFromSaved: false,
        markReviewed: false,
      });
    } else if (currentModelRef.current) {
      durationSecRef.current = currentModelRef.current.fullText.length > 0
        ? currentModelRef.current.fullText.length / (CHARS_PER_SEC * nextRate)
        : 0;
      setDurationSec(durationSecRef.current);
      updatePlaybackPosition(currentOffsetRef.current, { persist: false });
    }
  }, [updatePlaybackPosition]);

  const toggleLoop = useCallback(() => {
    setLoopState((previousLoop) => {
      const nextLoop = !previousLoop;
      loopRef.current = nextLoop;
      return nextLoop;
    });
  }, []);

  const seek = useCallback((fraction) => {
    const targetNotebook = currentNotebookRef.current;
    const playbackModel = currentModelRef.current;
    if (!targetNotebook?.uuid || !playbackModel?.fullText) {
      return;
    }

    const nextOffset = clamp(
      Math.floor(clamp(fraction, 0, 0.9999) * playbackModel.fullText.length),
      0,
      Math.max(0, playbackModel.fullText.length - 1),
    );

    pausedAtCharRef.current = nextOffset;
    void playRef.current?.(targetNotebook, undefined, {
      force: true,
      charOffset: nextOffset,
      resumeFromSaved: false,
      markReviewed: false,
    });
  }, []);

  const addToQueue = useCallback((notebook) => {
    setQueue((currentQueue) => {
      if (currentQueue.some((queuedNotebook) => queuedNotebook.uuid === notebook.uuid)) {
        return currentQueue;
      }

      return [...currentQueue, notebook];
    });
  }, []);

  const removeFromQueue = useCallback((notebookUuid) => {
    setQueue((currentQueue) => currentQueue.filter((notebook) => notebook.uuid !== notebookUuid));
  }, []);

  const clearQueue = useCallback(() => {
    setQueue([]);
  }, []);

  const playNext = useCallback(async () => {
    const currentQueue = queueRef.current;
    if (currentQueue.length === 0) {
      stopPlayback();
      return;
    }

    const nextNotebook = currentQueue[0];
    setQueue((previousQueue) => previousQueue.slice(1));
    await play(nextNotebook, undefined, { force: true });
  }, [play, stopPlayback]);

  const replay = useCallback(async () => {
    if (!currentNotebookRef.current?.uuid) {
      return;
    }

    clearResumeMarker(currentNotebookRef.current.uuid);
    pausedAtCharRef.current = 0;
    await playRef.current?.(currentNotebookRef.current, undefined, {
      force: true,
      charOffset: 0,
      resumeFromSaved: false,
      markReviewed: false,
    });
  }, [clearResumeMarker]);

  const value = {
    currentNotebook,
    isPlaying,
    isPreparing,
    progress,
    queue,
    togglePlay,
    play,
    pause,
    stopPlayback,
    replay,
    seek,
    addToQueue,
    removeFromQueue,
    clearQueue,
    playNext,
    volume,
    setVolume,
    rate,
    setRate,
    loop,
    toggleLoop,
    currentTimeSec,
    durationSec,
    currentCharOffset,
    currentFullText,
    isMinimized,
    setIsMinimized,
    showQueue,
    setShowQueue,
  };

  return (
    <AudioPlayerContext.Provider value={value}>
      {children}
    </AudioPlayerContext.Provider>
  );
};
