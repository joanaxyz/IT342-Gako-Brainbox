import { InputRule, mergeAttributes, Node } from '@tiptap/core';
import katex from 'katex';

const focusSoon = (element) => {
  window.requestAnimationFrame(() => {
    element?.focus?.();
    element?.select?.();
  });
};

const renderFormula = (element, latex, katexOptions, { displayMode = false } = {}) => {
  if (!element) {
    return;
  }

  if (!latex.trim()) {
    element.textContent = displayMode ? 'Display formula' : 'Inline formula';
    return;
  }

  try {
    katex.render(latex, element, {
      displayMode,
      throwOnError: false,
      ...katexOptions,
    });
  } catch {
    element.textContent = latex;
  }
};

const createMathNodeView = ({
  editor,
  node,
  getPos,
  katexOptions,
  displayMode = false,
}) => {
  const wrapper = document.createElement(displayMode ? 'div' : 'span');
  const preview = document.createElement(displayMode ? 'div' : 'span');
  const editorWrap = document.createElement('span');
  const input = displayMode ? document.createElement('textarea') : document.createElement('input');
  let currentNode = node;
  let currentLatex = node.attrs.latex ?? '';
  let isEditing = editor.isEditable && !currentLatex.trim();

  wrapper.className = `brainbox-math-node brainbox-math-node--${displayMode ? 'block' : 'inline'}`;
  wrapper.dataset.type = displayMode ? 'block-math' : 'inline-math';
  wrapper.contentEditable = 'false';

  preview.className = 'brainbox-math-node-preview';
  editorWrap.className = 'brainbox-math-node-editor';
  input.className = 'brainbox-math-node-input';
  input.setAttribute('spellcheck', 'false');
  input.setAttribute('autocomplete', 'off');
  input.setAttribute('aria-label', displayMode ? 'Edit display formula' : 'Edit inline formula');
  input.placeholder = displayMode ? 'Type LaTeX for a display formula' : 'Type LaTeX';

  if (displayMode) {
    input.rows = 3;
  } else {
    input.type = 'text';
  }

  editorWrap.appendChild(input);
  wrapper.append(preview, editorWrap);

  const getPosition = () => {
    const position = getPos?.();
    return typeof position === 'number' ? position : null;
  };

  const focusAfterNode = () => {
    const position = getPosition();
    if (position == null) {
      return;
    }

    const resolvedPos = Math.min(position + currentNode.nodeSize, editor.state.doc.content.size);
    window.requestAnimationFrame(() => {
      editor.commands.focus(resolvedPos);
    });
  };

  const removeNode = () => {
    const position = getPosition();
    if (position == null) {
      return;
    }

    editor.chain().focus().deleteRange({
      from: position,
      to: position + currentNode.nodeSize,
    }).run();
  };

  const commit = () => {
    const nextLatex = input.value.trim();
    const position = getPosition();

    if (position == null) {
      return;
    }

    if (!nextLatex) {
      removeNode();
      return;
    }

    const command = displayMode ? editor.commands.updateBlockMath : editor.commands.updateInlineMath;

    if (!command?.({ pos: position, latex: nextLatex })) {
      return;
    }

    currentLatex = nextLatex;
    isEditing = false;
    sync();
    focusAfterNode();
  };

  const cancel = () => {
    if (!currentLatex.trim()) {
      removeNode();
      return;
    }

    input.value = currentLatex;
    isEditing = false;
    sync();
    focusAfterNode();
  };

  const openEditor = (event) => {
    if (!editor.isEditable) {
      return;
    }

    event.preventDefault();
    event.stopPropagation();
    isEditing = true;
    sync();
  };

  const sync = () => {
    wrapper.classList.toggle('is-editing', isEditing);
    wrapper.classList.toggle('is-empty', !currentLatex.trim());
    wrapper.setAttribute('data-latex', currentLatex);

    if (isEditing) {
      input.value = currentLatex;
      renderFormula(preview, currentLatex, katexOptions, { displayMode });
      focusSoon(input);
      return;
    }

    renderFormula(preview, currentLatex, katexOptions, { displayMode });
  };

  preview.addEventListener('click', openEditor);
  input.addEventListener('blur', () => {
    // Delay commit to check if focus moved to another element within the wrapper
    // (e.g. ProseMirror transaction causing a temporary blur). If focus returns
    // to the input or stays in the wrapper, don't commit.
    setTimeout(() => {
      if (document.activeElement === input || wrapper.contains(document.activeElement)) {
        return;
      }
      commit();
    }, 150);
  });
  input.addEventListener('click', (event) => {
    event.stopPropagation();
  });
  input.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      event.preventDefault();
      cancel();
      return;
    }

    if (event.key === 'Enter' && (!displayMode || !event.shiftKey)) {
      event.preventDefault();
      commit();
    }
  });

  sync();

  return {
    dom: wrapper,
    update(updatedNode) {
      if (updatedNode.type.name !== currentNode.type.name) {
        return false;
      }

      currentNode = updatedNode;
      currentLatex = updatedNode.attrs.latex ?? '';
      sync();
      return true;
    },
    destroy() {
      preview.removeEventListener('click', openEditor);
    },
  };
};

export const InlineMath = Node.create({
  name: 'inlineMath',

  group: 'inline',

  inline: true,

  atom: true,

  addOptions() {
    return {
      katexOptions: undefined,
    };
  },

  addAttributes() {
    return {
      latex: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-latex'),
        renderHTML: (attributes) => ({
          'data-latex': attributes.latex,
        }),
      },
    };
  },

  addCommands() {
    return {
      insertInlineMath:
        (options) =>
        ({ editor, tr }) => {
          const latex = options?.latex ?? '';
          const from = options?.pos ?? editor.state.selection.from;

          tr.replaceWith(from, from, this.type.create({ latex }));
          return true;
        },
      deleteInlineMath:
        (options) =>
        ({ editor, tr }) => {
          const position = options?.pos ?? editor.state.selection.$from.pos;
          const currentNode = editor.state.doc.nodeAt(position);

          if (!currentNode || currentNode.type.name !== this.name) {
            return false;
          }

          tr.delete(position, position + currentNode.nodeSize);
          return true;
        },
      updateInlineMath:
        (options) =>
        ({ editor, tr }) => {
          const latex = options?.latex ?? '';
          const position = options?.pos ?? editor.state.selection.$from.pos;
          const currentNode = editor.state.doc.nodeAt(position);

          if (!currentNode || currentNode.type.name !== this.name) {
            return false;
          }

          tr.setNodeMarkup(position, this.type, {
            ...currentNode.attrs,
            latex,
          });
          return true;
        },
    };
  },

  parseHTML() {
    return [
      {
        tag: 'span[data-type="inline-math"]',
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ['span', mergeAttributes(HTMLAttributes, { 'data-type': 'inline-math' })];
  },

  addInputRules() {
    return [
      new InputRule({
        find: /(?<!\$)(\$\$([^$\n]+?)\$\$)(?!\$)/,
        handler: ({ state, range, match }) => {
          const latex = match[2];
          state.tr.replaceWith(range.from, range.to, this.type.create({ latex }));
        },
      }),
    ];
  },

  addNodeView() {
    const { katexOptions } = this.options;

    return (props) =>
      createMathNodeView({
        ...props,
        editor: this.editor,
        katexOptions,
        displayMode: false,
      });
  },
});

export const BlockMath = Node.create({
  name: 'blockMath',

  group: 'block',

  atom: true,

  addOptions() {
    return {
      katexOptions: undefined,
    };
  },

  addAttributes() {
    return {
      latex: {
        default: '',
        parseHTML: (element) => element.getAttribute('data-latex'),
        renderHTML: (attributes) => ({
          'data-latex': attributes.latex,
        }),
      },
    };
  },

  addCommands() {
    return {
      insertBlockMath:
        (options) =>
        ({ commands, editor }) => {
          const latex = options?.latex ?? '';
          return commands.insertContentAt(options?.pos ?? editor.state.selection.from, {
            type: this.name,
            attrs: { latex },
          });
        },
      deleteBlockMath:
        (options) =>
        ({ editor, tr }) => {
          const position = options?.pos ?? editor.state.selection.$from.pos;
          const currentNode = editor.state.doc.nodeAt(position);

          if (!currentNode || currentNode.type.name !== this.name) {
            return false;
          }

          tr.delete(position, position + currentNode.nodeSize);
          return true;
        },
      updateBlockMath:
        (options) =>
        ({ editor, tr }) => {
          const latex = options?.latex ?? '';
          const position = options?.pos ?? editor.state.selection.$from.pos;
          const currentNode = editor.state.doc.nodeAt(position);

          if (!currentNode || currentNode.type.name !== this.name) {
            return false;
          }

          tr.setNodeMarkup(position, this.type, {
            ...currentNode.attrs,
            latex,
          });
          return true;
        },
    };
  },

  parseHTML() {
    return [
      {
        tag: 'div[data-type="block-math"]',
      },
    ];
  },

  renderHTML({ HTMLAttributes }) {
    return ['div', mergeAttributes(HTMLAttributes, { 'data-type': 'block-math' })];
  },

  addInputRules() {
    return [
      new InputRule({
        find: /^\$\$\$([^$]+)\$\$\$$/,
        handler: ({ state, range, match }) => {
          const [, latex] = match;
          state.tr.replaceWith(range.from, range.to, this.type.create({ latex }));
        },
      }),
    ];
  },

  addNodeView() {
    const { katexOptions } = this.options;

    return (props) =>
      createMathNodeView({
        ...props,
        editor: this.editor,
        katexOptions,
        displayMode: true,
      });
  },
});
