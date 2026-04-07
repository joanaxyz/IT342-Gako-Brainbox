import { useCallback, useEffect, useMemo, useState } from 'react';
import { EDITOR_FONTS } from '../editorFonts';

const clampZoom = (value) => Math.min(1.6, Math.max(0.6, value));

export const useNoteEditorPreferences = (locationState) => {
  const [aiSidebarOpen, setAiSidebarOpen] = useState(() => (
    localStorage.getItem('noteEditorAiSidebarOpen') === 'true'
  ));
  const [isReviewModeOpen, setIsReviewModeOpen] = useState(() => (
    locationState?.mode === 'review' || localStorage.getItem('noteEditorReviewModeOpen') === 'true'
  ));
  const [editorFont, setEditorFont] = useState(() => (
    localStorage.getItem('noteEditorFont') || 'default'
  ));
  const [zoomLevel, setZoomLevel] = useState(() => {
    const savedZoom = Number(localStorage.getItem('noteEditorZoom') || 1);
    return Number.isFinite(savedZoom) ? clampZoom(savedZoom) : 1;
  });
  const [showLines, setShowLines] = useState(() => (
    localStorage.getItem('noteEditorShowLines') === 'true'
  ));

  useEffect(() => {
    localStorage.setItem('noteEditorAiSidebarOpen', aiSidebarOpen.toString());
  }, [aiSidebarOpen]);

  useEffect(() => {
    localStorage.setItem('noteEditorReviewModeOpen', isReviewModeOpen.toString());
  }, [isReviewModeOpen]);

  useEffect(() => {
    localStorage.setItem('noteEditorFont', editorFont);
  }, [editorFont]);

  useEffect(() => {
    localStorage.setItem('noteEditorZoom', zoomLevel.toString());
  }, [zoomLevel]);

  useEffect(() => {
    localStorage.setItem('noteEditorShowLines', showLines.toString());
  }, [showLines]);

  const handleZoomChange = useCallback((nextZoom) => {
    setZoomLevel(clampZoom(nextZoom));
  }, []);

  const handleZoomStep = useCallback((delta) => {
    setZoomLevel((currentZoom) => clampZoom(Number((currentZoom + delta).toFixed(2))));
  }, []);

  const fontFamily = useMemo(() => (
    EDITOR_FONTS.find((font) => font.value === editorFont)?.family ?? 'inherit'
  ), [editorFont]);

  return {
    aiSidebarOpen,
    setAiSidebarOpen,
    isReviewModeOpen,
    setIsReviewModeOpen,
    editorFont,
    setEditorFont,
    zoomLevel,
    handleZoomChange,
    handleZoomStep,
    showLines,
    setShowLines,
    fontFamily,
  };
};
