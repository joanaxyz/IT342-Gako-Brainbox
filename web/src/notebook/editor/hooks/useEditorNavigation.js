import { useCallback, useEffect, useRef, useState } from 'react';
import { useBlocker, useNavigate } from 'react-router-dom';

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
    if (!isSavingBeforeExit) navigate('/dashboard');
  }, [isSavingBeforeExit, navigate]);

  return { isSavingBeforeExit, handleBackHome };
};

export default useEditorNavigation;
