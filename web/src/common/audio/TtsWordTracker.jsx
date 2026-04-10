import { forwardRef, useCallback, useEffect, useMemo, useRef } from 'react';
import { findRangeIndexForOffset } from './playbackModel';

const assignRef = (ref, value) => {
  if (!ref) {
    return;
  }

  if (typeof ref === 'function') {
    ref(value);
    return;
  }

  ref.current = value;
};

const TtsWordTracker = forwardRef(({
  variant = 'subtitle',
  annotatedHtml = '',
  wordRanges = [],
  activeOffset = 0,
  isActive = true,
  isPlaying = false,
  className = '',
  wordClassName = '',
  activeWordClassName = '',
  autoScrollAxis = null,
  scrollPadding = 32,
  scrollOffsetRatio = 0.2,
}, forwardedRef) => {
  const containerRef = useRef(null);
  const activeWordRef = useRef(null);

  const setContainerRef = useCallback((node) => {
    containerRef.current = node;
    assignRef(forwardedRef, node);
  }, [forwardedRef]);

  const activeWordId = useMemo(() => {
    if (!isActive || wordRanges.length === 0) {
      return null;
    }

    const activeWordIndex = findRangeIndexForOffset(wordRanges, activeOffset);

    if (!Number.isInteger(activeWordIndex) || activeWordIndex < 0 || activeWordIndex >= wordRanges.length) {
      return null;
    }

    return wordRanges[activeWordIndex]?.id || `tts-word-${activeWordIndex}`;
  }, [activeOffset, isActive, wordRanges]);

  useEffect(() => {
    const currentActiveWord = activeWordRef.current;

    if (currentActiveWord) {
      currentActiveWord.classList.remove(activeWordClassName);
      activeWordRef.current = null;
    }

    const container = containerRef.current;

    if (!container || !activeWordId || !activeWordClassName) {
      return;
    }

    const nextActiveWord = container.querySelector(
      `[data-tts-word-id="${activeWordId}"], [data-reading-word-id="${activeWordId}"]`,
    );

    if (!(nextActiveWord instanceof HTMLElement)) {
      return;
    }

    nextActiveWord.classList.add(activeWordClassName);
    activeWordRef.current = nextActiveWord;

    if (autoScrollAxis === 'x') {
      container.scrollLeft = Math.max(0, nextActiveWord.offsetLeft - (container.clientWidth * scrollOffsetRatio));
      return;
    }

    if (autoScrollAxis === 'y' && isPlaying) {
      const containerRect = container.getBoundingClientRect();
      const wordRect = nextActiveWord.getBoundingClientRect();
      const isOutsideViewport = wordRect.top < (containerRect.top + scrollPadding)
        || wordRect.bottom > (containerRect.bottom - scrollPadding);

      if (isOutsideViewport) {
        nextActiveWord.scrollIntoView({ block: 'nearest', inline: 'nearest' });
      }
    }
  }, [
    activeWordClassName,
    activeWordId,
    autoScrollAxis,
    isPlaying,
    scrollOffsetRatio,
    scrollPadding,
  ]);

  if (variant === 'html') {
    return (
      <div
        ref={setContainerRef}
        className={className}
        dangerouslySetInnerHTML={{ __html: annotatedHtml }}
      />
    );
  }

  return (
    <div ref={setContainerRef} className={className}>
      {wordRanges.map((word, index) => {
        const wordId = word?.id || `tts-word-${index}`;

        return (
          <span
            key={wordId}
            data-tts-word-id={wordId}
            className={wordClassName}
          >
            {word?.text || ''}
          </span>
        );
      })}
    </div>
  );
});

TtsWordTracker.displayName = 'TtsWordTracker';

export default TtsWordTracker;