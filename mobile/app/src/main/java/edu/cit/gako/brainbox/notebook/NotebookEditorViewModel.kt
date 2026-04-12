package edu.cit.gako.brainbox.notebook

import android.content.Context
import android.text.Html
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import edu.cit.gako.brainbox.data.BrainBoxRepository
import edu.cit.gako.brainbox.data.NotebookMutationResult
import edu.cit.gako.brainbox.data.local.model.BrainBoxNotebookDocument
import edu.cit.gako.brainbox.data.local.model.ConflictDraft
import edu.cit.gako.brainbox.data.local.model.NotebookSyncState
import edu.cit.gako.brainbox.data.local.model.PendingMutation
import edu.cit.gako.brainbox.data.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.data.local.toDocument
import edu.cit.gako.brainbox.data.local.toEntity
import edu.cit.gako.brainbox.data.offline.BrainBoxLocalInfrastructure
import edu.cit.gako.brainbox.data.worker.BrainBoxSyncWorkScheduler
import edu.cit.gako.brainbox.network.models.AiConversationMessage
import edu.cit.gako.brainbox.network.models.AiQueryRequest
import edu.cit.gako.brainbox.network.models.CategoryDetail
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.network.models.NotebookVersionItem
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val LOCAL_NOTEBOOK_PREFIX = "local-"

private data class NotebookQueuedMutationPayload(
    val title: String? = null,
    val content: String? = null,
    val categoryId: Long? = null
)

class NotebookEditorViewModel(
    private val notebookUuid: String,
    private val appContext: Context,
    private val repository: BrainBoxRepository,
    private val localInfrastructure: BrainBoxLocalInfrastructure,
    private val gson: Gson = Gson()
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditorUiState(notebookUuid = notebookUuid))
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<NotebookUiEvent>()
    val uiEvents: SharedFlow<NotebookUiEvent> = _uiEvents.asSharedFlow()

    private var currentNotebook: NotebookDetail? = null

    init {
        loadCategories()
        refreshAiConfigs()
        loadNotebook()
    }

    fun loadNotebook() {
        viewModelScope.launch {
            val previousState = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = true,
                    fatalErrorMessage = null,
                    inlineMessage = null
                )
            }

            val cachedDocument = if (notebookUuid == "new") {
                null
            } else {
                localInfrastructure.database.notebookDao().getNotebook(notebookUuid)?.toDocument()
            }

            val loadedNotebook = runCatching {
                when {
                    notebookUuid == "new" -> buildNewNotebook()
                    notebookUuid.startsWith(LOCAL_NOTEBOOK_PREFIX) -> cachedDocument?.toNotebookDetail() ?: buildNewNotebook()
                    else -> repository.getNotebook(notebookUuid)
                }
            }.getOrElse { error ->
                cachedDocument?.toNotebookDetail() ?: run {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fatalErrorMessage = error.message ?: "We couldn't load that notebook."
                        )
                    }
                    return@launch
                }
            }

            currentNotebook = loadedNotebook
            _uiState.value = EditorUiState(
                notebookUuid = notebookUuid,
                resolvedNotebookUuid = loadedNotebook.uuid.takeUnless { it == "new" },
                title = loadedNotebook.title,
                categoryId = loadedNotebook.categoryId,
                categoryName = loadedNotebook.categoryName,
                contentHtml = loadedNotebook.content,
                outline = extractOutlineItems(loadedNotebook.content),
                saveState = EditorSaveState.SAVED,
                isLoading = false,
                categories = _uiState.value.categories,
                aiConfigs = _uiState.value.aiConfigs,
                selectedAiConfigId = _uiState.value.selectedAiConfigId,
                aiConversationTitle = previousState.aiConversationTitle,
                aiMessages = previousState.aiMessages,
                aiConversationHistory = previousState.aiConversationHistory,
                activeAiToolKey = resolveSupportedToolKey(
                    requestedKey = previousState.activeAiToolKey,
                    reviewMode = previousState.isReviewMode
                ),
                showAiPanel = previousState.showAiPanel,
                isReviewMode = previousState.isReviewMode,
                pendingQuizDraft = previousState.pendingQuizDraft,
                pendingFlashcardDraft = previousState.pendingFlashcardDraft,
                createdQuiz = previousState.createdQuiz,
                createdFlashcardDeck = previousState.createdFlashcardDeck,
                aiProposalState = previousState.aiProposalState
            )
            ensureAiConversationSeed()
        }
    }

    fun updateTitle(value: String) {
        _uiState.update { state ->
            state.copy(
                title = value,
                saveState = if (value == currentNotebook?.title && state.contentHtml == currentNotebook?.content && state.categoryId == currentNotebook?.categoryId) {
                    EditorSaveState.SAVED
                } else {
                    EditorSaveState.UNSAVED
                },
                inlineMessage = null
            )
        }
    }

    fun updateHtml(value: String) {
        _uiState.update { state ->
            state.copy(
                contentHtml = value,
                outline = extractOutlineItems(value),
                saveState = if (value == currentNotebook?.content && state.title == currentNotebook?.title && state.categoryId == currentNotebook?.categoryId) {
                    EditorSaveState.SAVED
                } else {
                    EditorSaveState.UNSAVED
                },
                inlineMessage = null
            )
        }
    }

    fun toggleReviewMode() {
        val nextReviewMode = !_uiState.value.isReviewMode
        _uiState.update {
            it.copy(
                isReviewMode = nextReviewMode,
                activeAiToolKey = resolveSupportedToolKey(
                    requestedKey = it.activeAiToolKey,
                    reviewMode = nextReviewMode
                )
            )
        }
        ensureAiConversationSeed()

        val notebook = currentNotebook
        if (nextReviewMode && notebook != null && !isLocalOnlyNotebook(notebook)) {
            viewModelScope.launch {
                runCatching { repository.markNotebookReviewed(notebook.uuid) }
            }
        }
    }

    fun requestCategorySheet(show: Boolean) {
        _uiState.update { it.copy(showCategorySheet = show) }
    }

    fun requestDeleteDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteDialog = show) }
    }

    fun requestClose() {
        viewModelScope.launch {
            if (_uiState.value.isDirty) {
                _uiState.update { it.copy(showExitConfirmDialog = true) }
            } else {
                _uiEvents.emit(NotebookUiEvent.CloseEditor)
            }
        }
    }

    fun dismissCloseDialog() {
        _uiState.update { it.copy(showExitConfirmDialog = false) }
    }

    fun discardChangesAndClose() {
        viewModelScope.launch {
            _uiState.update { it.copy(showExitConfirmDialog = false) }
            _uiEvents.emit(NotebookUiEvent.CloseEditor)
        }
    }

    fun selectCategory(category: CategoryDetail?) {
        _uiState.update { state ->
            state.copy(
                categoryId = category?.id,
                categoryName = category?.name,
                showCategorySheet = false,
                saveState = if (category?.id == currentNotebook?.categoryId && state.title == currentNotebook?.title && state.contentHtml == currentNotebook?.content) {
                    EditorSaveState.SAVED
                } else {
                    EditorSaveState.UNSAVED
                }
            )
        }
    }

    fun saveNotebook(isOnline: Boolean, closeAfterSave: Boolean = false) {
        val notebook = currentNotebook ?: buildNewNotebook()
        val stateSnapshot = _uiState.value
        val title = stateSnapshot.title.trim().ifBlank { "Untitled notebook" }
        val html = stateSnapshot.contentHtml.ifBlank { "<p></p>" }

        viewModelScope.launch {
            _uiState.update { it.copy(saveState = EditorSaveState.SAVING, inlineMessage = null, showExitConfirmDialog = false) }

            val result = when {
                isLocalOnlyNotebook(notebook) && isOnline -> {
                    repository.createNotebook(
                        title = title,
                        categoryId = stateSnapshot.categoryId,
                        content = html
                    )
                }

                isLocalOnlyNotebook(notebook) -> {
                    queueOfflineNotebookCreate(title, html, stateSnapshot.categoryId)
                }

                isOnline -> {
                    saveRemoteNotebook(
                        notebook = notebook,
                        title = title,
                        html = html,
                        categoryId = stateSnapshot.categoryId
                    )
                }

                else -> {
                    queueOfflineNotebookUpdate(notebook, title, html, stateSnapshot.categoryId)
                }
            }

            handleSaveResult(
                result = result,
                title = title,
                html = html,
                categoryId = stateSnapshot.categoryId,
                closeAfterSave = closeAfterSave,
                isOnline = isOnline
            )
        }
    }

    fun requestHistory() {
        val notebook = currentNotebook ?: return
        if (isLocalOnlyNotebook(notebook)) {
            viewModelScope.launch {
                _uiEvents.emit(NotebookUiEvent.Message("Version history becomes available after the notebook syncs to the server."))
            }
            return
        }

        viewModelScope.launch {
            val versions = runCatching { repository.getNotebookVersions(notebook.uuid) }
                .getOrElse {
                    _uiState.update { state ->
                        state.copy(inlineMessage = it.message ?: "We couldn't load notebook history.")
                    }
                    return@launch
                }

            _uiState.update { it.copy(versions = versions, isHistoryVisible = true) }
        }
    }

    fun dismissHistory() {
        _uiState.update { it.copy(isHistoryVisible = false) }
    }

    fun restoreVersion(version: NotebookVersionItem) {
        val notebook = currentNotebook ?: return
        if (isLocalOnlyNotebook(notebook)) {
            return
        }

        viewModelScope.launch {
            when (val result = repository.restoreNotebookVersion(notebook.uuid, version.id)) {
                is NotebookMutationResult.Success -> {
                    val restored = result.notebook ?: return@launch
                    currentNotebook = restored
                    persistNotebookSnapshot(restored, isOnline = true)
                    _uiState.update {
                        it.copy(
                            resolvedNotebookUuid = restored.uuid,
                            title = restored.title,
                            categoryId = restored.categoryId,
                            categoryName = restored.categoryName,
                            contentHtml = restored.content,
                            outline = extractOutlineItems(restored.content),
                            saveState = EditorSaveState.SAVED,
                            inlineMessage = null,
                            isHistoryVisible = false
                        )
                    }
                }

                is NotebookMutationResult.Conflict -> {
                    _uiState.update {
                        it.copy(
                            saveState = EditorSaveState.ERROR,
                            inlineMessage = result.message,
                            conflictedNotebook = result.latestNotebook,
                            isHistoryVisible = false
                        )
                    }
                }

                is NotebookMutationResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            saveState = EditorSaveState.ERROR,
                            inlineMessage = result.message,
                            isHistoryVisible = false
                        )
                    }
                }
            }
        }
    }

    fun useConflictedNotebook() {
        val latest = _uiState.value.conflictedNotebook ?: return
        currentNotebook = latest
        _uiState.update {
            it.copy(
                title = latest.title,
                categoryId = latest.categoryId,
                categoryName = latest.categoryName,
                contentHtml = latest.content,
                outline = extractOutlineItems(latest.content),
                saveState = EditorSaveState.SAVED,
                conflictedNotebook = null,
                inlineMessage = null
            )
        }
    }

    fun dismissConflict() {
        _uiState.update { it.copy(conflictedNotebook = null) }
    }

    fun requestExport() {
        viewModelScope.launch {
            _uiEvents.emit(
                NotebookUiEvent.ExportHtml(
                    title = _uiState.value.title.trim().ifBlank { "Untitled notebook" },
                    html = _uiState.value.contentHtml
                )
            )
        }
    }

    fun deleteNotebook(isOnline: Boolean) {
        val notebook = currentNotebook ?: return
        viewModelScope.launch {
            val result = when {
                isLocalOnlyNotebook(notebook) -> {
                    localInfrastructure.database.notebookDao().deleteNotebook(notebook.uuid)
                    NotebookMutationResult.Success(null)
                }

                isOnline -> repository.deleteNotebook(notebook.uuid, notebook.version)
                else -> {
                    localInfrastructure.offlineRepository.queueNotebookMutation(
                        notebookUuid = notebook.uuid,
                        operation = PendingMutationOperation.DELETE,
                        payloadJson = gson.toJson(NotebookQueuedMutationPayload()),
                        baseVersion = notebook.version
                    )
                    localInfrastructure.database.notebookDao().deleteNotebook(notebook.uuid)
                    BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
                    NotebookMutationResult.Success(null)
                }
            }

            when (result) {
                is NotebookMutationResult.Success -> {
                    _uiState.update { it.copy(showDeleteDialog = false) }
                    _uiEvents.emit(NotebookUiEvent.CloseEditor)
                }

                is NotebookMutationResult.Conflict -> {
                    _uiState.update {
                        it.copy(
                            showDeleteDialog = false,
                            saveState = EditorSaveState.ERROR,
                            inlineMessage = result.message
                        )
                    }
                }

                is NotebookMutationResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            showDeleteDialog = false,
                            saveState = EditorSaveState.ERROR,
                            inlineMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun toggleAiPanel() {
        val nextShow = !_uiState.value.showAiPanel
        _uiState.update { it.copy(showAiPanel = nextShow) }
        if (nextShow) {
            refreshAiConfigs()
            ensureAiConversationSeed()
        }
    }

    fun updateAiPrompt(value: String) {
        _uiState.update { it.copy(aiPrompt = value) }
    }

    fun selectAiTool(toolKey: String) {
        _uiState.update {
            it.copy(activeAiToolKey = resolveSupportedToolKey(toolKey, it.isReviewMode))
        }
    }

    fun startNewAiChat() {
        _uiState.update {
            it.copy(
                aiConversationTitle = "New chat",
                aiMessages = emptyList(),
                aiConversationHistory = archiveCurrentConversation(it),
                activeAiToolKey = NOTEBOOK_AI_TOOL_CHAT,
                aiPrompt = "",
                pendingQuizDraft = null,
                pendingFlashcardDraft = null,
                createdQuiz = null,
                createdFlashcardDeck = null,
                aiProposalState = AiProposalState()
            )
        }
        ensureAiConversationSeed()
    }

    fun restoreAiConversation(conversationId: String) {
        _uiState.update { state ->
            val selectedConversation = state.aiConversationHistory.firstOrNull { it.id == conversationId } ?: return@update state
            val archivedCurrent = currentConversationSnapshot(state)
            val remainingHistory = state.aiConversationHistory.filterNot { it.id == conversationId }
            state.copy(
                aiConversationTitle = selectedConversation.title,
                aiMessages = selectedConversation.messages,
                aiConversationHistory = listOfNotNull(archivedCurrent).plus(remainingHistory).take(12),
                activeAiToolKey = resolveSupportedToolKey(
                    requestedKey = selectedConversation.activeToolKey,
                    reviewMode = state.isReviewMode
                ),
                aiPrompt = "",
                pendingQuizDraft = null,
                pendingFlashcardDraft = null,
                createdQuiz = null,
                createdFlashcardDeck = null,
                aiProposalState = AiProposalState()
            )
        }
        ensureAiConversationSeed()
    }

    fun deleteAiConversation(conversationId: String) {
        _uiState.update { state ->
            state.copy(
                aiConversationHistory = state.aiConversationHistory.filterNot { it.id == conversationId }
            )
        }
    }

    fun submitAiPrompt(selectedText: String) {
        val notebook = currentNotebook ?: return
        if (isLocalOnlyNotebook(notebook)) {
            viewModelScope.launch {
                _uiEvents.emit(NotebookUiEvent.Message("Save and sync the notebook once before using notebook AI on mobile."))
            }
            return
        }

        val stateSnapshot = _uiState.value
        val activeTool = activeAiTools(stateSnapshot.isReviewMode)
            .firstOrNull { it.key == stateSnapshot.activeAiToolKey }
            ?: activeAiTools(stateSnapshot.isReviewMode).first()
        val prompt = stateSnapshot.aiPrompt.trim().ifBlank { activeTool.prompt }
        if (prompt.isBlank()) {
            viewModelScope.launch {
                _uiEvents.emit(NotebookUiEvent.Message("Enter a prompt for notebook AI first."))
            }
            return
        }

        val selectionMode = if (selectedText.isNotBlank()) "single_selection" else "document"
        val userMessage = NotebookAiMessageUi(
            id = UUID.randomUUID().toString(),
            role = NotebookAiMessageRole.User,
            text = prompt,
            leadingLabel = activeTool.label.takeIf { activeTool.key != NOTEBOOK_AI_TOOL_CHAT }
        )
        val pendingMessageId = UUID.randomUUID().toString()
        val pendingAssistantMessage = NotebookAiMessageUi(
            id = pendingMessageId,
            role = NotebookAiMessageRole.Assistant,
            text = if (_uiState.value.isReviewMode) {
                "Review AI is working through your note..."
            } else {
                "Notebook AI is working through your note..."
            },
            isPending = true,
            leadingLabel = if (_uiState.value.isReviewMode) "Review AI" else "Notebook AI"
        )
        val history = buildConversationHistory(stateSnapshot.aiMessages + userMessage)

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiWorking = true,
                    aiPrompt = "",
                    aiSelectedText = selectedText,
                    aiSelectionMode = selectionMode,
                    inlineMessage = null,
                    aiMessages = it.aiMessages + userMessage + pendingAssistantMessage
                )
            }

            val response = runCatching {
                repository.queryAi(
                    AiQueryRequest(
                        query = prompt,
                        notebookUuid = notebook.uuid,
                        conversationHistory = history,
                        selectedText = selectedText,
                        selectionMode = selectionMode,
                        mode = if (stateSnapshot.isReviewMode) "review" else "editor"
                    )
                )
            }.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isAiWorking = false,
                        inlineMessage = error.message ?: "Notebook AI is unavailable right now.",
                        aiMessages = it.aiMessages
                            .filterNot { message -> message.id == pendingMessageId }
                            .plus(
                                NotebookAiMessageUi(
                                    id = UUID.randomUUID().toString(),
                                    role = NotebookAiMessageRole.System,
                                    text = error.message ?: "Notebook AI is unavailable right now."
                                )
                            )
                    )
                }
                return@launch
            }

            val assistantReply = response.response
                ?.takeIf { it.isNotBlank() }
                ?: defaultAiResponseMessage(response.action)
            val proposedContent = response.editorContent.orEmpty()
            val proposalState = if (proposedContent.isNotBlank()) {
                AiProposalState(
                    isVisible = true,
                    responseText = assistantReply,
                    originalContent = _uiState.value.contentHtml,
                    proposedContent = proposedContent,
                    sourcePrompt = prompt
                )
            } else {
                AiProposalState()
            }

            _uiState.update {
                it.copy(
                    isAiWorking = false,
                    aiConversationTitle = response.conversationTitle
                        ?.takeIf(String::isNotBlank)
                        ?: it.aiConversationTitle,
                    aiMessages = it.aiMessages
                        .filterNot { message -> message.id == pendingMessageId }
                        .plus(
                            NotebookAiMessageUi(
                                id = UUID.randomUUID().toString(),
                                role = NotebookAiMessageRole.Assistant,
                                text = assistantReply,
                                leadingLabel = activeTool.label.takeIf { activeTool.key != NOTEBOOK_AI_TOOL_CHAT }
                            )
                        ),
                    pendingQuizDraft = response.quizData,
                    pendingFlashcardDraft = response.flashcardData,
                    aiProposalState = proposalState
                )
            }
        }
    }

    fun createPendingQuiz() {
        val notebook = currentNotebook ?: return
        val draft = _uiState.value.pendingQuizDraft ?: return
        if (isLocalOnlyNotebook(notebook)) {
            viewModelScope.launch {
                _uiEvents.emit(NotebookUiEvent.Message("Save and sync this notebook before creating a quiz."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiWorking = true, inlineMessage = null) }
            val createdQuiz = runCatching {
                repository.createQuiz(draft.copy(notebookUuid = notebook.uuid))
            }.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isAiWorking = false,
                        inlineMessage = error.message ?: "We couldn't create that quiz yet."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isAiWorking = false,
                    pendingQuizDraft = null,
                    createdQuiz = createdQuiz,
                    inlineMessage = "Quiz created from your notebook."
                )
            }
        }
    }

    fun createPendingFlashcardDeck() {
        val notebook = currentNotebook ?: return
        val draft = _uiState.value.pendingFlashcardDraft ?: return
        if (isLocalOnlyNotebook(notebook)) {
            viewModelScope.launch {
                _uiEvents.emit(NotebookUiEvent.Message("Save and sync this notebook before creating flashcards."))
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isAiWorking = true, inlineMessage = null) }
            val createdDeck = runCatching {
                repository.createFlashcardDeck(draft.copy(notebookUuid = notebook.uuid))
            }.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isAiWorking = false,
                        inlineMessage = error.message ?: "We couldn't create that flashcard deck yet."
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isAiWorking = false,
                    pendingFlashcardDraft = null,
                    createdFlashcardDeck = createdDeck,
                    inlineMessage = "Flashcard deck created from your notebook."
                )
            }
        }
    }

    fun dismissPendingQuiz() {
        _uiState.update { it.copy(pendingQuizDraft = null) }
    }

    fun dismissPendingFlashcards() {
        _uiState.update { it.copy(pendingFlashcardDraft = null) }
    }

    fun dismissCreatedQuiz() {
        _uiState.update { it.copy(createdQuiz = null) }
    }

    fun dismissCreatedFlashcardDeck() {
        _uiState.update { it.copy(createdFlashcardDeck = null) }
    }

    fun openCreatedQuiz() {
        val quiz = _uiState.value.createdQuiz ?: return
        viewModelScope.launch {
            _uiEvents.emit(NotebookUiEvent.OpenQuiz(quiz.uuid))
        }
    }

    fun openCreatedFlashcardDeck() {
        val deck = _uiState.value.createdFlashcardDeck ?: return
        viewModelScope.launch {
            _uiEvents.emit(NotebookUiEvent.OpenFlashcardDeck(deck.uuid))
        }
    }

    fun acceptAiProposal() {
        val proposedContent = _uiState.value.aiProposalState.proposedContent
        if (proposedContent.isBlank()) {
            return
        }

        _uiState.update {
            it.copy(
                contentHtml = proposedContent,
                outline = extractOutlineItems(proposedContent),
                saveState = EditorSaveState.UNSAVED,
                aiProposalState = AiProposalState(),
                showAiPanel = false,
                inlineMessage = "AI proposal applied. Review and save when you're ready."
            )
        }
    }

    fun rejectAiProposal() {
        _uiState.update { it.copy(aiProposalState = AiProposalState()) }
    }

    fun selectAiConfig(configId: Long) {
        viewModelScope.launch {
            val selected = runCatching { repository.selectAiConfig(configId) }
                .getOrElse {
                    _uiState.update { state ->
                        state.copy(inlineMessage = it.message ?: "We couldn't switch AI configurations.")
                    }
                    return@launch
                }

            _uiState.update { state ->
                state.copy(
                    selectedAiConfigId = selected.id,
                    inlineMessage = "Notebook AI is now using ${selected.name}."
                )
            }
            refreshAiConfigs()
        }
    }

    private fun refreshAiConfigs() {
        viewModelScope.launch {
            val configList = runCatching { repository.listAiConfigs() }.getOrNull() ?: return@launch
            _uiState.update {
                it.copy(
                    aiConfigs = configList.configs,
                    selectedAiConfigId = configList.selectedConfigId
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = runCatching { repository.getCategories() }.getOrDefault(emptyList())
            _uiState.update { state -> state.copy(categories = categories) }
        }
    }

    private suspend fun saveRemoteNotebook(
        notebook: NotebookDetail,
        title: String,
        html: String,
        categoryId: Long?
    ): NotebookMutationResult {
        var workingNotebook = notebook

        if (title != notebook.title || categoryId != notebook.categoryId) {
            when (
                val metadataResult = repository.updateNotebook(
                    uuid = notebook.uuid,
                    title = title,
                    categoryId = categoryId,
                    baseVersion = notebook.version
                )
            ) {
                is NotebookMutationResult.Success -> {
                    workingNotebook = metadataResult.notebook ?: notebook.copy(
                        title = title,
                        categoryId = categoryId
                    )
                }

                is NotebookMutationResult.Conflict -> return metadataResult
                is NotebookMutationResult.Failure -> return metadataResult
            }
        }

        return if (html != notebook.content) {
            repository.saveNotebookContent(
                uuid = workingNotebook.uuid,
                content = html,
                baseVersion = workingNotebook.version
            )
        } else {
            NotebookMutationResult.Success(
                workingNotebook.copy(
                    title = title,
                    categoryId = categoryId,
                    categoryName = resolveCategoryName(categoryId)
                )
            )
        }
    }

    private suspend fun queueOfflineNotebookCreate(
        title: String,
        html: String,
        categoryId: Long?
    ): NotebookMutationResult {
        val localUuid = currentNotebook?.uuid
            ?.takeIf { it.startsWith(LOCAL_NOTEBOOK_PREFIX) }
            ?: "$LOCAL_NOTEBOOK_PREFIX${UUID.randomUUID()}"
        val localNotebook = NotebookDetail(
            uuid = localUuid,
            title = title,
            content = html,
            version = 0L,
            categoryId = categoryId,
            categoryName = resolveCategoryName(categoryId)
        )
        currentNotebook = localNotebook
        persistNotebookSnapshot(localNotebook, isOnline = false)
        val existingCreateMutation = localInfrastructure.database.pendingMutationDao()
            .findMutationForEntity(localUuid, PendingMutationOperation.CREATE)
        localInfrastructure.offlineRepository.queueMutation(
            PendingMutation(
                clientMutationId = existingCreateMutation?.clientMutationId ?: UUID.randomUUID().toString(),
                entityType = edu.cit.gako.brainbox.data.local.model.OfflineEntityType.NOTEBOOK,
                entityUuid = localUuid,
                operation = PendingMutationOperation.CREATE,
                payloadJson = gson.toJson(
                    NotebookQueuedMutationPayload(
                        title = title,
                        content = html,
                        categoryId = categoryId
                    )
                ),
                baseVersion = null,
                queuedAt = existingCreateMutation?.queuedAt ?: System.currentTimeMillis(),
                attemptCount = existingCreateMutation?.attemptCount ?: 0,
                priority = 2
            )
        )
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
        return NotebookMutationResult.Success(localNotebook)
    }

    private suspend fun queueOfflineNotebookUpdate(
        notebook: NotebookDetail,
        title: String,
        html: String,
        categoryId: Long?
    ): NotebookMutationResult {
        val updatedNotebook = notebook.copy(
            title = title,
            content = html,
            categoryId = categoryId,
            categoryName = resolveCategoryName(categoryId)
        )
        currentNotebook = updatedNotebook
        persistNotebookSnapshot(updatedNotebook, isOnline = false)
        localInfrastructure.offlineRepository.queueNotebookMutation(
            notebookUuid = notebook.uuid,
            operation = PendingMutationOperation.UPDATE,
            payloadJson = gson.toJson(
                NotebookQueuedMutationPayload(
                    title = title.takeIf { it != notebook.title },
                    content = html,
                    categoryId = categoryId.takeIf { it != notebook.categoryId }
                )
            ),
            baseVersion = notebook.version
        )
        localInfrastructure.offlineRepository.markNotebookDirty(notebook.uuid)
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
        return NotebookMutationResult.Success(updatedNotebook)
    }

    private suspend fun handleSaveResult(
        result: NotebookMutationResult,
        title: String,
        html: String,
        categoryId: Long?,
        closeAfterSave: Boolean,
        isOnline: Boolean
    ) {
        when (result) {
            is NotebookMutationResult.Success -> {
                val savedNotebook = result.notebook ?: currentNotebook?.copy(
                    title = title,
                    content = html,
                    categoryId = categoryId,
                    categoryName = resolveCategoryName(categoryId)
                )
                if (savedNotebook != null) {
                    currentNotebook = savedNotebook
                    persistNotebookSnapshot(savedNotebook, isOnline)
                    _uiState.update {
                        it.copy(
                            resolvedNotebookUuid = savedNotebook.uuid.takeUnless { uuid -> uuid == "new" },
                            title = savedNotebook.title,
                            categoryId = savedNotebook.categoryId,
                            categoryName = savedNotebook.categoryName,
                            contentHtml = savedNotebook.content,
                            outline = extractOutlineItems(savedNotebook.content),
                            saveState = EditorSaveState.SAVED,
                            inlineMessage = if (isOnline) null else "Saved offline. We'll sync when you're back online."
                        )
                    }
                }
                if (closeAfterSave) {
                    _uiEvents.emit(NotebookUiEvent.CloseEditor)
                }
            }

            is NotebookMutationResult.Conflict -> {
                val notebook = currentNotebook
                if (result.latestNotebook != null && notebook != null) {
                    localInfrastructure.offlineRepository.storeConflictDraft(
                        ConflictDraft(
                            draftId = UUID.randomUUID().toString(),
                            notebookUuid = notebook.uuid,
                            baseVersion = notebook.version ?: 0L,
                            serverVersion = result.latestNotebook.version ?: 0L,
                            localTitle = title,
                            serverTitle = result.latestNotebook.title,
                            localContentHtml = html,
                            serverContentHtml = result.latestNotebook.content,
                            reason = result.message,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                _uiState.update {
                    it.copy(
                        saveState = EditorSaveState.ERROR,
                        inlineMessage = result.message,
                        conflictedNotebook = result.latestNotebook
                    )
                }
            }

            is NotebookMutationResult.Failure -> {
                _uiState.update {
                    it.copy(
                        saveState = EditorSaveState.ERROR,
                        inlineMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun persistNotebookSnapshot(notebook: NotebookDetail, isOnline: Boolean) {
        localInfrastructure.database.notebookDao().upsertNotebook(
            notebook.toNotebookDocument(
                existingOffline = true,
                syncState = if (isOnline) NotebookSyncState.CLEAN else NotebookSyncState.DIRTY
            ).toEntity()
        )
    }

    private fun buildNewNotebook(): NotebookDetail {
        return NotebookDetail(
            uuid = "new",
            title = "Untitled notebook",
            content = "<p></p>",
            version = null
        )
    }

    private fun ensureAiConversationSeed() {
        _uiState.update { state ->
            if (state.aiMessages.isNotEmpty()) {
                state
            } else {
                state.copy(
                    aiMessages = listOf(
                        NotebookAiMessageUi(
                            id = UUID.randomUUID().toString(),
                            role = NotebookAiMessageRole.Assistant,
                            text = if (state.isReviewMode) {
                                "Review AI is ready. Ask for a study guide, a summary, a quiz, or a flashcard deck from this notebook."
                            } else {
                                "Notebook AI is ready. Ask for edits, explanations, summaries, quizzes, or flashcards."
                            },
                            leadingLabel = if (state.isReviewMode) "Review AI" else "Notebook AI"
                        )
                    )
                )
            }
        }
    }

    private fun archiveCurrentConversation(state: EditorUiState): List<NotebookAiConversationHistoryUi> {
        val snapshot = currentConversationSnapshot(state) ?: return state.aiConversationHistory
        return listOf(snapshot).plus(state.aiConversationHistory).take(12)
    }

    private fun currentConversationSnapshot(state: EditorUiState): NotebookAiConversationHistoryUi? {
        val cleanMessages = state.aiMessages.filterNot { it.isPending }
        val meaningfulMessages = cleanMessages.filterNot(::isSeedAiMessage)
        if (meaningfulMessages.isEmpty()) {
            return null
        }

        val fallbackTitle = meaningfulMessages
            .firstOrNull { it.role == NotebookAiMessageRole.User }
            ?.text
            ?.replace(Regex("\\s+"), " ")
            ?.take(56)
            ?.trim()
            .orEmpty()
            .ifBlank { "Conversation" }
        val preview = cleanMessages
            .lastOrNull()
            ?.text
            ?.replace(Regex("\\s+"), " ")
            ?.take(140)
            ?.trim()
            .orEmpty()

        return NotebookAiConversationHistoryUi(
            id = UUID.randomUUID().toString(),
            title = state.aiConversationTitle.ifBlank { fallbackTitle },
            preview = preview.ifBlank { fallbackTitle },
            messageCount = cleanMessages.size,
            messages = cleanMessages,
            activeToolKey = resolveSupportedToolKey(
                requestedKey = state.activeAiToolKey,
                reviewMode = state.isReviewMode
            )
        )
    }

    private fun isSeedAiMessage(message: NotebookAiMessageUi): Boolean {
        if (message.role != NotebookAiMessageRole.Assistant) {
            return false
        }
        return message.text.startsWith("Notebook AI is ready.") || message.text.startsWith("Review AI is ready.")
    }

    private fun activeAiTools(reviewMode: Boolean): List<NotebookAiToolDefinition> {
        return if (reviewMode) reviewAiTools else editorAiTools
    }

    private fun resolveSupportedToolKey(requestedKey: String, reviewMode: Boolean): String {
        return activeAiTools(reviewMode)
            .firstOrNull { it.key == requestedKey }
            ?.key
            ?: NOTEBOOK_AI_TOOL_CHAT
    }

    private fun buildConversationHistory(messages: List<NotebookAiMessageUi>): List<AiConversationMessage> {
        return messages.mapNotNull { message ->
            when (message.role) {
                NotebookAiMessageRole.User -> AiConversationMessage("user", message.text)
                NotebookAiMessageRole.Assistant -> AiConversationMessage("assistant", message.text)
                else -> null
            }
        }
    }

    private fun defaultAiResponseMessage(action: String?): String {
        return when (action) {
            "create_quiz" -> "Quiz draft ready."
            "create_flashcard" -> "Flashcard deck draft ready."
            "replace_editor", "replace_selection", "add_to_editor", "replace_ai_selections" -> "Editor update ready to review."
            else -> "Notebook AI is ready."
        }
    }

    private fun resolveCategoryName(categoryId: Long?): String? {
        return _uiState.value.categories.firstOrNull { it.id == categoryId }?.name
    }

    private fun isLocalOnlyNotebook(notebook: NotebookDetail): Boolean {
        return notebook.uuid == "new" || notebook.uuid.startsWith(LOCAL_NOTEBOOK_PREFIX)
    }

    companion object {
        fun factory(
            notebookUuid: String,
            appContext: Context,
            repository: BrainBoxRepository,
            localInfrastructure: BrainBoxLocalInfrastructure
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotebookEditorViewModel(
                        notebookUuid = notebookUuid,
                        appContext = appContext,
                        repository = repository,
                        localInfrastructure = localInfrastructure
                    ) as T
                }
            }
        }
    }
}

internal fun extractOutlineItems(html: String): List<String> {
    return Regex("<h[1-6][^>]*>(.*?)</h[1-6]>", RegexOption.IGNORE_CASE)
        .findAll(html)
        .map { it.groupValues[1].replace(Regex("<[^>]+>"), " ").trim() }
        .filter { it.isNotBlank() }
        .toList()
}

private fun BrainBoxNotebookDocument.toNotebookDetail(): NotebookDetail {
    return NotebookDetail(
        uuid = uuid,
        title = title,
        content = contentHtml,
        wordCount = wordCount,
        createdAt = createdAt,
        updatedAt = updatedAt,
        lastReviewedAt = lastReviewedAt,
        version = version,
        categoryId = categoryId,
        categoryName = categoryName
    )
}

private fun NotebookDetail.toNotebookDocument(
    existingOffline: Boolean,
    syncState: NotebookSyncState
): BrainBoxNotebookDocument {
    return BrainBoxNotebookDocument(
        uuid = uuid,
        title = title,
        categoryId = categoryId,
        categoryName = categoryName,
        contentHtml = content,
        wordCount = wordCount,
        version = version ?: 0L,
        lastReviewedAt = lastReviewedAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isAvailableOffline = existingOffline,
        syncState = syncState,
        localUpdatedAt = System.currentTimeMillis(),
        remoteUpdatedAt = System.currentTimeMillis()
    )
}

internal fun String.toImportedHtml(): String {
    val trimmed = trim()
    if (trimmed.startsWith("<") && trimmed.contains(">")) {
        return trimmed
    }

    return trimmed
        .split(Regex("\\n\\s*\\n"))
        .joinToString(separator = "") { paragraph ->
            "<p>${paragraph.trim().replace("\n", "<br />")}</p>"
        }
        .ifBlank { "<p></p>" }
}

internal fun String.htmlToPreviewText(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString().trim().ifBlank { "No preview" }
}
