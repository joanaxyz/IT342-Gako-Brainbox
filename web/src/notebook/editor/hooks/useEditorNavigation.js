import { useCallback, useEffect, useRef, useState } from 'react';
import { useBlocker, useNavigate } from 'react-router-dom';
import { closeHostEditor, isAndroidHost } from '../../../app/host/brainBoxHost';

/**
 * Handles navigation-away blocking: auto-saves the document before leaving
 * and prevents navigation if the save fails.
 */
const useEditorNavigation = ({ hasUnsavedDocumentChanges, handleSaveNotebook, addNotification }) => {
  const navigate = useNavigate();
  const [isSavingBeforeExit, setIsSavingBeforeExit] = useState(false);
  const isExitSaveInFlightRef = useRef(false);
  const isMountedRef = useRef(true);

  useEffect(() => () => { isMountedRef.current = false; }, []);

  const navigationBlocker = useBlocker(useCallback(
    ({ currentLocation, nextLocation }) => {
      if (isAndroidHost()) return false;
      if (isExitSaveInFlightRef.current) return false;
      if (currentLocation.pathname === nextLocation.pathname) return false;
      return hasUnsavedDocumentChanges();
    },
    [hasUnsavedDocumentChanges],
  ));

  useEffect(() => {
    if (navigationBlocker.state !== 'blocked' || isExitSaveInFlightRef.current) return;

    isExitSaveInFlightRef.current = true;
    setIsSavingBeforeExit(true);

    void handleSaveNotebook()
      .then((response) => {
        if (!isMountedRef.current) return;
        if (response && !response.success) {
          addNotification(response.message || 'Failed to save notebook before leaving', 'error', 3000);
          navigationBlocker.reset();
          return;
        }
        navigationBlocker.proceed();
      })
      .finally(() => {
        if (!isMountedRef.current) return;
        isExitSaveInFlightRef.current = false;
        setIsSavingBeforeExit(false);
      });
  }, [addNotification, handleSaveNotebook, navigationBlocker]);

  const handleBackHome = useCallback(() => {
    if (isSavingBeforeExit) return;

    if (!isAndroidHost()) {
      navigate('/dashboard');
      return;
    }

    const exitEditor = async () => {
      if (!hasUnsavedDocumentChanges()) {
        if (!closeHostEditor()) {
          navigate('/dashboard');
        }
        return;
      }

      setIsSavingBeforeExit(true);
      isExitSaveInFlightRef.current = true;

      try {
        const response = await handleSaveNotebook();
        if (response && !response.success) {
          addNotification(response.message || 'Failed to save notebook before leaving', 'error', 3000);
          return;
        }

        if (!closeHostEditor()) {
          navigate('/dashboard');
        }
      } finally {
        if (isMountedRef.current) {
          setIsSavingBeforeExit(false);
        }
        isExitSaveInFlightRef.current = false;
      }
    };

    void exitEditor();
  }, [addNotification, handleSaveNotebook, hasUnsavedDocumentChanges, isSavingBeforeExit, navigate]);

  return { isSavingBeforeExit, handleBackHome };
};

export default useEditorNavigation;
