import { useCallback, useEffect, useState } from 'react';
import {
  DEFAULT_PAPER_HEIGHT,
  DEFAULT_PAPER_WIDTH,
  MAX_PAPER_WIDTH,
  MIN_PAPER_WIDTH,
  PAPER_ASPECT_RATIO,
} from '../constants';

const STORAGE_KEY = 'noteEditorPaperWidth';
const LEGACY_STORAGE_KEY = 'noteEditorMaxWidth';
const CONTAINER_GUTTER_PX = 24;
const MIN_AVAILABLE_PAPER_WIDTH = 160;

const clampPaperWidth = (value, minWidth, maxWidth) => (
  Math.round(Math.min(maxWidth, Math.max(minWidth, value)))
);

const getAvailablePaperWidth = (container, zoom, minWidth, maxWidth) => {
  if (!container) {
    return maxWidth;
  }

  const nextWidth = Math.max(
    MIN_AVAILABLE_PAPER_WIDTH,
    (container.clientWidth - CONTAINER_GUTTER_PX) / Math.max(zoom, 0.01),
  );
  const effectiveMinWidth = Math.min(minWidth, nextWidth);

  return clampPaperWidth(nextWidth, effectiveMinWidth, maxWidth);
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
  const [paperWidth, setPaperWidth] = useState(() => (
    clampPaperWidth(defaultWidth, minWidth, maxWidth)
  ));

  const syncWidth = useCallback(() => {
    const maxAvailableWidth = getAvailablePaperWidth(
      containerRef.current,
      zoom,
      minWidth,
      maxWidth,
    );
    const effectiveMinWidth = Math.min(minWidth, maxAvailableWidth);
    const nextWidth = clampPaperWidth(
      Math.min(defaultWidth, maxAvailableWidth),
      effectiveMinWidth,
      maxAvailableWidth,
    );

    setPaperWidth((currentWidth) => (
      currentWidth === nextWidth ? currentWidth : nextWidth
    ));
  }, [containerRef, defaultWidth, maxWidth, minWidth, zoom]);

  useEffect(() => {
    if (typeof window === 'undefined') {
      return;
    }

    localStorage.removeItem(STORAGE_KEY);
    localStorage.removeItem(LEGACY_STORAGE_KEY);
  }, []);

  useEffect(() => {
    const container = containerRef.current;

    syncWidth();

    if (!container || typeof ResizeObserver === 'undefined') {
      return undefined;
    }

    const resizeObserver = new ResizeObserver(() => {
      syncWidth();
    });

    resizeObserver.observe(container);

    return () => {
      resizeObserver.disconnect();
    };
  }, [containerRef, syncWidth]);

  return {
    paperWidth,
    paperHeight: Math.round(paperWidth * PAPER_ASPECT_RATIO) || DEFAULT_PAPER_HEIGHT,
  };
};
