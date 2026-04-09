import { useState, useEffect, useRef } from 'react';

/**
 * Computes the floating inline-review anchor position for the AI proposal overlay.
 * Handles both the "full diff" mode (anchor follows the active block)
 * and the "selection changes" mode (anchor follows the hovered highlight element).
 */
const useProposalOverlayAnchor = ({
  editorRef,
  editorContainerRef,
  isAiProposalOpen,
  isSelectionReviewMode,
  activeProposalChangeIndex,
  activeProposalWorkingBlockIndexes,
  proposalRenderToken,
}) => {
  const [inlineProposalAnchor, setInlineProposalAnchor] = useState(null);
  const [hoveredProposalChangeIndex, setHoveredProposalChangeIndex] = useState(-1);
  const hoveredProposalHighlightRef = useRef(null);

  // Full-diff mode: anchor follows the active block
  useEffect(() => {
    if (!isAiProposalOpen || isSelectionReviewMode) {
      setInlineProposalAnchor(null);
      return undefined;
    }

    let frameId = 0;
    const updateAnchor = () => {
      const focusBlock = activeProposalWorkingBlockIndexes?.[0];
      const blockBounds = Number.isInteger(focusBlock)
        ? editorRef.current?.getTopLevelBlockBounds?.(focusBlock)
        : null;
      const containerRect = editorContainerRef.current?.getBoundingClientRect?.();

      if (!blockBounds || !containerRect) {
        setInlineProposalAnchor(null);
        return;
      }

      setInlineProposalAnchor({
        top: Math.max(18, Math.min(blockBounds.bottom - containerRect.top + 10, containerRect.height - 62)),
        left: Math.max(18, Math.min(blockBounds.left - containerRect.left, containerRect.width - 148)),
      });
    };

    const schedule = () => {
      window.cancelAnimationFrame(frameId);
      frameId = window.requestAnimationFrame(updateAnchor);
    };

    schedule();
    const viewportEl = editorRef.current?.getViewportElement?.();
    viewportEl?.addEventListener('scroll', schedule, { passive: true });
    window.addEventListener('resize', schedule);

    return () => {
      window.cancelAnimationFrame(frameId);
      viewportEl?.removeEventListener('scroll', schedule);
      window.removeEventListener('resize', schedule);
    };
  }, [
    activeProposalChangeIndex,
    activeProposalWorkingBlockIndexes,
    editorContainerRef,
    editorRef,
    isAiProposalOpen,
    isSelectionReviewMode,
    proposalRenderToken,
  ]);

  // Selection-changes mode: anchor follows the clicked/hovered highlight element
  useEffect(() => {
    if (!isSelectionReviewMode) {
      hoveredProposalHighlightRef.current = null;
      setHoveredProposalChangeIndex(-1);
      return undefined;
    }

    const containerEl = editorContainerRef.current;
    const viewportEl = editorRef.current?.getViewportElement?.();
    if (!viewportEl || !containerEl) return undefined;

    let frameId = 0;

    const updateAnchor = () => {
      const el = hoveredProposalHighlightRef.current;
      const containerRect = containerEl.getBoundingClientRect();

      if (!el || !containerEl.contains(el)) {
        setInlineProposalAnchor(null);
        return;
      }

      const rect = el.getBoundingClientRect();
      if (!rect.width && !rect.height) {
        setInlineProposalAnchor(null);
        return;
      }

      setInlineProposalAnchor({
        top:  Math.max(18, Math.min(rect.bottom  - containerRect.top  + 10, containerRect.height - 62)),
        left: Math.max(18, Math.min(rect.left    - containerRect.left,      containerRect.width  - 220)),
      });
    };

    const schedule = () => {
      window.cancelAnimationFrame(frameId);
      frameId = window.requestAnimationFrame(updateAnchor);
    };

    const handleClick = (event) => {
      const pointer = event.target instanceof Element ? event.target : null;
      if (pointer?.closest('.ai-proposal-inline-review')) return;

      const target = pointer?.closest('[data-ai-change-index]') || null;
      if (!(target instanceof HTMLElement) || !containerEl.contains(target)) return;

      if (hoveredProposalHighlightRef.current !== target) {
        hoveredProposalHighlightRef.current = target;
        const parsed = Number.parseInt(target.dataset.aiChangeIndex || '-1', 10);
        setHoveredProposalChangeIndex(Number.isInteger(parsed) ? parsed : -1);
      }

      schedule();
    };

    containerEl.addEventListener('click', handleClick);
    viewportEl.addEventListener('scroll', schedule, { passive: true });
    window.addEventListener('resize', schedule);

    return () => {
      window.cancelAnimationFrame(frameId);
      containerEl.removeEventListener('click', handleClick);
      viewportEl.removeEventListener('scroll', schedule);
      window.removeEventListener('resize', schedule);
    };
  }, [editorContainerRef, editorRef, isSelectionReviewMode, proposalRenderToken]);

  // Clear anchor state when proposal closes
  useEffect(() => {
    if (!isAiProposalOpen) {
      setInlineProposalAnchor(null);
      setHoveredProposalChangeIndex(-1);
      hoveredProposalHighlightRef.current = null;
    }
  }, [isAiProposalOpen]);

  return { inlineProposalAnchor, hoveredProposalChangeIndex };
};

export default useProposalOverlayAnchor;
