import { useState, useCallback, useEffect } from 'react';
import {
  DEFAULT_PAPER_HEIGHT,
  DEFAULT_PAPER_WIDTH,
  MAX_PAPER_WIDTH,
  MIN_PAPER_WIDTH,
  PAPER_ASPECT_RATIO,
} from '../constants';

const STORAGE_KEY = 'noteEditorPaperWidth';
const LEGACY_STORAGE_KEY = 'noteEditorMaxWidth';
const CONTAINER_GUTTER_PX = 48;

const clampPaperWidth = (value, minWidth, maxWidth) => (
  Math.round(Math.min(maxWidth, Math.max(minWidth, value)))
);

const getAvailablePaperWidth = (container, zoom, minWidth, maxWidth) => {
  if (!container) {
    return maxWidth;
  }

  const nextWidth = (container.clientWidth - CONTAINER_GUTTER_PX) / Math.max(zoom, 0.01);
  return clampPaperWidth(nextWidth, minWidth, maxWidth);
};

export const useEditorResize = (
  containerRef,
  zoom = 1,
  {
    minWidth = MIN_PAPER_WIDTH,
    maxWidth = MAX_PAPER_WIDTH,
    defaultWidth = DEFAULT_PAPER_WIDTH,
  } = {},
) => {
  const [paperWidth, setPaperWidth] = useState(() => {
    const savedWidth = Number(localStorage.getItem(STORAGE_KEY));
    const legacyWidth = Number(localStorage.getItem(LEGACY_STORAGE_KEY));
    const fallbackWidth = Number.isFinite(savedWidth) && savedWidth > 0
      ? savedWidth
      : legacyWidth;

    return clampPaperWidth(
      Number.isFinite(fallbackWidth) && fallbackWidth > 0 ? fallbackWidth : defaultWidth,
      minWidth,
      maxWidth,
    );
  });
  const [isResizing, setIsResizing] = useState(false);

  const beginResize = useCallback((event) => {
    event.preventDefault();
    setIsResizing(true);
  }, []);

  useEffect(() => {
    if (!isResizing) {
      return undefined;
    }

    const handleMouseMove = (event) => {
      const container = containerRef.current;
      if (!container) {
        return;
      }

      const rect = container.getBoundingClientRect();
      const distanceFromCenter = Math.abs(event.clientX - (rect.left + (rect.width / 2)));
      const maxAvailableWidth = getAvailablePaperWidth(container, zoom, minWidth, maxWidth);
      const nextWidth = (distanceFromCenter * 2) / Math.max(zoom, 0.01);
      setPaperWidth(clampPaperWidth(nextWidth, minWidth, maxAvailableWidth));
    };

    const handleMouseUp = () => {
      setIsResizing(false);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
      document.body.style.cursor = '';
      document.body.style.userSelect = '';
    };
  }, [containerRef, isResizing, maxWidth, minWidth, zoom]);

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, paperWidth.toString());
    localStorage.removeItem(LEGACY_STORAGE_KEY);
  }, [paperWidth]);

  useEffect(() => {
    const container = containerRef.current;

    if (!container || typeof ResizeObserver === 'undefined') {
      return undefined;
    }

    const syncWidth = () => {
      const maxAvailableWidth = getAvailablePaperWidth(container, zoom, minWidth, maxWidth);
      setPaperWidth((currentWidth) => (
        currentWidth > maxAvailableWidth
          ? maxAvailableWidth
          : currentWidth
      ));
    };

    syncWidth();

    const resizeObserver = new ResizeObserver(() => {
      syncWidth();
    });

    resizeObserver.observe(container);

    return () => {
      resizeObserver.disconnect();
    };
  }, [containerRef, maxWidth, minWidth, zoom]);

  return {
    paperWidth,
    paperHeight: Math.round(paperWidth * PAPER_ASPECT_RATIO) || DEFAULT_PAPER_HEIGHT,
    isResizing,
    beginResize,
    setPaperWidth,
  };
};
