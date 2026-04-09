import { Extension, Node, mergeAttributes } from '@tiptap/core';
import { Table, TableCell, TableHeader, TableRow } from '@tiptap/extension-table';
import Highlight from '@tiptap/extension-highlight';
import Link from '@tiptap/extension-link';
import Subscript from '@tiptap/extension-subscript';
import Superscript from '@tiptap/extension-superscript';
import TaskItem from '@tiptap/extension-task-item';
import TaskList from '@tiptap/extension-task-list';
import TextAlign from '@tiptap/extension-text-align';
import { TextStyle } from '@tiptap/extension-text-style';
import Underline from '@tiptap/extension-underline';
import StarterKit from '@tiptap/starter-kit';
import { Plugin, PluginKey, TextSelection } from '@tiptap/pm/state';
import { Decoration, DecorationSet } from '@tiptap/pm/view';
import { BlockMath, InlineMath } from './mathExtensions';
import { BrainboxTableView, TableEnhancements } from './tableEnhancements';

export const aiHighlightKey = new PluginKey('aiHighlight');
export const aiSelectionHighlightKey = new PluginKey('aiSelectionHighlight');

const mapSelectionRanges = (ranges, tr) => ranges
  .map((range) => {
    const from = tr.docChanged ? tr.mapping.map(range.from, -1) : range.from;
    const to = tr.docChanged ? tr.mapping.map(range.to, 1) : range.to;

    if (!Number.isFinite(from) || !Number.isFinite(to) || from >= to) {
      return null;
    }

    return { ...range, from, to };
  })
  .filter(Boolean);

const rangesOverlap = (first, second) => first.from < second.to && second.from < first.to;

const mergeSelectionRanges = (existingRanges, nextRanges) => {
  const incomingRanges = nextRanges
    .filter((range) => Number.isFinite(range?.from) && Number.isFinite(range?.to) && range.from < range.to)
    .map((range) => ({ ...range }));

  if (incomingRanges.length === 0) {
    return existingRanges;
  }

  const remainingRanges = existingRanges.filter(
    (currentRange) => !incomingRanges.some((incomingRange) => rangesOverlap(currentRange, incomingRange)),
  );

  return [...remainingRanges, ...incomingRanges].sort((left, right) => left.from - right.from);
};

export const getAiSelectionRanges = (state) => aiSelectionHighlightKey.getState(state) || [];

export const AiHighlight = Extension.create({
  name: 'aiHighlight',
  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: aiHighlightKey,
        state: {
          init: () => [],
          apply(tr, previousRanges) {
            const nextRanges = tr.getMeta(aiHighlightKey);
            return nextRanges !== undefined ? nextRanges : previousRanges;
          },
        },
        props: {
          decorations(state) {
            const ranges = aiHighlightKey.getState(state);

            if (!ranges || ranges.length === 0) {
              return DecorationSet.empty;
            }

            const decorations = ranges.map(({ from, to, className, attributes = {} }) => Decoration.inline(
              from,
              to,
              {
                class: className || 'ai-changed-block',
                ...attributes,
              },
            ));

            return DecorationSet.create(state.doc, decorations);
          },
        },
      }),
    ];
  },
});

export const AiSelectionHighlight = Extension.create({
  name: 'aiSelectionHighlight',
  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: aiSelectionHighlightKey,
        state: {
          init: () => [],
          apply(tr, previousRanges) {
            const mappedRanges = mapSelectionRanges(previousRanges, tr);
            const meta = tr.getMeta(aiSelectionHighlightKey);

            if (!meta) {
              return mappedRanges;
            }

            if (meta.type === 'clear') {
              if (!Array.isArray(meta.ids) || meta.ids.length === 0) {
                return [];
              }

              return mappedRanges.filter((range) => !meta.ids.includes(range.id));
            }

            if (meta.type === 'set') {
              return mergeSelectionRanges([], Array.isArray(meta.ranges) ? meta.ranges : []);
            }

            if (meta.type === 'add') {
              return mergeSelectionRanges(mappedRanges, Array.isArray(meta.ranges) ? meta.ranges : []);
            }

            return mappedRanges;
          },
        },
        props: {
          decorations(state) {
            const ranges = getAiSelectionRanges(state);

            if (ranges.length === 0) {
              return DecorationSet.empty;
            }

            const decorations = ranges.map(({ from, to, id }) => Decoration.inline(
              from,
              to,
              {
                class: 'ai-selection-highlight',
                'data-ai-selection-id': id,
              },
              { id },
            ));

            return DecorationSet.create(state.doc, decorations);
          },
        },
      }),
    ];
  },
});

export const FontSize = Extension.create({
  name: 'fontSize',

  addGlobalAttributes() {
    return [
      {
        types: ['textStyle'],
        attributes: {
          fontSize: {
            default: null,
            parseHTML: (element) => element.style.fontSize || null,
            renderHTML: (attributes) => {
              if (!attributes.fontSize) {
                return {};
              }

              return {
                style: `font-size: ${attributes.fontSize}`,
              };
            },
          },
        },
      },
    ];
  },

  addCommands() {
    return {
      setFontSize:
        (size) =>
        ({ chain }) => chain().setMark('textStyle', { fontSize: size }).run(),
      unsetFontSize:
        () =>
        ({ chain }) => chain().setMark('textStyle', { fontSize: null }).removeEmptyTextStyle().run(),
    };
  },
});

const createPageBreakIcon = (className) => {
  const icon = document.createElement('span');
  icon.className = className;
  icon.setAttribute('aria-hidden', 'true');
  return icon;
};

export const PageBreak = Node.create({
  name: 'pageBreak',
  group: 'block',
  atom: true,
  selectable: false,
  isolating: true,

  parseHTML() {
    return [{ tag: 'div[data-page-break="true"]' }];
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', mergeAttributes(HTMLAttributes, { 'data-page-break': 'true' })];
  },

  addCommands() {
    return {
      insertPageBreak:
        () =>
        ({ state, dispatch }) => {
          const { from, to } = state.selection;
          const paragraphType = state.schema.nodes.paragraph;

          if (!paragraphType) {
            return false;
          }

          const tr = state.tr.replaceRangeWith(from, to, this.type.create());
          const paragraphPosition = from + this.type.create().nodeSize;

          tr.insert(paragraphPosition, paragraphType.create());
          tr.setSelection(TextSelection.near(tr.doc.resolve(paragraphPosition + 1)));

          if (dispatch) {
            dispatch(tr.scrollIntoView());
          }

          return true;
        },
    };
  },

  addNodeView() {
    return () => {
      const dom = document.createElement('div');
      dom.className = 'note-editor-page-break';
      dom.dataset.pageBreakMarker = 'true';
      dom.contentEditable = 'false';
      dom.setAttribute('aria-label', 'Export page break');
      dom.setAttribute('title', 'Export page break');

      const leftIcon = createPageBreakIcon('note-editor-page-break-icon note-editor-page-break-icon--left');
      const line = document.createElement('span');
      line.className = 'note-editor-page-break-line';
      line.setAttribute('aria-hidden', 'true');
      const rightIcon = createPageBreakIcon('note-editor-page-break-icon note-editor-page-break-icon--right');

      dom.append(leftIcon, line, rightIcon);

      return { dom };
    };
  },
});

export const createEditorExtensions = ({
  enableAiHighlight = true,
  enableAiSelectionHighlight = true,
  enableTableNormalization = true,
  cellMinWidth = 96,
  tableContainerWidth = null,
} = {}) => {
  const extensions = [
    StarterKit,
    Underline,
    Link.configure({
      openOnClick: false,
      HTMLAttributes: { class: 'editor-link' },
    }),
    TextAlign.configure({
      types: ['heading', 'paragraph'],
    }),
    Highlight.configure({ multicolor: true }),
    Table.configure({
      resizable: true,
      cellMinWidth,
      View: BrainboxTableView,
      HTMLAttributes: {
        class: 'brainbox-table',
      },
    }),
    TableRow,
    TableCell,
    TableHeader,
    Superscript,
    Subscript,
    TaskList,
    TaskItem.configure({ nested: true }),
    TextStyle,
    FontSize,
    BlockMath,
    InlineMath,
    PageBreak,
  ];

  if (enableTableNormalization) {
    extensions.push(TableEnhancements.configure({
      cellMinWidth,
      containerWidth: tableContainerWidth,
    }));
  }

  if (enableAiHighlight) {
    extensions.push(AiHighlight);
  }

  if (enableAiSelectionHighlight) {
    extensions.push(AiSelectionHighlight);
  }

  return extensions;
};
