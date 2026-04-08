import { Highlighter, Trash2 } from 'lucide-react';

const EditorCanvasToolbar = ({
  zoomLevel,
  onZoomChange,
  onZoomStep,
  hasTextSelection = false,
  aiSelectionCount = 0,
  onAddAiSelection,
  onClearAiSelections,
  isAiSelectionDisabled = false,
  showAiSelectionControls = true,
  showLeadingDivider = true,
  layout = 'canvas',
  className = '',
}) => {
  const rootClassName = [
    'editor-canvas-toolbar',
    layout === 'dock' ? 'editor-canvas-toolbar--dock' : '',
    !showAiSelectionControls ? 'editor-canvas-toolbar--zoom-only' : '',
    className,
  ]
    .filter(Boolean)
    .join(' ');

  return (
    <div className={rootClassName}>
      <div className="editor-canvas-toolbar-main">
        {showLeadingDivider && <div className="editor-canvas-toolbar-divider" />}

        <span className="editor-canvas-group-label">Zoom</span>

        <div className="editor-canvas-zoom">
          <button
            type="button"
            className="editor-canvas-zoom-btn"
            onClick={() => onZoomStep(-0.1)}
            aria-label="Zoom out"
          >
            -
          </button>
          <input
            className="editor-canvas-zoom-slider"
            type="range"
            min="60"
            max="160"
            step="10"
            value={Math.round(zoomLevel * 100)}
            onChange={(event) => onZoomChange(Number(event.target.value) / 100)}
            aria-label="Zoom"
          />
          <button
            type="button"
            className="editor-canvas-zoom-btn"
            onClick={() => onZoomStep(0.1)}
            aria-label="Zoom in"
          >
            +
          </button>
          <span className="editor-canvas-zoom-label">{Math.round(zoomLevel * 100)}%</span>
        </div>
      </div>

      {showAiSelectionControls && (
        <div className="editor-canvas-toolbar-end">
          <span className="editor-canvas-group-label">AI highlights</span>
          <div className="editor-canvas-ai-selection">
            <button
              type="button"
              className={`editor-canvas-ai-btn ${hasTextSelection ? 'is-armed' : ''}`.trim()}
              onMouseDown={(event) => event.preventDefault()}
              onClick={onAddAiSelection}
              disabled={!onAddAiSelection || isAiSelectionDisabled}
              aria-label="Add current selection as an AI highlight"
              title={hasTextSelection ? 'Save the current selection for AI editing' : 'Select text in the editor first'}
            >
              <Highlighter size={16} />
              <span>Add</span>
              {aiSelectionCount > 0 && (
                <strong>{aiSelectionCount}</strong>
              )}
            </button>
            <button
              type="button"
              className="editor-canvas-ai-btn editor-canvas-ai-btn--ghost"
              onClick={onClearAiSelections}
              disabled={!onClearAiSelections || isAiSelectionDisabled || aiSelectionCount === 0}
              aria-label="Clear AI highlights"
              title="Clear saved AI highlights"
            >
              <Trash2 size={15} />
              <span>Clear</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default EditorCanvasToolbar;
