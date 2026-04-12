package edu.cit.gako.brainbox.notebook

import edu.cit.gako.brainbox.network.models.CategoryDetail
import edu.cit.gako.brainbox.network.models.AiConfigResponse
import edu.cit.gako.brainbox.network.models.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.network.models.NotebookVersionItem
import edu.cit.gako.brainbox.network.models.QuizCreateRequest
import edu.cit.gako.brainbox.network.models.QuizDetail

enum class EditorSaveState {
    SAVED,
    UNSAVED,
    SAVING,
    ERROR
}

data class EditorUiState(
    val notebookUuid: String,
    val resolvedNotebookUuid: String? = null,
    val title: String = "Untitled notebook",
    val categoryId: Long? = null,
    val categoryName: String? = null,
    val contentHtml: String = "<p></p>",
    val outline: List<String> = emptyList(),
    val saveState: EditorSaveState = EditorSaveState.SAVED,
    val isLoading: Boolean = true,
    val fatalErrorMessage: String? = null,
    val inlineMessage: String? = null,
    val isReviewMode: Boolean = false,
    val versions: List<NotebookVersionItem> = emptyList(),
    val isHistoryVisible: Boolean = false,
    val conflictedNotebook: NotebookDetail? = null,
    val categories: List<CategoryDetail> = emptyList(),
    val showCategorySheet: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showExitConfirmDialog: Boolean = false,
    val showAiPanel: Boolean = false,
    val aiConfigs: List<AiConfigResponse> = emptyList(),
    val selectedAiConfigId: Long? = null,
    val aiConversationTitle: String = "New chat",
    val aiMessages: List<NotebookAiMessageUi> = emptyList(),
    val aiConversationHistory: List<NotebookAiConversationHistoryUi> = emptyList(),
    val activeAiToolKey: String = NOTEBOOK_AI_TOOL_CHAT,
    val isAiWorking: Boolean = false,
    val aiPrompt: String = "",
    val aiSelectedText: String = "",
    val aiSelectionMode: String = "",
    val pendingQuizDraft: QuizCreateRequest? = null,
    val pendingFlashcardDraft: FlashcardDeckCreateRequest? = null,
    val createdQuiz: QuizDetail? = null,
    val createdFlashcardDeck: FlashcardDeckDetail? = null,
    val aiProposalState: AiProposalState = AiProposalState()
) {
    val isDirty: Boolean
        get() = saveState == EditorSaveState.UNSAVED || saveState == EditorSaveState.ERROR
}

data class ReviewHeadingTarget(
    val title: String,
    val chunkIndex: Int
)

data class ReviewUiState(
    val plainText: String = "",
    val headings: List<String> = emptyList(),
    val headingTargets: List<ReviewHeadingTarget> = emptyList(),
    val activeWordRange: IntRange? = null,
    val activeChunkIndex: Int = 0,
    val totalChunks: Int = 0
)

data class AiProposalState(
    val isVisible: Boolean = false,
    val responseText: String = "",
    val originalContent: String = "",
    val proposedContent: String = "",
    val sourcePrompt: String = ""
)

data class NotebookAiConversationHistoryUi(
    val id: String,
    val title: String,
    val preview: String,
    val messageCount: Int,
    val messages: List<NotebookAiMessageUi>,
    val activeToolKey: String = NOTEBOOK_AI_TOOL_CHAT
)

sealed interface NotebookUiEvent {
    data object CloseEditor : NotebookUiEvent
    data class ExportHtml(val title: String, val html: String) : NotebookUiEvent
    data class Message(val value: String) : NotebookUiEvent
    data class OpenQuiz(val uuid: String) : NotebookUiEvent
    data class OpenFlashcardDeck(val uuid: String) : NotebookUiEvent
}

data class NotebookAiToolDefinition(
    val key: String,
    val label: String,
    val prompt: String,
    val description: String
)

const val NOTEBOOK_AI_TOOL_CHAT = "chat"
const val NOTEBOOK_AI_TOOL_IMPROVE = "improve"
const val NOTEBOOK_AI_TOOL_EXPAND = "expand"
const val NOTEBOOK_AI_TOOL_SUMMARIZE = "summarize"
const val NOTEBOOK_AI_TOOL_EXPLAIN = "explain"
const val NOTEBOOK_AI_TOOL_QUIZ = "quiz"
const val NOTEBOOK_AI_TOOL_FLASHCARDS = "flashcards"
const val NOTEBOOK_AI_TOOL_STUDY_GUIDE = "study-guide"

internal val editorAiTools: List<NotebookAiToolDefinition> = listOf(
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_CHAT,
        label = "Chat",
        prompt = "",
        description = "Ask open-ended questions about this notebook."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_IMPROVE,
        label = "Improve",
        prompt = "Improve the selected text if there is one. Otherwise improve the current note for clarity, flow, grammar, and structure while preserving its meaning. Keep the existing formatting patterns when they work, and upgrade the structure only when it genuinely helps comprehension.",
        description = "Polish the note without losing detail."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_EXPAND,
        label = "Expand",
        prompt = "Expand the selected text if there is one. Otherwise expand the current note with more detail, examples, and supporting explanation while keeping the existing structure.",
        description = "Add depth, examples, and supporting detail."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_SUMMARIZE,
        label = "Summarize",
        prompt = "Summarize this note in the AI chat only. Do not rewrite or otherwise change the notebook content unless I explicitly ask you to write into the note.",
        description = "Turn the note into a tighter study summary."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_EXPLAIN,
        label = "Explain",
        prompt = "Explain the core concepts in this note in simpler language and call out the trickiest ideas to remember.",
        description = "Break complex ideas into clearer explanations."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_QUIZ,
        label = "Quiz",
        prompt = "Generate a quiz with 8 multiple choice questions from this note.",
        description = "Create a quiz draft from the notebook."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_FLASHCARDS,
        label = "Flashcards",
        prompt = "Generate a deck of 12 study flashcards from this note.",
        description = "Create a flashcard deck draft from the notebook."
    )
)

internal val reviewAiTools: List<NotebookAiToolDefinition> = listOf(
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_CHAT,
        label = "Chat",
        prompt = "",
        description = "Ask open-ended review questions about the current note."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_STUDY_GUIDE,
        label = "Study Guide",
        prompt = "Turn this note into a concise study guide with key ideas, important terms, and a short list of review questions. Keep the response in chat only and do not rewrite the notebook itself.",
        description = "Build a compact study guide in chat."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_SUMMARIZE,
        label = "Summarize",
        prompt = "Summarize this note in the AI chat only. Do not rewrite or otherwise change the notebook content.",
        description = "Compress the note into a shorter review version."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_EXPLAIN,
        label = "Explain",
        prompt = "Explain the core concepts in this note in simpler language and call out the hardest ideas to remember.",
        description = "Explain the note as a review aid."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_QUIZ,
        label = "Quiz",
        prompt = "Generate a quiz with 8 multiple choice questions from this note.",
        description = "Create a quiz draft from the reviewed material."
    ),
    NotebookAiToolDefinition(
        key = NOTEBOOK_AI_TOOL_FLASHCARDS,
        label = "Flashcards",
        prompt = "Generate a deck of 12 study flashcards from this note.",
        description = "Create a flashcard deck draft from the reviewed material."
    )
)
