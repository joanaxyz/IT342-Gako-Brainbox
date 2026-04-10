import {
  BookText,
  FileQuestionMark,
  Layers,
  Lightbulb,
  MessagesSquare,
  NotebookPen,
  SpellCheck2,
} from 'lucide-react';

export const EDITOR_AI_TOOLS = [
  {
    key: 'chat',
    label: 'Chat',
    icon: MessagesSquare,
    prompt: '',
    description: 'Open-ended questions and targeted edits for the current note.',
  },
  {
    key: 'improve',
    label: 'Improve',
    icon: SpellCheck2,
    prompt: 'Improve the selected text if there is one. Otherwise improve the current note for clarity, flow, grammar, and structure while preserving its meaning. Keep the existing formatting patterns when they work, and upgrade the structure with headings, lists, tables, blockquotes, links, or code formatting only when they make the note clearer.',
    description: 'Polish the writing without stripping detail or intent.',
  },
  {
    key: 'expand',
    label: 'Expand',
    icon: NotebookPen,
    prompt: 'Expand the selected text if there is one. Otherwise expand the current note with more detail, examples, and supporting explanation while keeping the existing structure. Add subsections, lists, tables, worked examples, or code formatting where they genuinely improve comprehension.',
    description: 'Develop a section into a fuller draft with more depth.',
  },
  {
    key: 'summarize',
    label: 'Summarize',
    icon: BookText,
    prompt: 'Summarize this note in the AI chat sidebar only. Do not rewrite, prepend, append, or otherwise change the notebook content unless I explicitly ask you to write the summary into the note. Format the response as a strong study aid with a concise heading, short overview paragraph, and bullets or a small table when comparison helps.',
    description: 'Compress the note into a shorter study version in chat only.',
  },
  {
    key: 'explain',
    label: 'Explain',
    icon: Lightbulb,
    prompt: 'Explain the core concepts in this note.',
    description: 'Break down difficult concepts in clearer language.',
  },
  {
    key: 'quiz',
    label: 'Quiz',
    icon: FileQuestionMark,
    prompt: 'Generate a quiz with 8 multiple choice questions from this note.',
    description: 'Turn the current note into a quiz draft.',
  },
  {
    key: 'flashcards',
    label: 'Flashcards',
    icon: Layers,
    prompt: 'Generate a deck of 12 study flashcards from this note.',
    description: 'Convert the note into flashcards for review.',
  },
];

export const REVIEW_AI_TOOLS = [
  {
    key: 'chat',
    label: 'Chat',
    icon: MessagesSquare,
    prompt: '',
    description: 'Ask open-ended review questions about the current note.',
  },
  {
    key: 'study-guide',
    label: 'Study Guide',
    icon: NotebookPen,
    prompt: 'Turn this note into a concise study guide with key ideas, important terms, and a short list of review questions. Keep the response in chat only and do not rewrite the notebook itself.',
    description: 'Create a compact review guide without editing the note.',
  },
  {
    key: 'summarize',
    label: 'Summarize',
    icon: BookText,
    prompt: 'Summarize this note in the AI chat sidebar only. Do not rewrite or otherwise change the notebook content. Format the response as a strong study aid with a concise heading, short overview paragraph, and bullets or a small table when comparison helps.',
    description: 'Compress the note into a shorter study version in chat only.',
  },
  {
    key: 'explain',
    label: 'Explain',
    icon: Lightbulb,
    prompt: 'Explain the core concepts in this note in simpler language and call out the hardest ideas to remember.',
    description: 'Break down difficult concepts in clearer language.',
  },
  {
    key: 'quiz',
    label: 'Quiz',
    icon: FileQuestionMark,
    prompt: 'Generate a quiz with 8 multiple choice questions from this note.',
    description: 'Turn the current note into a quiz draft.',
  },
  {
    key: 'flashcards',
    label: 'Flashcards',
    icon: Layers,
    prompt: 'Generate a deck of 12 study flashcards from this note.',
    description: 'Convert the note into flashcards for review.',
  },
];
