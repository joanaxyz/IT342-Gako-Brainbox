import {
  startTransition,
  useDeferredValue,
  useState,
  useRef,
  useEffect,
  useCallback,
  useMemo,
} from 'react';
import { createPortal } from 'react-dom';
import { useNavigate } from 'react-router-dom';
import {
  AlertCircle,
  CheckCircle2,
  History,
  Plus,
  Search,
  Send,
  Settings,
  Sparkles,
  Trash2,
  X,
} from 'lucide-react';
import { aiAPI } from '../../../../common/utils/api.jsx';
import { useNotification } from '../../../../common/hooks/hooks';
import { useFlashcard, useQuiz } from '../../../shared/hooks/hooks';
import { isAndroidHost, openHostQuiz, openHostFlashcardDeck } from '../../../../app/host/brainBoxHost';
import AiSettingsModal from './AiSettingsModal';
import MarkdownIt from 'markdown-it';
import DOMPurify from 'dompurify';

const createMessageId = () => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }

  return `msg_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
};

const normalizeMessages = (items = []) => items.map((entry) => ({
  id: entry.id || createMessageId(),
  role: entry.role,
  content: entry.content,
  checkpoint: entry.checkpoint ?? null,
}));

const buildFallbackConversationTitle = (value) => {
  const normalized = value
    ?.replace(/\s+/g, ' ')
    .replace(/["']/g, '')
    .trim();

  if (!normalized) {
    return 'New chat';
  }

  return normalized.length > 60 ? `${normalized.slice(0, 60).trim()}...` : normalized;
};

const sanitizeConversationTitle = (value, messages = []) => {
  const normalized = value
    ?.replace(/[\r\n]+/g, ' ')
    .replace(/["']/g, '')
    .trim();

  if (normalized) {
    return normalized.length > 60 ? `${normalized.slice(0, 60).trim()}...` : normalized;
  }

  const firstUserMessage = messages.find((entry) => entry.role === 'user')?.content;
  return buildFallbackConversationTitle(firstUserMessage);
};

const EDIT_INTENT_PATTERN = /\b(improve|expand|rewrite|reword|rephrase|paraphrase|edit|polish|refine|fix|shorten|condense|tighten|clarify|revise|replace|update|simplify|formalize|restructure|clean up|make (?:this|it)|turn (?:this|it)|write (?:this|it)|add .*?(?:into|to) (?:the )?(?:note|document))\b/i;
const EDIT_TOOL_KEYS = new Set(['improve', 'expand']);

const hasEditIntent = (value, activeToolKey, mode, isToolTriggered = false) => {
  if (mode !== 'editor') {
    return false;
  }

  if (isToolTriggered && EDIT_TOOL_KEYS.has(activeToolKey)) {
    return true;
  }

  return EDIT_INTENT_PATTERN.test(value);
};

const formatRelativeTime = (isoString) => {
  if (!isoString) return '';
  const diff = Date.now() - new Date(isoString).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  return `${days}d ago`;
};

const formatCheckpointTime = (isoString) => {
  if (!isoString) {
    return '';
  }

  return new Intl.DateTimeFormat(undefined, {
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  }).format(new Date(isoString));
};

const EDITOR_TOOL_INSTRUCTIONS = [
  'Pick a tool from the rail to choose the kind of help you want.',
  'Select text and click Add in the editor if you want saved AI highlights for targeted edits.',
  'If nothing is selected, the editor will ask whether the AI should use the whole note instead.',
  'Use Clear to remove saved AI highlights when you want a fresh target set.',
  'Review AI proposals before accepting them into the note.',
];

const REVIEW_TOOL_INSTRUCTIONS = [
  'Pick a tool from the rail to choose the kind of review help you want.',
  'Select a passage in the note if you want the AI to focus on that exact section.',
  'Use Summarize, Study Guide, Explain, Quiz, and Flashcards to turn the note into review material.',
  'Review mode keeps the notebook itself unchanged.',
];

const QUIZ_ACTION_ALIASES = new Set([
  'create_quiz',
  'create_quizzes',
  'generate_quiz',
  'generate_quizzes',
  'quiz',
  'quizzes',
]);

const FLASHCARD_ACTION_ALIASES = new Set([
  'create_flashcard',
  'create_flashcards',
  'generate_flashcard',
  'generate_flashcards',
  'create_flashcard_deck',
  'create_deck',
  'deck',
  'flashcard',
  'flashcards',
]);

const tryParseJsonObject = (value) => {
  if (!value) {
    return null;
  }

  if (typeof value === 'string') {
    try {
      const parsed = JSON.parse(value);
      return parsed && typeof parsed === 'object' ? parsed : null;
    } catch {
      return null;
    }
  }

  return typeof value === 'object' ? value : null;
};

const normalizeGeneratedAction = (action, { quizDraft, flashcardDraft }) => {
  const normalizedAction = typeof action === 'string' ? action.trim().toLowerCase() : '';

  if (QUIZ_ACTION_ALIASES.has(normalizedAction)) {
    return 'create_quiz';
  }

  if (FLASHCARD_ACTION_ALIASES.has(normalizedAction)) {
    return 'create_flashcard';
  }

  if (quizDraft && !flashcardDraft) {
    return 'create_quiz';
  }

  if (flashcardDraft && !quizDraft) {
    return 'create_flashcard';
  }

  return normalizedAction || 'none';
};

const normalizeQuizQuestion = (value) => {
  const question = tryParseJsonObject(value);
  if (!question) {
    return null;
  }

  const text = typeof question.text === 'string' ? question.text.trim() : '';
  const options = Array.isArray(question.options)
    ? question.options.filter((option) => typeof option === 'string' && option.trim())
    : [];
  const parsedCorrectIndex = Number.parseInt(question.correctIndex, 10);
  const correctIndex = Number.isFinite(parsedCorrectIndex)
    && parsedCorrectIndex >= 0
    && parsedCorrectIndex < options.length
    ? parsedCorrectIndex
    : 0;

  if (!text || options.length < 2) {
    return null;
  }

  return {
    type: 'multiple_choice',
    text,
    options: options.slice(0, 4),
    correctIndex,
  };
};

const normalizeQuizDraft = (value, notebookUuid) => {
  const quiz = tryParseJsonObject(value);
  if (!quiz) {
    return null;
  }

  const questionSource = quiz.questions ?? quiz.items ?? quiz.quizQuestions;
  const questions = Array.isArray(questionSource)
    ? questionSource.map(normalizeQuizQuestion).filter(Boolean)
    : [];

  if (questions.length === 0) {
    return null;
  }

  return {
    title: typeof quiz.title === 'string' && quiz.title.trim()
      ? quiz.title.trim()
      : 'Generated Quiz',
    description: typeof quiz.description === 'string' && quiz.description.trim()
      ? quiz.description.trim()
      : 'AI-generated quiz from this note.',
    difficulty: typeof quiz.difficulty === 'string' && quiz.difficulty.trim()
      ? quiz.difficulty.trim()
      : 'medium',
    notebookUuid,
    questions,
  };
};

const normalizeFlashcardCard = (value) => {
  const card = tryParseJsonObject(value);
  if (!card) {
    return null;
  }

  const front = typeof card.front === 'string' ? card.front.trim() : '';
  const back = typeof card.back === 'string' ? card.back.trim() : '';

  if (!front || !back) {
    return null;
  }

  return { front, back };
};

const normalizeFlashcardDraft = (value, notebookUuid) => {
  const flashcardDeck = tryParseJsonObject(value);
  if (!flashcardDeck) {
    return null;
  }

  const cardSource = flashcardDeck.cards
    ?? flashcardDeck.flashcards
    ?? flashcardDeck.items
    ?? flashcardDeck.deckCards;
  const cards = Array.isArray(cardSource)
    ? cardSource.map(normalizeFlashcardCard).filter(Boolean)
    : [];

  if (cards.length === 0) {
    return null;
  }

  return {
    title: typeof flashcardDeck.title === 'string' && flashcardDeck.title.trim()
      ? flashcardDeck.title.trim()
      : 'Generated Flashcards',
    description: typeof flashcardDeck.description === 'string' && flashcardDeck.description.trim()
      ? flashcardDeck.description.trim()
      : 'AI-generated flashcard deck from this note.',
    notebookUuid,
    cards,
  };
};

const resolveGeneratedQuizDraft = (payload, notebookUuid) => {
  const normalizedPayload = tryParseJsonObject(payload);
  if (!normalizedPayload) {
    return null;
  }

  const candidates = [
    normalizedPayload.quizData,
    normalizedPayload.quiz,
    normalizedPayload.quiz_data,
    normalizedPayload.generatedQuiz,
    Array.isArray(normalizedPayload.questions) ? normalizedPayload : null,
  ];

  return candidates
    .map((candidate) => normalizeQuizDraft(candidate, notebookUuid))
    .find(Boolean) ?? null;
};

const resolveGeneratedFlashcardDraft = (payload, notebookUuid) => {
  const normalizedPayload = tryParseJsonObject(payload);
  if (!normalizedPayload) {
    return null;
  }

  const candidates = [
    normalizedPayload.flashcardData,
    normalizedPayload.flashcardsData,
    normalizedPayload.flashcardDeck,
    normalizedPayload.deckData,
    normalizedPayload.deck,
    normalizedPayload.generatedDeck,
    normalizedPayload.generatedFlashcards,
    (Array.isArray(normalizedPayload.cards) || Array.isArray(normalizedPayload.flashcards))
      ? normalizedPayload
      : null,
  ];

  return candidates
    .map((candidate) => normalizeFlashcardDraft(candidate, notebookUuid))
    .find(Boolean) ?? null;
};

const AiAssistantSidebar = ({
  isOpen,
  onClose,
  notebookUuid,
  activeToolKey = 'chat',
  onActiveToolChange,
  mode = 'editor',
  quickTools = [],
  getSelectionText,
  getAiSelections,
  isToolHelpOpen = false,
  onToolHelpClose,
  contained = false,
  onApplyEditorContent,
  hasProposedChanges = false,
  pendingProposalSourceId = null,
  acceptedCheckpointEvent = null,
  onRestoreCheckpoint,
  className = '',
}) => {
  const [showAiSettings, setShowAiSettings] = useState(false);
  const [message, setMessage] = useState('');
  const [messages, setMessages] = useState([]);
  const [historyQuery, setHistoryQuery] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const [error, setError] = useState(null);
  const [pendingQuiz, setPendingQuiz] = useState(null);
  const [pendingFlashcard, setPendingFlashcard] = useState(null);
  const [createdQuiz, setCreatedQuiz] = useState(null);
  const [createdFlashcard, setCreatedFlashcard] = useState(null);
  const [isCreating, setIsCreating] = useState(false);
  const [conversations, setConversations] = useState([]);
  const [activeConvUuid, setActiveConvUuid] = useState(null);
  const [isLoadingHistory, setIsLoadingHistory] = useState(false);
  const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);
  const [conversationTitle, setConversationTitle] = useState('New chat');

  const deferredHistoryQuery = useDeferredValue(historyQuery);
  const filteredConversations = useMemo(() => {
    const normalizedQuery = deferredHistoryQuery.trim().toLowerCase();

    if (!normalizedQuery) {
      return conversations;
    }

    return conversations.filter((conversation) => {
      const haystack = `${conversation.title || ''} ${conversation.messages || ''}`.toLowerCase();
      return haystack.includes(normalizedQuery);
    });
  }, [conversations, deferredHistoryQuery]);
  const activeTool = quickTools.find((tool) => tool.key === activeToolKey) || quickTools[0] || null;
  const ActiveToolIcon = activeTool?.icon;
  const messagesListRef = useRef(null);

  const md = useMemo(() => new MarkdownIt({ html: false, linkify: true, typographer: true }), []);
  const renderMarkdown = useCallback((value) => {
    if (!value) return '';
    try {
      const raw = md.render(typeof value === 'string' ? value : String(value));
      if (typeof DOMPurify !== 'undefined' && typeof window !== 'undefined') {
        return DOMPurify.sanitize(raw);
      }
      return raw;
    } catch (e) {
      return String(value);
    }
  }, [md]);
  const { addNotification } = useNotification();
  const { createQuiz } = useQuiz();
  const { createFlashcard } = useFlashcard();
  const navigate = useNavigate();
  const resolvedToolInstructions = mode === 'review'
    ? REVIEW_TOOL_INSTRUCTIONS
    : EDITOR_TOOL_INSTRUCTIONS;
  const sidebarLabel = mode === 'review' ? 'Review AI assistant' : 'AI assistant';

  useEffect(() => {
    const messageList = messagesListRef.current;

    if (!messageList) {
      return;
    }

    messageList.scrollTo({
      top: messageList.scrollHeight,
      behavior: 'smooth',
    });
  }, [messages, isTyping, pendingQuiz, pendingFlashcard]);

  useEffect(() => {
    setMessages([]);
    setMessage('');
    setError(null);
    setPendingQuiz(null);
    setPendingFlashcard(null);
    setCreatedQuiz(null);
    setCreatedFlashcard(null);
    setActiveConvUuid(null);
    setHistoryQuery('');
    setConversationTitle('New chat');
    setIsHistoryModalOpen(false);
  }, [notebookUuid]);

  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    const handleKeyDown = (event) => {
      if (event.key === 'Escape') {
        if (isHistoryModalOpen) {
          setIsHistoryModalOpen(false);
          return;
        }

        onClose?.();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isHistoryModalOpen, isOpen, onClose]);

  useEffect(() => {
    if (!isOpen || typeof window === 'undefined') {
      return undefined;
    }

    const isOverlayMode = !contained && window.matchMedia('(max-width: 1280px)').matches;
    if (!isOverlayMode) {
      return undefined;
    }

    const { overflow } = document.body.style;
    document.body.style.overflow = 'hidden';

    return () => {
      document.body.style.overflow = overflow;
    };
  }, [contained, isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setIsHistoryModalOpen(false);
    }
  }, [isOpen]);

  const loadHistory = useCallback(async () => {
    if (!notebookUuid) return;
    setIsLoadingHistory(true);

    try {
      const res = await aiAPI.getConversations(notebookUuid);
      if (res.success) {
        setConversations((res.data || []).filter((conversation) => conversation.mode === mode));
      }
    } catch {
      // silently fail
    } finally {
      setIsLoadingHistory(false);
    }
  }, [mode, notebookUuid]);

  useEffect(() => {
    if (isHistoryModalOpen) {
      void loadHistory();
    }
  }, [isHistoryModalOpen, loadHistory]);

  const upsertConversation = useCallback((conversation) => {
    if (!conversation?.uuid) {
      return;
    }

    setConversations((currentConversations) => {
      const remainingConversations = currentConversations.filter((entry) => entry.uuid !== conversation.uuid);
      return [conversation, ...remainingConversations];
    });
  }, []);

  const persistConversation = useCallback(async (updatedMessages, preferredTitle) => {
    if (!notebookUuid || updatedMessages.length <= 1) {
      return;
    }

    const convTitle = sanitizeConversationTitle(preferredTitle, updatedMessages);

    try {
      if (activeConvUuid) {
        const response = await aiAPI.updateConversation(activeConvUuid, updatedMessages, convTitle);
        if (response.success && response.data) {
          setConversationTitle(response.data.title || convTitle);
          upsertConversation(response.data);
        }
      } else {
        const response = await aiAPI.saveConversation(notebookUuid, mode, updatedMessages, convTitle);
        if (response.success && response.data) {
          setActiveConvUuid(response.data.uuid);
          setConversationTitle(response.data.title || convTitle);
          upsertConversation(response.data);
        }
      }
    } catch {
      // silently fail - history saving is non-critical
    }
  }, [activeConvUuid, mode, notebookUuid, upsertConversation]);

  useEffect(() => {
    if (
      !acceptedCheckpointEvent
      || acceptedCheckpointEvent.notebookUuid !== notebookUuid
      || !acceptedCheckpointEvent.sourceMessageId
      || !acceptedCheckpointEvent.checkpoint
    ) {
      return;
    }

    let nextMessagesForPersistence = null;
    setMessages((currentMessages) => {
      let didChange = false;
      const nextMessages = currentMessages.map((entry) => {
        if (entry.id !== acceptedCheckpointEvent.sourceMessageId) {
          return entry;
        }

        didChange = true;
        return {
          ...entry,
          checkpoint: acceptedCheckpointEvent.checkpoint,
        };
      });

      if (didChange) {
        nextMessagesForPersistence = nextMessages;
      }

      return didChange ? nextMessages : currentMessages;
    });

    if (nextMessagesForPersistence) {
      void persistConversation(nextMessagesForPersistence);
    }
  }, [acceptedCheckpointEvent, notebookUuid, persistConversation]);

  const buildConversationHistory = (currentMessages) => currentMessages
    .slice(1)
    .map((entry) => ({ role: entry.role, content: entry.content }));

  const resolveAiSelectionTargets = useCallback(() => {
    const nextSelections = getAiSelections?.();
    return Array.isArray(nextSelections) ? nextSelections : [];
  }, [getAiSelections]);

  const handleSendMessage = async (textOverride, options = {}) => {
    const text = textOverride !== undefined ? textOverride : message;
    const selectedText = options.selectedText !== undefined
      ? options.selectedText
      : (getSelectionText?.() || '');
    const aiSelections = Array.isArray(options.aiSelections)
      ? options.aiSelections
      : resolveAiSelectionTargets();
    const effectiveSelectedText = aiSelections.length > 0 ? '' : selectedText;
    let resolvedSelectionMode = options.selectionMode
      || (aiSelections.length > 0 ? 'ai_selection' : effectiveSelectedText ? 'single_selection' : '');

    if (!text.trim() || isTyping || !notebookUuid) {
      return;
    }

    if (
      !options.skipIntentGuard
      && hasEditIntent(text, activeToolKey, mode, Boolean(options.toolTriggered))
      && !effectiveSelectedText.trim()
      && aiSelections.length === 0
      && resolvedSelectionMode !== 'document'
    ) {
      resolvedSelectionMode = 'document';
    }

    const userMessage = {
      id: createMessageId(),
      role: 'user',
      content: text,
    };
    const historySnapshot = [...messages];
    const nextMessages = [...messages, userMessage];
    setMessages(nextMessages);

    if (textOverride === undefined) {
      setMessage('');
    }

    setIsTyping(true);
    setError(null);

    try {
      const history = buildConversationHistory(historySnapshot);
      const response = await aiAPI.query(
        text,
        notebookUuid,
        history,
        effectiveSelectedText,
        mode,
        {
          selectionMode: resolvedSelectionMode,
          aiSelections,
        },
      );

      if (!response.success) {
        throw new Error(response.message || 'Failed to get AI response');
      }

      const {
        response: reply,
        action,
        editorContent,
        conversationTitle: aiConversationTitle,
        selectionEdits,
      } = response.data;
      const normalizedQuizDraft = resolveGeneratedQuizDraft(response.data, notebookUuid);
      const normalizedFlashcardDraft = resolveGeneratedFlashcardDraft(response.data, notebookUuid);
      const normalizedGenerationAction = normalizeGeneratedAction(action, {
        quizDraft: normalizedQuizDraft,
        flashcardDraft: normalizedFlashcardDraft,
      });

      const finalMessages = [
        ...nextMessages,
        {
          id: createMessageId(),
          role: 'assistant',
          content: reply,
        },
      ];
      setMessages(finalMessages);
      const nextConversationTitle = sanitizeConversationTitle(aiConversationTitle, finalMessages);
      setConversationTitle(nextConversationTitle);

      if (
        onApplyEditorContent
        && action === 'add_to_editor'
        && editorContent
      ) {
        onApplyEditorContent(editorContent, 'append', { sourceMessageId: userMessage.id });
      } else if (
        onApplyEditorContent
        && action === 'replace_editor'
        && editorContent
      ) {
        const replaceResult = onApplyEditorContent(editorContent, 'replace', {
          sourceMessageId: userMessage.id,
          clearAllAiSelections: true,
        });
        if (replaceResult?.applied === false) {
          addNotification('The AI\'s response didn\'t produce any changes to the note.', 'info', 3500);
        }
      } else if (
        onApplyEditorContent
        && action === 'replace_selection'
        && editorContent
      ) {
        const selectionResult = onApplyEditorContent(editorContent, 'replace_selection', {
          sourceMessageId: userMessage.id,
          aiSelectionIds: aiSelections.map((selection) => selection.id),
        });
        if (selectionResult?.applied === false) {
          addNotification(
            selectionResult?.reason === 'no_changes'
              ? 'The rephrased text is the same as the original — no changes were made.'
              : 'No text was selected in the editor. Highlight the text you want to change first.',
            'info',
            3500,
          );
        }
      } else if (
        onApplyEditorContent
        && action === 'replace_ai_selections'
        && Array.isArray(selectionEdits)
        && selectionEdits.length > 0
      ) {
        const aiSelResult = onApplyEditorContent('', 'replace_ai_selections', {
          sourceMessageId: userMessage.id,
          aiSelectionIds: selectionEdits.map((selection) => selection.id),
          selectionEdits,
        });
        if (aiSelResult?.applied === false) {
          addNotification(
            'The AI\'s rephrasing didn\'t produce any detectable changes. The selections may already match the result.',
            'info',
            3500,
          );
        }
      } else if (normalizedGenerationAction === 'create_quiz' && normalizedQuizDraft) {
        setPendingQuiz(normalizedQuizDraft);
        setPendingFlashcard(null);
      } else if (normalizedGenerationAction === 'create_flashcard' && normalizedFlashcardDraft) {
        setPendingFlashcard(normalizedFlashcardDraft);
        setPendingQuiz(null);
      }

      void persistConversation(finalMessages, nextConversationTitle);
    } catch {
      setError('Failed to reach AI service. Please try again.');
    } finally {
      setIsTyping(false);
    }
  };

  const handleToolClick = (tool) => {
    const selectedText = getSelectionText?.() || '';
    const aiSelections = resolveAiSelectionTargets();
    const basePrompt = tool.prompt;
    const prompt = !aiSelections.length && selectedText && tool.includeSelectionPreview !== false
      ? `[Selected text: "${selectedText.length > 300 ? `${selectedText.slice(0, 300)}...` : selectedText}"]\n\n${basePrompt}`
      : basePrompt;

    void handleSendMessage(prompt, {
      selectedText: aiSelections.length > 0 ? '' : selectedText,
      aiSelections,
      toolLabel: tool.label,
      toolTriggered: true,
    });
  };

  const handleRunTool = (tool) => {
    onActiveToolChange?.(tool.key);
    handleToolClick(tool);
  };

  const handleLoadConversation = (conversation) => {
    try {
      const parsed = JSON.parse(conversation.messages);
      setMessages(normalizeMessages(parsed));
      setActiveConvUuid(conversation.uuid);
      setConversationTitle(sanitizeConversationTitle(conversation.title, parsed));
      setError(null);
      setPendingQuiz(null);
      setPendingFlashcard(null);
      setCreatedQuiz(null);
      setCreatedFlashcard(null);
      setIsHistoryModalOpen(false);
    } catch {
      addNotification('Failed to load conversation', 'error', 3000);
    }
  };

  const handleDeleteConversation = async (uuid, event) => {
    event.stopPropagation();
    try {
      const res = await aiAPI.deleteConversation(uuid);
      if (res.success) {
        setConversations((currentConversations) => currentConversations.filter((conversation) => conversation.uuid !== uuid));
        if (activeConvUuid === uuid) {
          setActiveConvUuid(null);
          setConversationTitle('New chat');
        }
      }
    } catch {
      addNotification('Failed to delete conversation', 'error', 3000);
    }
  };

  const handleNewChat = () => {
    setMessages([]);
    setMessage('');
    setError(null);
    setPendingQuiz(null);
    setPendingFlashcard(null);
    setCreatedQuiz(null);
    setCreatedFlashcard(null);
    setActiveConvUuid(null);
    setConversationTitle('New chat');
    setIsHistoryModalOpen(false);
    onActiveToolChange?.('chat');
  };

  const handleCreateQuiz = async () => {
    if (!pendingQuiz || isCreating) {
      return;
    }

    setIsCreating(true);
    try {
      const response = await createQuiz(pendingQuiz, false);
      if (response.success) {
        setCreatedQuiz(response.data);
        setPendingQuiz(null);
        addNotification('Quiz created successfully!', 'success', 3000);
      } else {
        addNotification(response.message || 'Failed to create quiz', 'error', 3000);
      }
    } catch {
      addNotification('Failed to create quiz', 'error', 3000);
    } finally {
      setIsCreating(false);
    }
  };

  const handleCreateFlashcard = async () => {
    if (!pendingFlashcard || isCreating) {
      return;
    }

    setIsCreating(true);
    try {
      const response = await createFlashcard(pendingFlashcard, false);
      if (response.success) {
        setCreatedFlashcard(response.data);
        setPendingFlashcard(null);
        addNotification('Flashcard deck created successfully!', 'success', 3000);
      } else {
        addNotification(response.message || 'Failed to create deck', 'error', 3000);
      }
    } catch {
      addNotification('Failed to create deck', 'error', 3000);
    } finally {
      setIsCreating(false);
    }
  };

  const handleViewQuiz = useCallback(() => {
    if (!createdQuiz?.uuid) {
      return;
    }

    if (isAndroidHost()) {
      openHostQuiz(createdQuiz.uuid);
      return;
    }

    startTransition(() => {
      navigate('/quizzes', { state: { autoOpenQuizUuid: createdQuiz.uuid } });
    });
  }, [createdQuiz?.uuid, navigate]);

  const handleViewFlashcardDeck = useCallback(() => {
    if (!createdFlashcard?.uuid) {
      return;
    }

    if (isAndroidHost()) {
      openHostFlashcardDeck(createdFlashcard.uuid);
      return;
    }

    startTransition(() => {
      navigate('/flashcards', { state: { autoOpenDeckUuid: createdFlashcard.uuid } });
    });
  }, [createdFlashcard?.uuid, navigate]);

  const historyModal = isHistoryModalOpen && typeof document !== 'undefined'
    ? createPortal(
      <>
        <button
          type="button"
          className="ai-history-modal-backdrop"
          onClick={() => setIsHistoryModalOpen(false)}
          aria-label="Close history"
        />
        <div className="ai-history-modal" role="dialog" aria-modal="true" aria-label="Chat history">
          <div className="ai-history-modal-header">
            <div className="ai-history-modal-copy">
              <span className="ai-section-label">Saved conversations</span>
              <strong>History</strong>
            </div>
            <button
              type="button"
              className="ai-sidebar-icon-btn"
              onClick={() => setIsHistoryModalOpen(false)}
              aria-label="Close history"
            >
              <X size={16} />
            </button>
          </div>

          <label className="ai-tool-search ai-history-search" htmlFor={`ai-history-search-${mode}`}>
            <Search size={15} />
            <input
              id={`ai-history-search-${mode}`}
              type="search"
              value={historyQuery}
              onChange={(event) => setHistoryQuery(event.target.value)}
              placeholder="Search sessions..."
            />
          </label>

          {isLoadingHistory && (
            <div className="ai-history-loading">Loading...</div>
          )}

          {!isLoadingHistory && filteredConversations.length === 0 && (
            <div className="ai-history-empty">
              {deferredHistoryQuery.trim()
                ? 'No matching conversations.'
                : 'No saved conversations yet. Start chatting and your conversations will appear here.'}
            </div>
          )}

          <div className="ai-history-list">
            {filteredConversations.map((conversation) => (
              <div
                key={conversation.uuid}
                className={`ai-history-item ${activeConvUuid === conversation.uuid ? 'is-active' : ''}`}
              >
                <button
                  type="button"
                  className="ai-history-item-main"
                  onClick={() => handleLoadConversation(conversation)}
                >
                  <div className="ai-history-item-body">
                    <span className="ai-history-item-title">{conversation.title || 'Untitled'}</span>
                    <span className="ai-history-item-time">{formatRelativeTime(conversation.updatedAt)}</span>
                  </div>
                </button>
                <button
                  type="button"
                  className="ai-history-item-delete"
                  onClick={(event) => handleDeleteConversation(conversation.uuid, event)}
                  aria-label="Delete conversation"
                  title="Delete"
                >
                  <Trash2 size={13} />
                </button>
              </div>
            ))}
          </div>
        </div>
      </>,
      document.body,
    )
    : null;

  return (
    <>
      {!contained && isOpen && (
        <button
          type="button"
          className="ai-sidebar-backdrop"
          onClick={onClose}
          aria-label={`Close ${sidebarLabel}`}
        />
      )}
      <aside
        className={`ai-sidebar ${className} ${(contained || isOpen) ? 'open' : ''} ${hasProposedChanges ? 'with-proposal' : ''}`.trim()}
        role="dialog"
        aria-label={sidebarLabel}
        aria-modal="false"
      >
        <div className="ai-sidebar-inner">
          <div className="ai-sidebar-header ai-sidebar-header--conversation">
            <div className="ai-sidebar-heading">
              <div className="ai-sidebar-title-mark">
                <Sparkles size={14} className="ai-sparkle-icon" />
              </div>
              <div className="ai-sidebar-heading-copy">
                <strong className="ai-sidebar-heading-title">{conversationTitle}</strong>
              </div>
            </div>
            <div className="ai-sidebar-header-actions">
              <button
                type="button"
                className={`ai-sidebar-icon-btn${showAiSettings ? ' is-active' : ''}`}
                onClick={() => setShowAiSettings((v) => !v)}
                aria-label="AI provider settings"
                title="AI settings"
              >
                <Settings size={16} />
              </button>
              <button
                type="button"
                className="ai-sidebar-icon-btn"
                onClick={handleNewChat}
                aria-label="Start new chat"
                title="New chat"
              >
                <Plus size={16} />
              </button>
              <button
                type="button"
                className="ai-sidebar-icon-btn"
                onClick={() => setIsHistoryModalOpen(true)}
                aria-label="Open chat history"
                title="History"
                disabled={!notebookUuid}
              >
                <History size={16} />
              </button>
            </div>
          </div>

          <div className="ai-sidebar-body">
            <AiSettingsModal isOpen={showAiSettings} onClose={() => setShowAiSettings(false)} />
            <div className="ai-chat-container">
              {isToolHelpOpen && (
                  <section className="ai-tool-help-card">
                    <div className="ai-tool-help-header">
                      <div className="ai-tool-help-copy">
                        <span className="ai-section-label">Tool help</span>
                        <strong>How to use the rail</strong>
                      </div>
                      <button
                        type="button"
                        className="ai-sidebar-icon-btn"
                        onClick={onToolHelpClose}
                        aria-label="Close tool instructions"
                      >
                        <X size={14} />
                      </button>
                    </div>
                    <ol className="ai-tool-help-list">
                      {resolvedToolInstructions.map((instruction) => (
                        <li key={instruction}>{instruction}</li>
                      ))}
                    </ol>
                  </section>
                )}

                {activeTool && activeToolKey !== 'chat' && (
                  <section className="ai-active-tool-bar">
                    <div className="ai-active-tool-main">
                      <div className="ai-active-tool-icon">
                        {ActiveToolIcon && <ActiveToolIcon size={16} />}
                      </div>
                      <div className="ai-active-tool-copy">
                        <span className="ai-section-label">Active tool</span>
                        <strong>{activeTool.label}</strong>
                      </div>
                    </div>
                    <button
                      type="button"
                      className="ai-btn-create ai-active-tool-run"
                      onClick={() => handleRunTool(activeTool)}
                      disabled={!notebookUuid || isTyping}
                    >
                      Run
                    </button>
                  </section>
                )}

                <div className="ai-chat-messages" ref={messagesListRef}>
                  {messages.map((entry) => {
                    const isProposalSource = (
                      entry.role === 'user'
                      && mode === 'editor'
                      && hasProposedChanges
                      && pendingProposalSourceId === entry.id
                    );
                    const hasCheckpoint = Boolean(entry.checkpoint?.versionId);

                    return (
                      <div key={entry.id} className={`ai-message ai-message--${entry.role}`}>
                        {entry.role === 'assistant' ? (
                          <div
                            className="ai-message-content"
                            dangerouslySetInnerHTML={{ __html: renderMarkdown(entry.content || '') }}
                          />
                        ) : (
                          <div className="ai-message-content" style={{ whiteSpace: 'pre-wrap' }}>
                            {entry.content}
                          </div>
                        )}
                        {entry.role === 'user' && (isProposalSource || hasCheckpoint) && (
                          <div className="ai-message-footer">
                            {isProposalSource && (
                              <span className="ai-message-badge ai-message-badge--pending">
                                Pending editor changes
                              </span>
                            )}
                            {hasCheckpoint && (
                              <span className="ai-message-badge ai-message-badge--saved">
                                Saved {formatCheckpointTime(entry.checkpoint.savedAt)}
                              </span>
                            )}
                            {hasCheckpoint && onRestoreCheckpoint && (
                              <button
                                type="button"
                                className="ai-message-action"
                                onClick={() => onRestoreCheckpoint(entry.checkpoint)}
                              >
                                Revert to this point
                              </button>
                            )}
                          </div>
                        )}
                      </div>
                    );
                  })}

                  {isTyping && (
                    <div className="ai-message ai-message--assistant">
                      <div className="ai-message-typing">
                        <span />
                        <span />
                        <span />
                      </div>
                    </div>
                  )}

                  {error && (
                    <div className="ai-message ai-message--error">
                      <div className="ai-message-content ai-message-content--error">
                        <AlertCircle size={14} />
                        {error}
                      </div>
                    </div>
                  )}

                  {pendingQuiz && (
                    <div className="ai-generated-card">
                      <div className="ai-generated-card-header">
                        <CheckCircle2 size={14} />
                        <strong>{pendingQuiz.title || 'Generated Quiz'}</strong>
                      </div>
                      <div className="ai-generated-card-meta">
                        {pendingQuiz.questions?.length ?? 0} questions - {pendingQuiz.difficulty || 'medium'}
                      </div>
                      <div className="ai-generated-card-actions">
                        <button type="button" className="ai-btn-dismiss" onClick={() => setPendingQuiz(null)}>Dismiss</button>
                        <button type="button" className="ai-btn-create" onClick={handleCreateQuiz} disabled={isCreating}>
                          {isCreating ? 'Creating...' : (
                            <>
                              <CheckCircle2 size={13} /> Create Quiz
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  )}

                  {createdQuiz && (
                    <div className="ai-generated-card ai-generated-card--success">
                      <div className="ai-generated-card-header">
                        <CheckCircle2 size={14} />
                        <strong>{createdQuiz.title || 'Quiz created'}</strong>
                      </div>
                      <div className="ai-generated-card-meta">
                        {createdQuiz.questionCount ?? 0} questions - {createdQuiz.difficulty || 'medium'}
                      </div>
                      <div className="ai-generated-card-actions">
                        <button type="button" className="ai-btn-dismiss" onClick={() => setCreatedQuiz(null)}>Dismiss</button>
                        <button
                          type="button"
                          className="ai-btn-create"
                          onClick={handleViewQuiz}
                        >
                          View Quiz
                        </button>
                      </div>
                    </div>
                  )}

                  {createdFlashcard && (
                    <div className="ai-generated-card ai-generated-card--success">
                      <div className="ai-generated-card-header">
                        <CheckCircle2 size={14} />
                        <strong>{createdFlashcard.title || 'Deck created'}</strong>
                      </div>
                      <div className="ai-generated-card-meta">
                        {createdFlashcard.cardCount ?? 0} cards
                      </div>
                      <div className="ai-generated-card-actions">
                        <button type="button" className="ai-btn-dismiss" onClick={() => setCreatedFlashcard(null)}>Dismiss</button>
                        <button
                          type="button"
                          className="ai-btn-create"
                          onClick={handleViewFlashcardDeck}
                        >
                          View Deck
                        </button>
                      </div>
                    </div>
                  )}

                  {pendingFlashcard && (
                    <div className="ai-generated-card">
                      <div className="ai-generated-card-header">
                        <CheckCircle2 size={14} />
                        <strong>{pendingFlashcard.title || 'Generated Flashcards'}</strong>
                      </div>
                      <div className="ai-generated-card-meta">
                        {pendingFlashcard.cards?.length ?? 0} cards
                      </div>
                      <div className="ai-generated-card-actions">
                        <button type="button" className="ai-btn-dismiss" onClick={() => setPendingFlashcard(null)}>Dismiss</button>
                        <button type="button" className="ai-btn-create" onClick={handleCreateFlashcard} disabled={isCreating}>
                          {isCreating ? 'Creating...' : (
                            <>
                              <CheckCircle2 size={13} /> Create Deck
                            </>
                          )}
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                <div className="ai-chat-input-wrapper">
                  <div className="ai-chat-input-container">
                    <textarea
                      placeholder={
                        mode === 'review'
                          ? 'Ask about this note...'
                          : activeToolKey === 'chat'
                            ? 'Ask anything...'
                            : `Ask a follow-up about ${activeTool?.label?.toLowerCase() || 'this note'}...`
                      }
                      value={message}
                      onChange={(event) => setMessage(event.target.value)}
                      onKeyDown={(event) => {
                        if (event.key === 'Enter' && !event.shiftKey) {
                          event.preventDefault();
                          void handleSendMessage();
                        }
                      }}
                      rows={1}
                    />
                    <button
                      type="button"
                      className="ai-send-btn"
                      onClick={() => void handleSendMessage()}
                      disabled={!message.trim() || isTyping || !notebookUuid}
                    >
                      <Send size={16} />
                    </button>
                  </div>
                </div>
            </div>
          </div>
        </div>
      </aside>
      {historyModal}
    </>
  );
};

export default AiAssistantSidebar;
