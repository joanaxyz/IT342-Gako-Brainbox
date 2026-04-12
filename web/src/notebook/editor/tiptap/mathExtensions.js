import 'katex/dist/katex.min.css';
import { initVirtualKeyboardInCurrentBrowsingContext } from 'mathlive';
import 'mathlive/fonts.css';
import { InputRule, mergeAttributes, Node } from '@tiptap/core';
import katex from 'katex';

if (typeof window !== 'undefined') {
  initVirtualKeyboardInCurrentBrowsingContext();
}

const focusSoon = (element) => {
  customElements.whenDefined('math-field').then(() => {
    window.requestAnimationFrame(() => {
      window.requestAnimationFrame(() => {
        if (!element?.isConnected) {
          return;
        }

        try {
          element.focus?.();
        } catch {
          // MathLive can throw during early lifecycle focus; let the user focus manually.
        }
      });
    });
  });
};

const renderEquation = (element, latex, katexOptions, { displayMode = false } = {}) => {
  if (!element) {
    return;
  }

  if (!latex.trim()) {
    element.textContent = '';
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

// ── Shared equation toolbar (singleton, anchored below editor toolbar) ──
const SYMBOL_CATEGORIES = [
  {
    label: 'αβΔ', title: 'Greek letters',
    symbols: [
      { label: 'α', latex: '\\alpha' }, { label: 'β', latex: '\\beta' },
      { label: 'γ', latex: '\\gamma' }, { label: 'δ', latex: '\\delta' },
      { label: 'ε', latex: '\\epsilon' }, { label: 'ζ', latex: '\\zeta' },
      { label: 'η', latex: '\\eta' }, { label: 'θ', latex: '\\theta' },
      { label: 'λ', latex: '\\lambda' }, { label: 'μ', latex: '\\mu' },
      { label: 'π', latex: '\\pi' }, { label: 'ρ', latex: '\\rho' },
      { label: 'σ', latex: '\\sigma' }, { label: 'τ', latex: '\\tau' },
      { label: 'φ', latex: '\\phi' }, { label: 'ψ', latex: '\\psi' },
      { label: 'ω', latex: '\\omega' }, { label: 'Γ', latex: '\\Gamma' },
      { label: 'Δ', latex: '\\Delta' }, { label: 'Θ', latex: '\\Theta' },
      { label: 'Λ', latex: '\\Lambda' }, { label: 'Π', latex: '\\Pi' },
      { label: 'Σ', latex: '\\Sigma' }, { label: 'Φ', latex: '\\Phi' },
      { label: 'Ω', latex: '\\Omega' },
    ],
  },
  {
    label: '×÷≥', title: 'Math operators',
    symbols: [
      { label: '×', latex: '\\times' }, { label: '÷', latex: '\\div' },
      { label: '±', latex: '\\pm' }, { label: '∓', latex: '\\mp' },
      { label: '·', latex: '\\cdot' }, { label: '∘', latex: '\\circ' },
      { label: '∗', latex: '\\ast' }, { label: '⊕', latex: '\\oplus' },
      { label: '⊗', latex: '\\otimes' },
    ],
  },
  {
    label: '≤≠≥', title: 'Relations',
    symbols: [
      { label: '≤', latex: '\\leq' }, { label: '≥', latex: '\\geq' },
      { label: '≠', latex: '\\neq' }, { label: '≈', latex: '\\approx' },
      { label: '≡', latex: '\\equiv' }, { label: '∝', latex: '\\propto' },
      { label: '∈', latex: '\\in' }, { label: '∉', latex: '\\notin' },
      { label: '⊂', latex: '\\subset' }, { label: '⊃', latex: '\\supset' },
      { label: '⊆', latex: '\\subseteq' }, { label: '⊇', latex: '\\supseteq' },
      { label: '∪', latex: '\\cup' }, { label: '∩', latex: '\\cap' },
    ],
  },
  {
    label: '√x²', title: 'Structures',
    symbols: [
      { label: 'x²', latex: 'superscript', insert: '^{#?}' },
      { label: 'x₂', latex: 'subscript', insert: '_{#?}' },
      { label: '√', latex: '\\sqrt{}', insert: '\\sqrt{#?}' },
      { label: '∛', latex: '\\sqrt[3]{}', insert: '\\sqrt[3]{#?}' },
      { label: 'a/b', latex: '\\frac{}{}', insert: '\\frac{#?}{#?}' },
      { label: '()', latex: '\\left(\\right)', insert: '\\left(#?\\right)' },
      { label: '[]', latex: '\\left[\\right]', insert: '\\left[#?\\right]' },
      { label: '{}', latex: '\\left\\{\\right\\}', insert: '\\left\\{#?\\right\\}' },
      { label: '||', latex: '\\left|\\right|', insert: '\\left|#?\\right|' },
    ],
  },
  {
    label: '→∑∫', title: 'Arrows & big operators',
    symbols: [
      { label: '→', latex: '\\rightarrow' }, { label: '←', latex: '\\leftarrow' },
      { label: '↔', latex: '\\leftrightarrow' }, { label: '⇒', latex: '\\Rightarrow' },
      { label: '⇐', latex: '\\Leftarrow' }, { label: '⇔', latex: '\\Leftrightarrow' },
      { label: '↑', latex: '\\uparrow' }, { label: '↓', latex: '\\downarrow' },
      { label: '∑', latex: '\\sum', insert: '\\sum_{#?}^{#?}' }, { label: '∏', latex: '\\prod', insert: '\\prod_{#?}^{#?}' },
      { label: '∫', latex: '\\int', insert: '\\int_{#?}^{#?}' }, { label: '∬', latex: '\\iint' },
      { label: '∞', latex: '\\infty' }, { label: '∂', latex: '\\partial' },
      { label: '∇', latex: '\\nabla' }, { label: '…', latex: '\\ldots' },
    ],
  },
];

let equationToolbarInstance = null;

const createMathfieldElement = (displayMode) => {
  const mathfield = document.createElement('math-field');
  mathfield.className = 'brainbox-mathfield';
  mathfield.setAttribute('aria-label', displayMode ? 'Edit display equation' : 'Edit inline equation');
  mathfield.setAttribute('smart-fence', '');
  mathfield.setAttribute('smart-superscript', '');
  mathfield.setAttribute('default-mode', 'math');
  mathfield.mathVirtualKeyboardPolicy = 'manual';
  mathfield.style.border = 'none';
  mathfield.style.outline = 'none';
  mathfield.style.background = 'transparent';
  mathfield.style.padding = '0';
  mathfield.style.margin = '0';
  return mathfield;
};

const getEquationToolbar = () => {
  if (equationToolbarInstance) return equationToolbarInstance;

  const bar = document.createElement('div');
  bar.className = 'brainbox-eq-toolbar';
  let activeField = null;
  let onChangeCb = null;
  let openDropdown = null;

  const syncKeyboardButtonState = () => {
    const isVisible = Boolean(window.mathVirtualKeyboard?.visible);
    keyboardButton.classList.toggle('is-active', isVisible);
    keyboardButton.setAttribute('aria-pressed', isVisible ? 'true' : 'false');
  };

  const closeDropdowns = () => {
    if (openDropdown) {
      openDropdown.classList.remove('is-open');
      openDropdown = null;
    }
  };

  const insertIntoField = (symbol) => {
    if (!activeField) return;
    const template = symbol.insert ?? symbol.latex;
    activeField.insert(template, {
      format: 'latex',
      selectionMode: template.includes('#?') ? 'placeholder' : 'after',
      focus: true,
      scrollIntoView: false,
    });
    activeField.focus();
    onChangeCb?.();
  };

  // "New equation" label
  const label = document.createElement('span');
  label.className = 'brainbox-eq-toolbar-label';
  label.textContent = 'New equation';
  bar.appendChild(label);

  const keyboardButton = document.createElement('button');
  keyboardButton.type = 'button';
  keyboardButton.className = 'brainbox-eq-toolbar-btn brainbox-eq-toolbar-btn--control brainbox-eq-toolbar-btn--icon';
  keyboardButton.textContent = '⌨';
  keyboardButton.title = 'Toggle MathLive virtual keyboard';
  keyboardButton.setAttribute('aria-label', 'Toggle MathLive virtual keyboard');
  keyboardButton.tabIndex = -1;
  keyboardButton.setAttribute('aria-pressed', 'false');
  keyboardButton.addEventListener('mousedown', (e) => {
    e.preventDefault();
    e.stopPropagation();
    closeDropdowns();
    if (!activeField) {
      return;
    }

    activeField.focus();
    activeField.executeCommand(window.mathVirtualKeyboard?.visible ? 'hideVirtualKeyboard' : 'showVirtualKeyboard');
    syncKeyboardButtonState();
  });
  bar.appendChild(keyboardButton);

  if (window.mathVirtualKeyboard) {
    window.mathVirtualKeyboard.addEventListener('virtual-keyboard-toggle', syncKeyboardButtonState);
  }

  for (const category of SYMBOL_CATEGORIES) {
    const group = document.createElement('span');
    group.className = 'brainbox-eq-toolbar-group';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'brainbox-eq-toolbar-btn';
    btn.textContent = category.label + ' ▾';
    btn.title = category.title;
    btn.tabIndex = -1;

    const dropdown = document.createElement('div');
    dropdown.className = 'brainbox-eq-toolbar-dropdown';

    for (const sym of category.symbols) {
      const symBtn = document.createElement('button');
      symBtn.type = 'button';
      symBtn.className = 'brainbox-eq-toolbar-sym';
      symBtn.textContent = sym.label;
      symBtn.title = sym.insert ?? sym.latex;
      symBtn.tabIndex = -1;
      symBtn.addEventListener('mousedown', (e) => {
        e.preventDefault();
        e.stopPropagation();
        insertIntoField(sym);
        closeDropdowns();
      });
      dropdown.appendChild(symBtn);
    }

    btn.addEventListener('mousedown', (e) => {
      e.preventDefault();
      e.stopPropagation();
      if (openDropdown === dropdown) { closeDropdowns(); return; }
      closeDropdowns();
      dropdown.classList.add('is-open');
      openDropdown = dropdown;
    });

    group.append(btn, dropdown);
    bar.appendChild(group);
  }

  equationToolbarInstance = {
    element: bar,
    show(fieldEl, onChange) {
      activeField = fieldEl;
      onChangeCb = onChange;
      const anchor = document.querySelector('.editor-toolbar-shell');
      if (anchor && !anchor.contains(bar)) {
        anchor.appendChild(bar);
      }
      bar.style.display = '';
      syncKeyboardButtonState();
    },
    hide() {
      activeField = null;
      onChangeCb = null;
      closeDropdowns();
      if (window.mathVirtualKeyboard?.visible) {
        window.mathVirtualKeyboard.hide({ animate: true });
      }
      syncKeyboardButtonState();
      bar.style.display = 'none';
    },
    closeDropdowns,
  };

  return equationToolbarInstance;
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
  const mathfield = createMathfieldElement(displayMode);
  let currentNode = node;
  let currentLatex = node.attrs.latex ?? '';
  let isEditing = editor.isEditable && !currentLatex.trim();

  wrapper.className = `brainbox-math-node brainbox-math-node--${displayMode ? 'block' : 'inline'}`;
  wrapper.dataset.type = displayMode ? 'block-math' : 'inline-math';
  wrapper.contentEditable = 'false';

  preview.className = 'brainbox-math-node-preview';
  editorWrap.className = 'brainbox-math-node-editor';
  editorWrap.append(mathfield);
  wrapper.append(preview, editorWrap);

  const getPosition = () => {
    const position = getPos?.();
    return typeof position === 'number' ? position : null;
  };

  const focusAfterNode = () => {
    const position = getPosition();
    if (position == null) return;
    const resolvedPos = Math.min(position + currentNode.nodeSize, editor.state.doc.content.size);
    window.requestAnimationFrame(() => editor.commands.focus(resolvedPos));
  };

  const removeNode = () => {
    const position = getPosition();
    if (position == null) return;
    editor.chain().focus().deleteRange({
      from: position,
      to: position + currentNode.nodeSize,
    }).run();
  };

  const syncDraftAppearance = () => {
    const draft = mathfield.getValue().trim();
    wrapper.classList.toggle('is-empty', !draft);
  };

  const commit = () => {
    const nextLatex = mathfield.getValue().trim();
    const position = getPosition();

    if (position == null) return;

    if (!nextLatex) {
      removeNode();
      return;
    }

    const command = displayMode ? editor.commands.updateBlockMath : editor.commands.updateInlineMath;
    if (!command?.({ pos: position, latex: nextLatex })) return;

    currentLatex = nextLatex;
    isEditing = false;
    getEquationToolbar().hide();
    sync();
    focusAfterNode();
  };

  const cancel = () => {
    if (!currentLatex.trim()) {
      removeNode();
      return;
    }

    mathfield.setValue(currentLatex);
    isEditing = false;
    getEquationToolbar().hide();
    sync();
    focusAfterNode();
  };

  const openEditor = (event) => {
    if (!editor.isEditable) return;
    event.preventDefault();
    event.stopPropagation();
    isEditing = true;
    sync();
  };

  const sync = () => {
    wrapper.classList.toggle('is-editing', isEditing);
    const hasLatex = currentLatex.trim().length > 0;
    wrapper.classList.toggle('is-empty', !hasLatex);

    if (isEditing) {
      mathfield.setValue(currentLatex);
      syncDraftAppearance();
      getEquationToolbar().show(mathfield, () => syncDraftAppearance());
      focusSoon(mathfield);
      return;
    }

    getEquationToolbar().hide();
    renderEquation(preview, currentLatex, katexOptions, { displayMode });
  };

  preview.addEventListener('click', openEditor);
  editorWrap.addEventListener('mousedown', (e) => {
    e.stopPropagation();
  });
  editorWrap.addEventListener('click', (e) => {
    e.stopPropagation();
    if (isEditing) {
      mathfield.focus();
    }
  });

  const stopBubble = (e) => e.stopPropagation();
  mathfield.addEventListener('beforeinput', stopBubble);
  mathfield.addEventListener('input', (e) => {
    e.stopPropagation();
    syncDraftAppearance();
  });
  mathfield.addEventListener('change', stopBubble);
  mathfield.addEventListener('selection-change', stopBubble);
  mathfield.addEventListener('pointerdown', stopBubble);
  mathfield.addEventListener('mousedown', stopBubble);
  mathfield.addEventListener('click', (e) => {
    e.stopPropagation();
    getEquationToolbar().closeDropdowns();
  });
  mathfield.addEventListener('blur', () => {
    setTimeout(() => {
      const active = document.activeElement;
      if (active === mathfield || wrapper.contains(active) || getEquationToolbar().element.contains(active)) return;
      commit();
    }, 150);
  });
  mathfield.addEventListener('keydown', (e) => {
    e.stopPropagation();
    if (e.key === 'Escape') {
      e.preventDefault();
      cancel();
      return;
    }
    if (displayMode) {
      if (e.key === 'Enter' && e.shiftKey) { e.preventDefault(); commit(); }
    } else {
      if (e.key === 'Enter') { e.preventDefault(); commit(); }
    }
  });

  sync();

  return {
    dom: wrapper,
    update(updatedNode) {
      if (updatedNode.type.name !== currentNode.type.name) return false;
      currentNode = updatedNode;
      currentLatex = updatedNode.attrs.latex ?? '';
      if (!isEditing) sync();
      return true;
    },
    destroy() {
      preview.removeEventListener('click', openEditor);
      if (isEditing) getEquationToolbar().hide();
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
          const from = options?.range?.from ?? options?.pos ?? editor.state.selection.from;
          const to = options?.range?.to ?? editor.state.selection.to ?? from;

          tr.replaceRangeWith(from, to, this.type.create({ latex }));
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
          const from = options?.range?.from ?? options?.pos ?? editor.state.selection.from;
          const to = options?.range?.to ?? editor.state.selection.to ?? from;

          return commands.insertContentAt({ from, to }, {
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
