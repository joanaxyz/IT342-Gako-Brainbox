import { Editor as CoreEditor } from '@tiptap/core';
import { EditorContent, useEditor } from '@tiptap/react';
import {
  forwardRef,
  useCallback,
  useEffect,
  useImperativeHandle,
  useMemo,
  useRef,
  useState,
} from 'react';
import {
  DEFAULT_PAPER_HEIGHT,
  DEFAULT_PAPER_PADDING_BOTTOM,
  DEFAULT_PAPER_PADDING_TOP,
  DEFAULT_PAPER_PADDING_X,
  DEFAULT_PAPER_WIDTH,
} from '../../constants';
import {
  aiHighlightKey,
  aiSelectionHighlightKey,
  createEditorExtensions,
  getAiSelectionRanges,
  ttsWordHighlightKey,
} from '../../tiptap/createEditorExtensions';
import { findRangeIndexForOffset } from '../../../../common/audio/playbackModel';
import TableBubbleMenu from '../TableBubbleMenu/TableBubbleMenu';
import './NoteEditorContent.css';

const TABLE_CELL_MIN_WIDTH = 96;
const createAiSelectionId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `ai_sel_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
};

const buildOutlineItems = (editor) => {
  const items = [];

  editor?.state.doc.descendants((node, pos) => {
    if (node.type.name === 'heading') {
      items.push({
        level: node.attrs.level,
        text: node.textContent,
        pos,
      });
    }
  });

  return items;
};

const WORD_PATTERN = /\S+/g;

const buildTtsPositionMap = (editor) => {
  if (!editor) return [];

  const positions = [];
  let charOffset = 0;
  let isFirstBlock = true;

  editor.state.doc.descendants((node, pos) => {
    if (node.isBlock && !node.isTextblock) return;

    if (node.isTextblock) {
      if (!isFirstBlock) {
        charOffset += 1;
      }
      isFirstBlock = false;
      return;
    }

    if (node.isText && node.text) {
      const text = node.text;
      for (const match of text.matchAll(WORD_PATTERN)) {
        const wordStart = match.index;
        positions.push({
          charStart: charOffset + wordStart,
          charEnd: charOffset + wordStart + match[0].length,
          from: pos + wordStart,
          to: pos + wordStart + match[0].length,
        });
      }
      charOffset += text.length;
    }
  });

  return positions;
};

const NoteEditorContent = forwardRef(({
  storageKey,
  content,
  contentSyncToken = 0,
  onUpdateContent,
  onFocus,
  onBlur: onSaveBlur,
  fontFamily = 'inherit',
  showLines = false,
  onOutlineChange,
  onSelectionStateChange,
  onEditorReady,
  readOnly = false,
  reviewMode = false,
  paperWidth = DEFAULT_PAPER_WIDTH,
  paperHeight = DEFAULT_PAPER_HEIGHT,
  zoom = 1,
  aiSelectionMode = false,
  ttsActiveOffset = 0,
  ttsIsActive = false,
  ttsIsPlaying = false,
  ttsWordRanges = [],
}, ref) => {
  const viewportRef = useRef(null);
  const paperRef = useRef(null);
  const resizeObserverRef = useRef(null);
  const lastKnownContentRef = useRef(content || '');
  const lastAppliedContentSyncTokenRef = useRef(contentSyncToken);
  const suppressExternalUpdateRef = useRef(false);
  const isEditable = !readOnly;
  const bodyWidth = Math.max(160, paperWidth - (DEFAULT_PAPER_PADDING_X * 2));

  const [surfaceHeight, setSurfaceHeight] = useState(paperHeight);

  const editorExtensions = useMemo(() => createEditorExtensions({
    enableAiHighlight: true,
    enableAiSelectionHighlight: reviewMode || !readOnly,
    enableTtsWordHighlight: reviewMode,
    enableTableNormalization: !readOnly,
    cellMinWidth: TABLE_CELL_MIN_WIDTH,
    tableContainerWidth: bodyWidth,
  }), [bodyWidth, readOnly, reviewMode]);

  const scratchExtensions = useMemo(() => createEditorExtensions({
    enableAiHighlight: false,
    enableAiSelectionHighlight: false,
    enableTableNormalization: false,
    cellMinWidth: TABLE_CELL_MIN_WIDTH,
  }), []);

  const updateOutline = useCallback((currentEditor) => {
    onOutlineChange?.(buildOutlineItems(currentEditor));
  }, [onOutlineChange]);

  const emitSelectionState = useCallback((currentEditor) => {
    if (!currentEditor || (readOnly && !reviewMode)) {
      onSelectionStateChange?.({
        hasTextSelection: false,
        aiSelectionCount: 0,
        selectedText: '',
        isEditorFocused: false,
      });
      return;
    }

    const { from, to } = currentEditor.state.selection;
    const selectedText = from !== to
      ? currentEditor.state.doc.textBetween(from, to, ' ')
      : '';

    onSelectionStateChange?.({
      hasTextSelection: from !== to,
      aiSelectionCount: getAiSelectionRanges(currentEditor.state).length,
      selectedText,
      isEditorFocused: Boolean(currentEditor.isFocused),
    });
  }, [onSelectionStateChange, readOnly, reviewMode]);

  const editor = useEditor({
    extensions: editorExtensions,
    content: content || '',
    editable: isEditable,
    onCreate: ({ editor: currentEditor }) => {
      lastKnownContentRef.current = currentEditor.getHTML();

      if (!readOnly) {
        const savedPos = localStorage.getItem(`noteEditorPos_${storageKey}`);

        if (savedPos) {
          const pos = Number.parseInt(savedPos, 10);

          if (pos >= 0 && pos <= currentEditor.state.doc.content.size) {
            currentEditor.commands.setTextSelection(pos);
          }
        }

        currentEditor.commands.normalizeTables?.();
      }

      updateOutline(currentEditor);
      emitSelectionState(currentEditor);
      onEditorReady?.(currentEditor);
    },
    onUpdate: ({ editor: currentEditor }) => {
      const html = currentEditor.getHTML();
      lastKnownContentRef.current = html;
      updateOutline(currentEditor);
      emitSelectionState(currentEditor);

      if (!isEditable) {
        return;
      }

      if (suppressExternalUpdateRef.current) {
        suppressExternalUpdateRef.current = false;
        return;
      }

      const pos = currentEditor.state.selection.from;
      localStorage.setItem(`noteEditorPos_${storageKey}`, pos.toString());
      onUpdateContent?.(html);
    },
    onSelectionUpdate: ({ editor: currentEditor }) => {
      if (!isEditable && !reviewMode) {
        return;
      }

      if (isEditable) {
        const pos = currentEditor.state.selection.from;
        localStorage.setItem(`noteEditorPos_${storageKey}`, pos.toString());
      }

      emitSelectionState(currentEditor);
    },
    onBlur: ({ editor: currentEditor }) => {
      if (!isEditable) {
        return;
      }

      onSaveBlur?.(currentEditor.getHTML());
    },
    onFocus: ({ editor: currentEditor }) => {
      if (!isEditable) {
        return;
      }

      onFocus?.(currentEditor);
    },
    editorProps: {
      attributes: {
        class: 'note-editor-body',
      },
    },
  }, [editorExtensions, emitSelectionState, isEditable, storageKey, updateOutline]);

  const insertEquation = useCallback((kind = 'auto') => {
    if (!editor) {
      return;
    }

    const { selection } = editor.state;
    const isEmptyParagraph = selection.empty
      && selection.$from.parent.type.name === 'paragraph'
      && selection.$from.parent.textContent.trim().length === 0;
    const shouldInsertBlock = kind === 'block' || (kind === 'auto' && isEmptyParagraph);
    const range = {
      from: selection.from,
      to: selection.to,
    };

    if (shouldInsertBlock) {
      editor.chain().focus().insertBlockMath({ latex: '', range }).run();
      return;
    }

    editor.chain().focus().insertInlineMath({ latex: '', range }).run();
  }, [editor]);

  const applyExternalContent = useCallback((nextContent) => {
    if (!editor || editor.isDestroyed) {
      return;
    }

    suppressExternalUpdateRef.current = true;
    editor.commands.setContent(nextContent, false);
    editor.commands.normalizeTables?.();

    window.setTimeout(() => {
      suppressExternalUpdateRef.current = false;
    }, 0);
  }, [editor]);

  useEffect(() => {
    if (editor) {
      editor.setEditable(isEditable);
    }
  }, [editor, isEditable]);

  useEffect(() => {
    if (!editor || content === undefined || editor.isDestroyed) {
      return;
    }

    if (lastAppliedContentSyncTokenRef.current === contentSyncToken) {
      return;
    }

    const liveHtml = editor.getHTML();

    if (content !== liveHtml) {
      applyExternalContent(content);
    }

    lastKnownContentRef.current = editor.getHTML();
    lastAppliedContentSyncTokenRef.current = contentSyncToken;
    updateOutline(editor);
    emitSelectionState(editor);
  }, [applyExternalContent, content, contentSyncToken, editor, emitSelectionState, updateOutline]);

  useEffect(() => {
    if (!editor) {
      return;
    }

    updateOutline(editor);
    emitSelectionState(editor);

    const handleUpdate = ({ editor: currentEditor }) => {
      lastKnownContentRef.current = currentEditor.getHTML();
      emitSelectionState(currentEditor);
    };

    editor.on('update', handleUpdate);

    return () => {
      editor.off('update', handleUpdate);
    };
  }, [editor, emitSelectionState, updateOutline]);

  useEffect(() => {
    if (!editor || readOnly) {
      return;
    }

    const frame = window.requestAnimationFrame(() => {
      editor.commands.normalizeTables?.();
    });

    return () => window.cancelAnimationFrame(frame);
  }, [editor, paperWidth, readOnly]);

  useEffect(() => {
    const paper = paperRef.current;

    if (!paper) {
      return undefined;
    }

    const measureHeight = () => {
      setSurfaceHeight(Math.max(paperHeight, Math.ceil(paper.offsetHeight)));
    };

    measureHeight();

    if (typeof ResizeObserver === 'undefined') {
      return undefined;
    }

    resizeObserverRef.current?.disconnect();
    resizeObserverRef.current = new ResizeObserver(() => {
      measureHeight();
    });
    resizeObserverRef.current.observe(paper);

    return () => {
      resizeObserverRef.current?.disconnect();
      resizeObserverRef.current = null;
    };
  }, [content, paperHeight, paperWidth, zoom]);

  // ── TTS word highlight (review mode only) ─────────────────────────────
  const ttsPositionMapRef = useRef([]);

  useEffect(() => {
    if (!editor || !reviewMode) {
      ttsPositionMapRef.current = [];
      return;
    }
    ttsPositionMapRef.current = buildTtsPositionMap(editor);
  }, [editor, reviewMode, content]);

  useEffect(() => {
    if (!editor?.view || !reviewMode) return;

    if (!ttsIsActive || ttsWordRanges.length === 0) {
      editor.view.dispatch(editor.state.tr.setMeta(ttsWordHighlightKey, { from: 0, to: 0 }));
      return;
    }

    const wordIndex = findRangeIndexForOffset(ttsWordRanges, ttsActiveOffset);
    if (wordIndex < 0 || wordIndex >= ttsWordRanges.length) {
      editor.view.dispatch(editor.state.tr.setMeta(ttsWordHighlightKey, { from: 0, to: 0 }));
      return;
    }

    const word = ttsWordRanges[wordIndex];
    const posMap = ttsPositionMapRef.current;

    // Find the matching ProseMirror position for this word's char offset
    let pmFrom = 0;
    let pmTo = 0;
    for (let i = 0; i < posMap.length; i++) {
      if (posMap[i].charStart <= word.start && posMap[i].charEnd >= word.end) {
        pmFrom = posMap[i].from;
        pmTo = posMap[i].to;
        break;
      }
      if (posMap[i].charStart >= word.start) {
        pmFrom = posMap[i].from;
        pmTo = posMap[i].to;
        break;
      }
    }

    if (pmFrom > 0 && pmTo > pmFrom) {
      editor.view.dispatch(editor.state.tr.setMeta(ttsWordHighlightKey, { from: pmFrom, to: pmTo }));

      // Auto-scroll active word into view when playing
      if (ttsIsPlaying) {
        try {
          const coords = editor.view.coordsAtPos(pmFrom);
          const viewport = viewportRef.current;
          if (coords && viewport) {
            const vpRect = viewport.getBoundingClientRect();
            const padding = 32;
            if (coords.top < vpRect.top + padding || coords.bottom > vpRect.bottom - padding) {
              const domNode = editor.view.domAtPos(pmFrom);
              if (domNode?.node) {
                const el = domNode.node.nodeType === 3 ? domNode.node.parentElement : domNode.node;
                el?.scrollIntoView({ block: 'nearest', inline: 'nearest' });
              }
            }
          }
        } catch {
          // coords calculation can fail at edge positions
        }
      }
    } else {
      editor.view.dispatch(editor.state.tr.setMeta(ttsWordHighlightKey, { from: 0, to: 0 }));
    }
  }, [editor, reviewMode, ttsIsActive, ttsIsPlaying, ttsActiveOffset, ttsWordRanges]);

  const scrollToHeading = useCallback((pos) => {
    if (!editor) {
      return;
    }

    if (!reviewMode) {
      editor.commands.focus(pos);
    }

    const element = editor.view?.nodeDOM(pos);

    if (element instanceof HTMLElement) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start', inline: 'nearest' });
    }
  }, [editor, reviewMode]);

  const resolveAiSelectionTargets = useCallback((currentEditor) => {
    if (!currentEditor) {
      return [];
    }

    return getAiSelectionRanges(currentEditor.state)
      .map((range) => ({
        ...range,
        text: currentEditor.state.doc.textBetween(range.from, range.to, ' ').trim(),
      }))
      .filter((range) => range.text.length > 0);
  }, []);

  const dispatchAiSelectionMeta = useCallback((meta) => {
    if (!editor?.view) {
      return [];
    }

    editor.view.dispatch(editor.state.tr.setMeta(aiSelectionHighlightKey, meta));
    emitSelectionState(editor);
    return resolveAiSelectionTargets(editor);
  }, [editor, emitSelectionState, resolveAiSelectionTargets]);

  const dispatchAiHighlightMeta = useCallback((ranges) => {
    if (!editor?.view) {
      return;
    }

    editor.view.dispatch(editor.state.tr.setMeta(aiHighlightKey, ranges));
  }, [editor]);

  const getTopLevelBlockRanges = useCallback(() => {
    if (!editor) {
      return [];
    }

    const ranges = [];
    let blockIndex = 0;

    editor.state.doc.content.forEach((node, offset) => {
      ranges.push({
        blockIndex,
        node,
        from: offset,
        to: offset + node.nodeSize,
      });
      blockIndex += 1;
    });

    return ranges;
  }, [editor]);

  const getTextHighlightRangesForBlock = useCallback((blockRange) => {
    if (!blockRange?.node) {
      return [];
    }

    const textRanges = [];

    blockRange.node.descendants((node, pos) => {
      if (!node.isText || !node.text?.trim()) {
        return;
      }

      const from = blockRange.from + pos + 1;
      const to = from + node.nodeSize;

      if (from < to) {
        textRanges.push({ from, to });
      }
    });

    return textRanges;
  }, []);

  const buildAiHighlightRangesFromDescriptors = useCallback((descriptors = []) => {
    if (!editor) {
      return [];
    }

    const blockRanges = getTopLevelBlockRanges();
    const rangesByPosition = new Map();

    descriptors.forEach((descriptor) => {
      const selectedBlockIndexes = new Set(Array.isArray(descriptor?.blockIndexes) ? descriptor.blockIndexes : []);
      const activeBlockIndexes = new Set(Array.isArray(descriptor?.activeBlockIndexes) ? descriptor.activeBlockIndexes : []);
      const tone = descriptor?.tone ? `ai-changed-block--${descriptor.tone}` : '';
      const reviewStatus = descriptor?.reviewStatus ? `ai-changed-block--${descriptor.reviewStatus}` : '';

      blockRanges.forEach((blockRange) => {
        const { blockIndex } = blockRange;

        if (!selectedBlockIndexes.has(blockIndex)) {
          return;
        }

        getTextHighlightRangesForBlock(blockRange).forEach(({ from, to }) => {
          rangesByPosition.set(`${from}:${to}`, {
            from,
            to,
            className: [
              'ai-changed-block',
              tone,
              reviewStatus,
              activeBlockIndexes.has(blockIndex) ? 'is-active' : '',
            ].filter(Boolean).join(' '),
          });
        });
      });
    });

    return Array.from(rangesByPosition.values());
  }, [editor, getTextHighlightRangesForBlock, getTopLevelBlockRanges]);

  useImperativeHandle(ref, () => ({
    scrollToHeading,
    getHTML: () => editor?.getHTML() || '',
    appendContent: (newContent) => {
      if (!editor) {
        return;
      }

      editor.commands.focus('end');
      editor.commands.insertContentAt(editor.state.doc.content.size, newContent);
      editor.commands.normalizeTables?.();
    },
    setContent: (newContent) => {
      if (!editor) {
        return;
      }

      applyExternalContent(newContent);
      lastKnownContentRef.current = editor.getHTML() || newContent || '';
      emitSelectionState(editor);
    },
    insertPageBreak: () => {
      editor?.chain().focus().insertPageBreak().run();
    },
    insertTable: ({ rows = 3, cols = 3, withHeaderRow = true } = {}) => {
      if (!editor) {
        return;
      }

      editor.chain().focus().insertTable({ rows, cols, withHeaderRow }).run();
      editor.commands.normalizeTables?.();
    },
    insertEquation,
    getSelectedText: () => {
      if (!editor) {
        return '';
      }

      const { from, to } = editor.state.selection;

      if (from === to) {
        return '';
      }

      return editor.state.doc.textBetween(from, to, ' ');
    },
    getSelectionRange: () => {
      if (!editor) {
        return null;
      }

      const { from, to } = editor.state.selection;

      if (from === to) {
        return null;
      }

      return { from, to };
    },
    getAiSelectionTargets: () => resolveAiSelectionTargets(editor),
    addAiSelectionFromCurrentSelection: () => {
      if (!editor) {
        return null;
      }

      const { from, to } = editor.state.selection;

      if (from === to) {
        return null;
      }

      const nextRange = {
        id: createAiSelectionId(),
        from,
        to,
      };

      const nextSelections = dispatchAiSelectionMeta({
        type: 'add',
        ranges: [nextRange],
      });

      return nextSelections.find((selection) => selection.id === nextRange.id) || null;
    },
    clearAiSelections: (ids) => dispatchAiSelectionMeta({
      type: 'clear',
      ids: Array.isArray(ids) ? ids : [],
    }),
    setAllAiSelections: () => {
      if (!editor) {
        return [];
      }

      const nextRanges = getTopLevelBlockRanges()
        .filter(({ node }) => node.nodeSize > 2 && node.textContent.trim().length > 0)
        .map(({ from, to }) => ({
          id: createAiSelectionId(),
          from: from + 1,
          to: to - 1,
        }))
        .filter((range) => range.from < range.to);

      return dispatchAiSelectionMeta({
        type: 'set',
        ranges: nextRanges,
      });
    },
    replaceSelection: (html) => {
      if (!editor) {
        return;
      }

      const { from, to } = editor.state.selection;

      if (from === to) {
        return;
      }

      editor.chain().focus().deleteRange({ from, to }).insertContentAt(from, html).run();
      editor.commands.normalizeTables?.();
    },
    insertPlainText: (text) => {
      if (!editor || !text) {
        return;
      }

      editor.chain().focus().insertContent(text).run();
    },
    buildProposal: (nextContent, mode = 'replace', options = {}) => {
      if (!editor) {
        return nextContent;
      }

      const scratchEditor = new CoreEditor({
        element: document.createElement('div'),
        editable: false,
        extensions: scratchExtensions,
        content: editor.getHTML(),
      });

      try {
        if (mode === 'append') {
          scratchEditor.commands.insertContentAt(scratchEditor.state.doc.content.size, nextContent);
        } else if (mode === 'replace_ai_selections') {
          const aiSelectionEdits = Array.isArray(options.selectionEdits) ? options.selectionEdits : [];
          const rangesById = new Map(resolveAiSelectionTargets(editor).map((range) => [range.id, range]));

          aiSelectionEdits
            .map((edit) => ({
              ...edit,
              range: rangesById.get(edit.id),
            }))
            .filter((edit) => edit.range && edit.content)
            .sort((left, right) => right.range.from - left.range.from)
            .forEach((edit) => {
              scratchEditor.chain()
                .setTextSelection({ from: edit.range.from, to: edit.range.to })
                .deleteRange({ from: edit.range.from, to: edit.range.to })
                .insertContentAt(edit.range.from, edit.content)
                .run();
            });
        } else if (mode === 'replace_selection') {
          const { from, to } = editor.state.selection;

          if (from === to) {
            return scratchEditor.getHTML();
          }

          scratchEditor.chain()
            .setTextSelection({ from, to })
            .deleteRange({ from, to })
            .insertContentAt(from, nextContent)
            .run();
        } else {
          scratchEditor.commands.setContent(nextContent, false);
        }

        return scratchEditor.getHTML();
      } finally {
        scratchEditor.destroy();
      }
    },
    captureViewportScroll: () => viewportRef.current?.scrollTop ?? 0,
    restoreViewportScroll: (scrollTop = 0) => {
      if (viewportRef.current) {
        viewportRef.current.scrollTop = scrollTop;
      }
    },
    normalizeTables: () => {
      editor?.commands.normalizeTables?.();
    },
    getEditor: () => editor,
    focusEditor: () => {
      editor?.commands.focus();
    },
    isFocused: () => editor?.isFocused ?? false,
    setAiHighlights: (ranges = []) => {
      dispatchAiHighlightMeta(ranges);
    },
    setAiHighlightsByBlockIndexes: (blockIndexes = [], options = {}) => {
      const ranges = buildAiHighlightRangesFromDescriptors([{
        blockIndexes,
        tone: options.tone,
        activeBlockIndexes: options.activeBlockIndexes,
      }]);

      dispatchAiHighlightMeta(ranges);
    },
    setAiHighlightsByBlockDescriptors: (descriptors = []) => {
      const ranges = buildAiHighlightRangesFromDescriptors(descriptors);
      dispatchAiHighlightMeta(ranges);
    },
    scrollToTopLevelBlock: (targetBlockIndex) => {
      if (!editor || !Number.isInteger(targetBlockIndex)) {
        return;
      }

      const targetRange = getTopLevelBlockRanges().find(({ blockIndex }) => blockIndex === targetBlockIndex);

      if (!targetRange) {
        return;
      }

      const element = editor.view?.nodeDOM(targetRange.from);

      if (element instanceof HTMLElement) {
        element.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'nearest' });
      }
    },
    getTopLevelBlockBounds: (targetBlockIndex) => {
      if (!editor || !Number.isInteger(targetBlockIndex)) {
        return null;
      }

      const targetRange = getTopLevelBlockRanges().find(({ blockIndex }) => blockIndex === targetBlockIndex);

      if (!targetRange) {
        return null;
      }

      const element = editor.view?.nodeDOM(targetRange.from);

      if (!(element instanceof HTMLElement)) {
        return null;
      }

      const { top, right, bottom, left, width, height } = element.getBoundingClientRect();
      return {
        top,
        right,
        bottom,
        left,
        width,
        height,
      };
    },
    getViewportElement: () => viewportRef.current,
    setAiHighlightsFromDiff: (oldHtml, newHtml) => {
      if (!editor) {
        return;
      }

      const parseBlocks = (html) => {
        if (!html) {
          return [];
        }

        const wrapper = document.createElement('div');
        wrapper.innerHTML = html;
        return Array.from(wrapper.children).map((element) => element.textContent.trim());
      };

      const oldBlocks = parseBlocks(oldHtml);
      const newBlocks = parseBlocks(newHtml);
      const oldSet = new Set(oldBlocks.filter((block) => block.length > 0));

      const changedIndices = new Set(
        newBlocks
          .map((block, index) => ({ block, index }))
          .filter(({ block }) => block.length > 0 && !oldSet.has(block))
          .map(({ index }) => index),
      );

      const ranges = buildAiHighlightRangesFromDescriptors([{
        blockIndexes: Array.from(changedIndices),
        tone: 'proposal',
      }]);

      dispatchAiHighlightMeta(ranges);
    },
    clearAiHighlights: () => {
      if (!editor) {
        return;
      }

      dispatchAiHighlightMeta([]);
    },
  }), [
    applyExternalContent,
    dispatchAiHighlightMeta,
    dispatchAiSelectionMeta,
    editor,
    buildAiHighlightRangesFromDescriptors,
    emitSelectionState,
    getTopLevelBlockRanges,
    insertEquation,
    resolveAiSelectionTargets,
    scratchExtensions,
    scrollToHeading,
  ]);

  const effectiveZoom = zoom;
  const scaledWidth = Math.round(paperWidth * effectiveZoom);
  const scaledHeight = Math.round(surfaceHeight * effectiveZoom);
  const paperStyles = {
    '--document-paper-width': `${paperWidth}px`,
    '--document-paper-height': `${paperHeight}px`,
    '--document-padding-x': `${DEFAULT_PAPER_PADDING_X}px`,
    '--document-padding-top': `${DEFAULT_PAPER_PADDING_TOP}px`,
    '--document-padding-bottom': `${DEFAULT_PAPER_PADDING_BOTTOM}px`,
    '--document-body-width': `${bodyWidth}px`,
    fontFamily,
  };

  return (
    <div
      className={`note-editor-content${showLines ? ' show-lines' : ''}${aiSelectionMode ? ' is-ai-selection-mode' : ''}`}
      style={paperStyles}
    >
      <div className="note-editor-canvas-viewport" ref={viewportRef}>
        <div
          className="note-editor-canvas-stage"
          style={{ width: `${scaledWidth}px`, minHeight: `${scaledHeight}px` }}
        >
          <div
            className="note-editor-canvas-scale"
            style={{
              width: `${paperWidth}px`,
              transform: `scale(${effectiveZoom})`,
            }}
          >
            <div className="note-editor-paper-shell">
              <article
                className={`note-editor-paper${readOnly ? ' is-readonly' : ''}${reviewMode ? ' is-review-mode' : ''}`}
                ref={paperRef}
                aria-label={reviewMode ? 'Document review' : readOnly ? 'Document preview' : 'Document editor'}
              >
                <div className="note-editor-flow">
                  <EditorContent editor={editor} />
                  {!readOnly && editor && <TableBubbleMenu editor={editor} zoom={effectiveZoom} />}
                </div>
                {!readOnly && (
                  <div className="note-editor-spacer" onClick={() => editor?.commands.focus('end')} />
                )}
              </article>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
});

export default NoteEditorContent;
