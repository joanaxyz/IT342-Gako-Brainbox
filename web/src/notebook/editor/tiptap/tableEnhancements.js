import { Extension, findParentNodeClosestToPos } from '@tiptap/core';
import { TableView as BaseTableView } from '@tiptap/extension-table';
import { Plugin, PluginKey, TextSelection } from '@tiptap/pm/state';
import { TableMap, columnResizingPluginKey } from '@tiptap/pm/tables';

const TABLE_META = 'brainbox-table-normalized';
const tablePluginKey = new PluginKey('brainboxTableEnhancements');
const tableResizeOverlayKey = new PluginKey('brainboxTableResizeOverlay');

const clamp = (value, min, max) => Math.min(max, Math.max(min, value));

const isFiniteNumber = (value) => Number.isFinite(value) && value > 0;

const normalizeTableAlign = (value) => {
  if (value === 'center' || value === 'right') {
    return value;
  }

  return 'left';
};

const parseTableAlign = (element) => {
  const datasetAlign = element.getAttribute('data-table-align');

  if (datasetAlign) {
    return normalizeTableAlign(datasetAlign);
  }

  const marginLeft = element.style.marginLeft;
  const marginRight = element.style.marginRight;

  if (marginLeft === 'auto' && marginRight === 'auto') {
    return 'center';
  }

  if (marginLeft === 'auto') {
    return 'right';
  }

  return 'left';
};

const parseTableWidth = (element) => {
  const widthValue = Number.parseInt(
    element.getAttribute('data-table-width') || element.style.width || '',
    10,
  );

  return isFiniteNumber(widthValue) ? widthValue : null;
};

const getColumnCount = (tableNode) => {
  const row = tableNode.firstChild;

  if (!row) {
    return 0;
  }

  let total = 0;
  row.forEach((cell) => {
    total += cell.attrs.colspan || 1;
  });

  return total;
};

const getStoredColumnWidths = (tableNode, cellMinWidth) => {
  const widths = [];
  const row = tableNode.firstChild;

  if (!row) {
    return widths;
  }

  row.forEach((cell) => {
    const colspan = cell.attrs.colspan || 1;
    const storedWidths = Array.isArray(cell.attrs.colwidth) ? cell.attrs.colwidth : [];

    for (let index = 0; index < colspan; index += 1) {
      widths.push(Math.max(cellMinWidth, storedWidths[index] || cellMinWidth));
    }
  });

  return widths;
};

const hasExplicitColumnWidths = (tableNode) => {
  const row = tableNode.firstChild;

  if (!row) {
    return false;
  }

  let hasExplicitWidths = false;

  row.forEach((cell) => {
    const storedWidths = Array.isArray(cell.attrs.colwidth) ? cell.attrs.colwidth : [];

    if (storedWidths.some(isFiniteNumber)) {
      hasExplicitWidths = true;
    }
  });

  return hasExplicitWidths;
};

const normalizeColumnWidths = (widths, targetWidth, cellMinWidth) => {
  if (widths.length === 0) {
    return widths;
  }

  const minimumWidth = widths.length * cellMinWidth;
  const safeTargetWidth = Math.max(minimumWidth, targetWidth);
  const currentWidth = widths.reduce((sum, value) => sum + value, 0);
  const baseWidths = currentWidth > 0
    ? widths
    : widths.map(() => Math.max(cellMinWidth, Math.round(safeTargetWidth / widths.length)));

  const scaledWidths = baseWidths.map((width) => Math.max(
    cellMinWidth,
    Math.round((width / Math.max(currentWidth, 1)) * safeTargetWidth),
  ));

  let difference = safeTargetWidth - scaledWidths.reduce((sum, value) => sum + value, 0);
  const order = scaledWidths
    .map((width, index) => ({ width, index }))
    .sort((a, b) => b.width - a.width)
    .map(({ index }) => index);

  let cursor = 0;
  while (difference !== 0 && cursor < safeTargetWidth * 4) {
    const index = order[cursor % order.length];

    if (difference > 0) {
      scaledWidths[index] += 1;
      difference -= 1;
    } else if (scaledWidths[index] > cellMinWidth) {
      scaledWidths[index] -= 1;
      difference += 1;
    }

    cursor += 1;
  }

  return scaledWidths;
};

const getPreviousTableWidth = (tableNode, fallbackWidth) => {
  const storedWidth = isFiniteNumber(tableNode?.attrs?.tableWidth) ? Math.round(tableNode.attrs.tableWidth) : null;
  return storedWidth ?? fallbackWidth;
};

const getPairLockedColumnWidths = (currentWidths, previousWidths, previousTableNode, availableWidth, cellMinWidth) => {
  if (!previousTableNode || currentWidths.length !== previousWidths.length || currentWidths.length === 0) {
    return null;
  }

  const changedColumns = currentWidths
    .map((width, index) => (width !== previousWidths[index] ? index : null))
    .filter((value) => Number.isInteger(value));

  if (changedColumns.length !== 1) {
    return null;
  }

  const changedIndex = changedColumns[0];
  const minimumWidth = currentWidths.length * cellMinWidth;
  const previousTotalWidth = previousWidths.reduce((sum, value) => sum + value, 0);
  const lockedWidth = clamp(
    getPreviousTableWidth(previousTableNode, previousTotalWidth),
    minimumWidth,
    availableWidth,
  );

  if (changedIndex < currentWidths.length - 1) {
    const nextIndex = changedIndex + 1;
    const adjustedWidths = [...currentWidths];
    const delta = adjustedWidths[changedIndex] - previousWidths[changedIndex];

    adjustedWidths[nextIndex] = previousWidths[nextIndex] - delta;

    if (adjustedWidths[nextIndex] < cellMinWidth) {
      const overflow = cellMinWidth - adjustedWidths[nextIndex];
      adjustedWidths[nextIndex] = cellMinWidth;
      adjustedWidths[changedIndex] = Math.max(cellMinWidth, adjustedWidths[changedIndex] - overflow);
    }

    const adjustedTotalWidth = adjustedWidths.reduce((sum, value) => sum + value, 0);
    const difference = lockedWidth - adjustedTotalWidth;

    if (difference !== 0) {
      adjustedWidths[nextIndex] = Math.max(cellMinWidth, adjustedWidths[nextIndex] + difference);
    }

    return {
      targetWidth: lockedWidth,
      widths: adjustedWidths,
    };
  }

  const adjustedWidths = [...currentWidths];
  const currentTotalWidth = adjustedWidths.reduce((sum, value) => sum + value, 0);
  const targetWidth = clamp(currentTotalWidth, minimumWidth, availableWidth);

  if (currentTotalWidth !== targetWidth) {
    adjustedWidths[changedIndex] = Math.max(
      cellMinWidth,
      adjustedWidths[changedIndex] - (currentTotalWidth - targetWidth),
    );
  }

  return {
    targetWidth,
    widths: adjustedWidths,
  };
};

const getLockedPreviewWidths = (baseWidths, changedIndex, desiredWidth, cellMinWidth, lockedWidth) => {
  if (!Array.isArray(baseWidths) || changedIndex < 0 || changedIndex >= baseWidths.length) {
    return null;
  }

  const adjustedWidths = [...baseWidths];
  adjustedWidths[changedIndex] = desiredWidth;

  if (changedIndex < adjustedWidths.length - 1) {
    const nextIndex = changedIndex + 1;
    const delta = adjustedWidths[changedIndex] - baseWidths[changedIndex];

    adjustedWidths[nextIndex] = baseWidths[nextIndex] - delta;

    if (adjustedWidths[nextIndex] < cellMinWidth) {
      const overflow = cellMinWidth - adjustedWidths[nextIndex];
      adjustedWidths[nextIndex] = cellMinWidth;
      adjustedWidths[changedIndex] = Math.max(cellMinWidth, adjustedWidths[changedIndex] - overflow);
    }

    const adjustedTotalWidth = adjustedWidths.reduce((sum, value) => sum + value, 0);
    const difference = lockedWidth - adjustedTotalWidth;

    if (difference !== 0) {
      adjustedWidths[nextIndex] = Math.max(cellMinWidth, adjustedWidths[nextIndex] + difference);
    }

    return {
      widths: adjustedWidths,
      targetWidth: lockedWidth,
    };
  }

  const targetWidth = adjustedWidths.reduce((sum, value) => sum + value, 0);

  return {
    widths: adjustedWidths,
    targetWidth,
  };
};

const applyColumnWidths = (tr, tableNode, tablePos, widths) => {
  const map = TableMap.get(tableNode);
  const firstColumnByCellPos = new Map();

  map.map.forEach((cellPos, index) => {
    if (!firstColumnByCellPos.has(cellPos)) {
      firstColumnByCellPos.set(cellPos, index % map.width);
    }
  });

  firstColumnByCellPos.forEach((leftColumn, relativeCellPos) => {
    const absoluteCellPos = tablePos + relativeCellPos + 1;
    const cellNode = tr.doc.nodeAt(absoluteCellPos);

    if (!cellNode) {
      return;
    }

    const colspan = cellNode.attrs.colspan || 1;
    const nextColWidth = widths.slice(leftColumn, leftColumn + colspan);
    const previousColWidth = Array.isArray(cellNode.attrs.colwidth) ? cellNode.attrs.colwidth : [];

    if (
      nextColWidth.length === previousColWidth.length
      && nextColWidth.every((value, index) => value === previousColWidth[index])
    ) {
      return;
    }

    tr.setNodeMarkup(absoluteCellPos, undefined, {
      ...cellNode.attrs,
      colwidth: nextColWidth,
    });
  });
};

const normalizeTableNode = (tr, tableNode, previousTableNode, tablePos, containerWidth, cellMinWidth) => {
  const columnCount = getColumnCount(tableNode);

  if (columnCount === 0) {
    return false;
  }

  const minimumWidth = columnCount * cellMinWidth;
  const availableWidth = Math.max(minimumWidth, containerWidth || minimumWidth);
  const currentWidths = getStoredColumnWidths(tableNode, cellMinWidth);
  const previousWidths = previousTableNode ? getStoredColumnWidths(previousTableNode, cellMinWidth) : [];
  const currentTotalWidth = currentWidths.reduce((sum, value) => sum + value, 0);
  const storedWidth = isFiniteNumber(tableNode.attrs.tableWidth) ? Math.round(tableNode.attrs.tableWidth) : null;
  const lockedResize = hasExplicitColumnWidths(tableNode)
    ? getPairLockedColumnWidths(
      currentWidths,
      previousWidths,
      previousTableNode,
      availableWidth,
      cellMinWidth,
    )
    : null;
  const targetWidth = lockedResize
    ? lockedResize.targetWidth
    : hasExplicitColumnWidths(tableNode)
      ? clamp(
        currentTotalWidth || storedWidth || minimumWidth,
        minimumWidth,
        availableWidth,
      )
      : clamp(
        storedWidth ?? availableWidth,
        minimumWidth,
        availableWidth,
      );
  const nextWidths = lockedResize?.widths || normalizeColumnWidths(currentWidths, targetWidth, cellMinWidth);
  const nextAlign = normalizeTableAlign(tableNode.attrs.tableAlign);
  const shouldUpdateTable = tableNode.attrs.tableWidth !== targetWidth || tableNode.attrs.tableAlign !== nextAlign;
  const shouldUpdateWidths = nextWidths.some((value, index) => value !== currentWidths[index]);

  if (!shouldUpdateTable && !shouldUpdateWidths) {
    return false;
  }

  tr.setNodeMarkup(tablePos, undefined, {
    ...tableNode.attrs,
    tableWidth: targetWidth,
    tableAlign: nextAlign,
  });

  applyColumnWidths(tr, tableNode, tablePos, nextWidths);
  return true;
};

const ensureParagraphAfterTables = (tr, doc, schema) => {
  const paragraphType = schema.nodes.paragraph;
  const insertionPositions = [];

  if (!paragraphType) {
    return false;
  }

  doc.descendants((node, pos) => {
    if (node.type.name !== 'table') {
      return;
    }

    const endPosition = pos + node.nodeSize;
    const $end = doc.resolve(endPosition);
    const parent = $end.parent;
    const index = $end.index();
    const nextNode = parent.child(index);

    if (nextNode?.type.name === 'paragraph') {
      return;
    }

    if (parent.canReplaceWith(index, index, paragraphType)) {
      insertionPositions.push(endPosition);
    }
  });

  insertionPositions
    .sort((a, b) => b - a)
    .forEach((position) => {
      tr.insert(position, paragraphType.create());
    });

  return insertionPositions.length > 0;
};

const normalizeSelectionAfterTable = (tr) => {
  const { selection } = tr;
  const nodeSelection = selection.node;

  if (nodeSelection?.type?.name === 'table') {
    const nextPosition = Math.min(selection.from + nodeSelection.nodeSize + 1, tr.doc.content.size);
    tr.setSelection(TextSelection.near(tr.doc.resolve(nextPosition), 1));
    return true;
  }

  if (!selection.empty) {
    return false;
  }

  const nodeBefore = selection.$from.nodeBefore;
  const nodeAfter = selection.$from.nodeAfter;

  if (nodeBefore?.type.name === 'table' && nodeAfter?.type.name === 'paragraph') {
    const safePosition = Math.min(selection.from + 1, tr.doc.content.size);
    tr.setSelection(TextSelection.near(tr.doc.resolve(safePosition), 1));
    return true;
  }

  return false;
};

const getContainerWidth = (editor, fallbackWidth, preferredWidth) => {
  if (isFiniteNumber(preferredWidth)) {
    return Math.round(preferredWidth);
  }

  const width = editor?.view?.dom?.clientWidth;
  return isFiniteNumber(width) ? Math.round(width) : fallbackWidth;
};

const normalizeTablesInDoc = (tr, doc, previousDoc, editor, cellMinWidth) => {
  const containerWidth = getContainerWidth(editor, null, editor?.extensionStorage?.tableEnhancements?.containerWidth);
  let didChange = false;

  doc.descendants((node, pos) => {
    if (node.type.name !== 'table') {
      return;
    }

    const previousNode = previousDoc?.nodeAt(pos)?.type?.name === 'table'
      ? previousDoc.nodeAt(pos)
      : null;

    didChange = normalizeTableNode(tr, node, previousNode, pos, containerWidth, cellMinWidth) || didChange;
  });

  return didChange;
};

const getResolvedTableCellElement = (view, pos) => {
  const domNode = view.nodeDOM(pos);

  if (domNode instanceof HTMLElement) {
    return domNode.closest('td, th');
  }

  const domAtPos = view.domAtPos(pos);
  const directChild = domAtPos?.node?.childNodes?.[domAtPos.offset];
  const candidate = directChild instanceof HTMLElement
    ? directChild
    : domAtPos?.node instanceof HTMLElement
      ? domAtPos.node
      : domAtPos?.node?.parentElement;

  return candidate?.closest?.('td, th') || null;
};

const getActiveResizeGuideElements = (view, handlePos) => {
  if (!view || !Number.isInteger(handlePos) || handlePos < 0) {
    return null;
  }

  const cellElement = getResolvedTableCellElement(view, handlePos);

  if (!(cellElement instanceof HTMLElement)) {
    return null;
  }

  const tableElement = cellElement.closest('table');
  const wrapperElement = tableElement?.closest('.brainbox-table-wrapper, .tableWrapper');
  const handleElement = cellElement.querySelector('.column-resize-handle');

  if (!(tableElement instanceof HTMLTableElement) || !(wrapperElement instanceof HTMLElement)) {
    return null;
  }

  return {
    cellElement,
    handleElement: handleElement instanceof HTMLElement ? handleElement : null,
    tableElement,
    wrapperElement,
  };
};

const applyLiveResizePreview = (view, pluginState, cellMinWidth, pointerX) => {
  if (!pluginState?.dragging || !Number.isFinite(pointerX)) {
    return;
  }

  const elements = getActiveResizeGuideElements(view, pluginState.activeHandle);

  if (!elements) {
    return;
  }

  const $cell = view.state.doc.resolve(pluginState.activeHandle);
  const tableNode = $cell.node(-1);
  const map = TableMap.get(tableNode);
  const tableStart = $cell.start(-1);
  const columnIndex = map.colCount($cell.pos - tableStart) + $cell.nodeAfter.attrs.colspan - 1;
  const baseWidths = getStoredColumnWidths(tableNode, cellMinWidth);
  const tableWidth = getPreviousTableWidth(
    tableNode,
    baseWidths.reduce((sum, value) => sum + value, 0),
  );
  const desiredWidth = Math.max(cellMinWidth, pluginState.dragging.startWidth + (pointerX - pluginState.dragging.startX));
  const preview = getLockedPreviewWidths(baseWidths, columnIndex, desiredWidth, cellMinWidth, tableWidth);

  if (!preview) {
    return;
  }

  Array.from(elements.tableElement.querySelectorAll('colgroup > col')).forEach((colElement, index) => {
    if (!(colElement instanceof HTMLTableColElement)) {
      return;
    }

    const nextWidth = preview.widths[index];

    if (Number.isFinite(nextWidth)) {
      colElement.style.width = `${nextWidth}px`;
      colElement.style.minWidth = `${nextWidth}px`;
    }
  });

  elements.tableElement.style.width = `${preview.targetWidth}px`;
  elements.tableElement.style.minWidth = `${preview.targetWidth}px`;
};

const syncResizeGuide = (view, cellMinWidth, pointerX) => {
  const pluginState = columnResizingPluginKey.getState(view.state);

  if (!pluginState || pluginState.activeHandle < 0) {
    return;
  }

  applyLiveResizePreview(view, pluginState, cellMinWidth, pointerX);
};

export class BrainboxTableView extends BaseTableView {
  constructor(node, cellMinWidth, view) {
    super(node, cellMinWidth);
    this.view = view;
    this.dom.classList.add('brainbox-table-wrapper');
    this.table.classList.add('brainbox-table');
    this.applyLayout(node);
  }

  update(node) {
    const didUpdate = super.update(node);

    if (didUpdate) {
      this.applyLayout(node);
    }

    return didUpdate;
  }

  applyLayout(node) {
    const tableAlign = normalizeTableAlign(node.attrs.tableAlign);
    const tableWidth = isFiniteNumber(node.attrs.tableWidth) ? Math.round(node.attrs.tableWidth) : null;

    this.dom.dataset.tableAlign = tableAlign;
    this.dom.style.width = 'max-content';
    this.dom.style.maxWidth = '100%';
    this.dom.style.overflowX = 'auto';
    this.dom.style.overflowY = 'visible';
    this.dom.style.marginLeft = tableAlign === 'left' ? '0' : 'auto';
    this.dom.style.marginRight = tableAlign === 'center' ? 'auto' : '0';
    this.table.dataset.tableAlign = tableAlign;
    this.table.style.tableLayout = 'fixed';
    this.table.style.maxWidth = 'none';
    this.table.style.marginLeft = '0';
    this.table.style.marginRight = '0';

    if (tableWidth) {
      this.table.style.width = `${tableWidth}px`;
      this.table.style.minWidth = `${tableWidth}px`;
    }

    this.table.setAttribute('data-table-width', tableWidth ? String(tableWidth) : '');
  }
}

export const TableEnhancements = Extension.create({
  name: 'tableEnhancements',

  addOptions() {
    return {
      cellMinWidth: 96,
      containerWidth: null,
    };
  },

  onCreate() {
    this.storage.containerWidth = this.options.containerWidth;
  },

  onUpdate() {
    this.storage.containerWidth = this.options.containerWidth;
  },

  addGlobalAttributes() {
    return [
      {
        types: ['table'],
        attributes: {
          tableAlign: {
            default: 'left',
            parseHTML: parseTableAlign,
            renderHTML: (attributes) => ({
              'data-table-align': normalizeTableAlign(attributes.tableAlign),
            }),
          },
          tableWidth: {
            default: null,
            parseHTML: parseTableWidth,
            renderHTML: (attributes) => (
              isFiniteNumber(attributes.tableWidth)
                ? { 'data-table-width': String(Math.round(attributes.tableWidth)) }
                : {}
            ),
          },
        },
      },
    ];
  },

  addCommands() {
    return {
      setTableAlignment:
        (tableAlign = 'left') =>
        ({ state, dispatch }) => {
          const tableNode = findParentNodeClosestToPos(state.selection.$from, (node) => node.type.name === 'table');

          if (!tableNode) {
            return false;
          }

          const nextAttrs = {
            ...tableNode.node.attrs,
            tableAlign: normalizeTableAlign(tableAlign),
          };

          if (dispatch) {
            dispatch(state.tr.setNodeMarkup(tableNode.pos, undefined, nextAttrs));
          }

          return true;
        },

      normalizeTables:
        () =>
        ({ state, dispatch }) => {
          const tr = state.tr;
          const didNormalizeWidths = normalizeTablesInDoc(
            tr,
            state.doc,
            null,
            this.editor,
            this.options.cellMinWidth,
          );
          const didInsertParagraph = ensureParagraphAfterTables(tr, state.doc, state.schema);
          const didNormalizeSelection = normalizeSelectionAfterTable(tr);

          if (!didNormalizeWidths && !didInsertParagraph && !didNormalizeSelection) {
            return false;
          }

          tr.setMeta(TABLE_META, true);

          if (dispatch) {
            dispatch(tr);
          }

          return true;
        },
    };
  },

  addProseMirrorPlugins() {
    return [
      new Plugin({
        key: tablePluginKey,
        appendTransaction: (transactions, _oldState, newState) => {
          if (!transactions.some((transaction) => transaction.docChanged || transaction.selectionSet)) {
            return null;
          }

          if (transactions.some((transaction) => transaction.getMeta(TABLE_META))) {
            return null;
          }

          const tr = newState.tr;
          const didNormalizeWidths = normalizeTablesInDoc(
            tr,
            newState.doc,
            _oldState.doc,
            this.editor,
            this.options.cellMinWidth,
          );
          const didInsertParagraph = ensureParagraphAfterTables(tr, newState.doc, newState.schema);
          const didNormalizeSelection = normalizeSelectionAfterTable(tr);

          if (!didNormalizeWidths && !didInsertParagraph && !didNormalizeSelection) {
            return null;
          }

          tr.setMeta(TABLE_META, true);
          return tr;
        },
      }),
      new Plugin({
        key: tableResizeOverlayKey,
        view: (view) => {
          let animationFrameId = null;
          let lastPointerX = null;

          const sync = (pointerX = lastPointerX) => {
            syncResizeGuide(view, this.options.cellMinWidth, pointerX);
          };
          const scheduleSync = (pointerX) => {
            if (Number.isFinite(pointerX)) {
              lastPointerX = pointerX;
            }

            if (animationFrameId !== null) {
              return;
            }

            animationFrameId = window.requestAnimationFrame(() => {
              animationFrameId = null;
              sync();
            });
          };
          const handleMouseMove = (event) => {
            const pluginState = columnResizingPluginKey.getState(view.state);

            if (!pluginState?.dragging) {
              return;
            }

            scheduleSync(event.clientX);
          };
          const handleViewportChange = () => scheduleSync();

          window.addEventListener('mousemove', handleMouseMove);
          window.addEventListener('scroll', handleViewportChange, true);
          window.addEventListener('resize', handleViewportChange);

          scheduleSync();

          return {
            update: () => scheduleSync(),
            destroy: () => {
              if (animationFrameId !== null) {
                window.cancelAnimationFrame(animationFrameId);
              }

              window.removeEventListener('mousemove', handleMouseMove);
              window.removeEventListener('scroll', handleViewportChange, true);
              window.removeEventListener('resize', handleViewportChange);
            },
          };
        },
      }),
    ];
  },
});
