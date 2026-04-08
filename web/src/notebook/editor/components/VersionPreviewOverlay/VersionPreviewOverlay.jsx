import { useEffect, useMemo, useRef, useState } from 'react';
import {
  ChevronLeft,
  ChevronRight,
  History,
  RotateCcw,
  X,
} from 'lucide-react';
import NoteEditorContent from '../NoteEditorContent/NoteEditorContent';
import { buildProposalComparisonSession } from '../../utils/proposalComparison';
import './VersionPreviewOverlay.css';

const formatVersionTimestamp = (value) => {
  if (!value) {
    return 'Unknown save time';
  }

  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(value));
};

const getVersionDate = (version) => version?.version || version?.created_at || version?.date || null;

const VersionPreviewOverlay = ({
  isOpen,
  previewVersion,
  currentContent,
  previewContent,
  fontFamily,
  paperWidth,
  paperHeight,
  onClose,
  onOpenHistory,
  onRestore,
}) => {
  const currentPaneRef = useRef(null);
  const previewPaneRef = useRef(null);
  const [activeChangeIndex, setActiveChangeIndex] = useState(-1);
  const comparisonSession = useMemo(
    () => buildProposalComparisonSession(currentContent || '', previewContent || ''),
    [currentContent, previewContent],
  );
  const changes = comparisonSession.changes;
  const currentChange = activeChangeIndex >= 0 ? changes[activeChangeIndex] || null : null;
  const currentDescriptors = useMemo(() => ([
    {
      blockIndexes: changes.flatMap((change) => change.originalBlockIndexes),
      tone: 'original',
      activeBlockIndexes: currentChange?.originalBlockIndexes || [],
    },
  ]), [changes, currentChange]);
  const previewDescriptors = useMemo(() => ([
    {
      blockIndexes: changes.flatMap((change) => change.proposedBlockIndexes),
      tone: 'proposal',
      activeBlockIndexes: currentChange?.proposedBlockIndexes || [],
    },
  ]), [changes, currentChange]);
  const previewDate = getVersionDate(previewVersion);

  useEffect(() => {
    setActiveChangeIndex(changes.length > 0 ? 0 : -1);
  }, [changes.length, previewVersion?.id]);

  useEffect(() => {
    if (!isOpen) {
      currentPaneRef.current?.clearAiHighlights?.();
      previewPaneRef.current?.clearAiHighlights?.();
      return;
    }

    const frame = window.requestAnimationFrame(() => {
      currentPaneRef.current?.setAiHighlightsByBlockDescriptors?.(currentDescriptors);
      previewPaneRef.current?.setAiHighlightsByBlockDescriptors?.(previewDescriptors);

      const currentFocusBlock = currentChange?.originalBlockIndexes?.[0];
      const previewFocusBlock = currentChange?.proposedBlockIndexes?.[0];

      if (Number.isInteger(currentFocusBlock)) {
        currentPaneRef.current?.scrollToTopLevelBlock?.(currentFocusBlock);
      }

      if (Number.isInteger(previewFocusBlock)) {
        previewPaneRef.current?.scrollToTopLevelBlock?.(previewFocusBlock);
      }
    });

    return () => window.cancelAnimationFrame(frame);
  }, [currentChange, currentDescriptors, isOpen, previewDescriptors]);

  if (!isOpen) {
    return null;
  }

  const hasChanges = changes.length > 0;

  return (
    <>
      <div className="version-preview-backdrop" aria-hidden="true" />
      <section className="version-preview-modal" role="dialog" aria-modal="true" aria-label="Version comparison preview">
        <header className="version-preview-header">
          <div className="version-preview-header-copy">
            <span className="version-preview-kicker">Version Preview</span>
            <strong>{previewDate ? formatVersionTimestamp(previewDate) : 'Selected version'}</strong>
            <small>
              {hasChanges ? `${changes.length} changed section${changes.length === 1 ? '' : 's'}` : 'No content differences'}
            </small>
          </div>

          <div className="version-preview-header-actions">
            <div className="version-preview-nav">
              <button
                type="button"
                className="version-preview-nav-btn"
                onClick={() => setActiveChangeIndex((currentIndex) => (
                  currentIndex <= 0 ? changes.length - 1 : currentIndex - 1
                ))}
                disabled={!hasChanges}
                aria-label="Previous change"
              >
                <ChevronLeft size={16} />
              </button>
              <span className="version-preview-nav-copy">
                {hasChanges ? `${activeChangeIndex + 1} / ${changes.length}` : 'No changes'}
              </span>
              <button
                type="button"
                className="version-preview-nav-btn"
                onClick={() => setActiveChangeIndex((currentIndex) => (
                  currentIndex >= changes.length - 1 ? 0 : currentIndex + 1
                ))}
                disabled={!hasChanges}
                aria-label="Next change"
              >
                <ChevronRight size={16} />
              </button>
            </div>

            <button
              type="button"
              className="version-preview-btn version-preview-btn--ghost"
              onClick={onOpenHistory}
            >
              <History size={15} />
              <span>History</span>
            </button>
            <button
              type="button"
              className="version-preview-btn version-preview-btn--restore"
              onClick={onRestore}
            >
              <RotateCcw size={15} />
              <span>Restore</span>
            </button>
            <button
              type="button"
              className="version-preview-btn version-preview-btn--ghost"
              onClick={onClose}
              aria-label="Close version preview"
            >
              <X size={15} />
              <span>Close</span>
            </button>
          </div>
        </header>

        <div className="version-preview-body">
          <section className="version-preview-pane">
            <header className="version-preview-pane-header">
              <span>Current notebook</span>
            </header>
            <div className="version-preview-pane-body">
              <NoteEditorContent
                key={`version_preview_current_${previewVersion?.id ?? 'none'}`}
                ref={currentPaneRef}
                storageKey="version_preview_current"
                content={currentContent || ''}
                readOnly
                fontFamily={fontFamily}
                paperWidth={paperWidth}
                paperHeight={paperHeight}
                onOutlineChange={() => {}}
              />
            </div>
          </section>

          <section className="version-preview-pane">
            <header className="version-preview-pane-header version-preview-pane-header--accent">
              <span>{previewDate ? `Version from ${formatVersionTimestamp(previewDate)}` : 'Selected version'}</span>
            </header>
            <div className="version-preview-pane-body">
              <NoteEditorContent
                key={`version_preview_selected_${previewVersion?.id ?? 'none'}`}
                ref={previewPaneRef}
                storageKey={`version_preview_selected_${previewVersion?.id ?? 'none'}`}
                content={previewContent || ''}
                readOnly
                fontFamily={fontFamily}
                paperWidth={paperWidth}
                paperHeight={paperHeight}
                onOutlineChange={() => {}}
              />
            </div>
          </section>
        </div>
      </section>
    </>
  );
};

export default VersionPreviewOverlay;
