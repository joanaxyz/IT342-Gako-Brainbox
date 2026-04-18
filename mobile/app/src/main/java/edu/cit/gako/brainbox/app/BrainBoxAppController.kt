package edu.cit.gako.brainbox.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.audio.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.audio.buildNotebookTtsRequest
import edu.cit.gako.brainbox.audio.toPlaybackUiState
import edu.cit.gako.brainbox.data.local.model.OfflineEntityType
import edu.cit.gako.brainbox.data.local.model.PendingMutation
import edu.cit.gako.brainbox.data.local.model.PendingMutationOperation
import edu.cit.gako.brainbox.data.local.toDocument
import edu.cit.gako.brainbox.data.worker.BrainBoxSyncWorkScheduler
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.UserProfile
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import retrofit2.HttpException

private data class QuizAttemptQueuePayload(
    val score: Int
)

private data class FlashcardAttemptQueuePayload(
    val mastery: Int
)

class BrainBoxAppController(
    appGraph: BrainBoxAppGraph,
    private val scope: CoroutineScope,
    private val onMessage: (String) -> Unit
) {
    private val appContext = appGraph.context
    private val repository = appGraph.repository
    private val localInfrastructure = appGraph.localInfrastructure
    private val audioStore = appGraph.audioStore
    private val audioClient = BrainBoxAudioClient(appGraph.context)
    private val gson = Gson()
    private var lastAutomaticRefreshAtMillis = 0L

    var state by mutableStateOf(AppState())
        private set

    init {
        observePlaybackState()
        observeOfflineSyncState()
    }

    fun bootstrap() {
        scope.launch {
            if (!repository.hasSession()) {
                state = state.copy(isBootstrapping = false)
                return@launch
            }

            syncHome(setBusy = false)
        }
    }

    fun handleGoogleLogin(idToken: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val user = repository.loginWithGoogle(idToken)
                state = state.copy(
                    isAuthenticated = true,
                    user = user,
                    currentTab = HomeTab.DASHBOARD
                )
                syncHome(setBusy = false)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't sign you in with Google. Try again.")
            }
        }
    }

    fun handleLogin(username: String, password: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                val user = repository.login(username, password)
                state = state.copy(
                    isAuthenticated = true,
                    user = user,
                    currentTab = HomeTab.DASHBOARD
                )
                syncHome(setBusy = false)
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't sign you in. Check your credentials and try again.")
            }
        }
    }

    fun handleRegister(username: String, email: String, password: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                repository.register(username, email, password)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.LOGIN
                )
                showMessage("Account created. Sign in to continue.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't create that account yet. Check the details and try again.")
            }
        }
    }

    fun handleSendResetCode(email: String) {
        scope.launch {
            state = state.copy(isBusy = true)

            try {
                repository.sendPasswordResetCode(email)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.FORGOT_CODE,
                    pendingResetEmail = email
                )
                showMessage("Reset code sent to $email.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't send a reset code right now.")
            }
        }
    }

    fun handleVerifyResetCode(code: String) {
        scope.launch {
            val email = state.pendingResetEmail
            if (email.isBlank()) {
                showMessage("Start with your email so we know where to send the reset code.")
                return@launch
            }

            state = state.copy(isBusy = true)

            try {
                val resetToken = repository.verifyPasswordResetCode(email, code)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.RESET_PASSWORD,
                    resetToken = resetToken
                )
                showMessage("Code verified. Set your new password.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("That reset code didn't work. Try again.")
            }
        }
    }

    fun handleResetPassword(password: String) {
        scope.launch {
            val token = state.resetToken
            if (token.isNullOrBlank()) {
                showMessage("Request a fresh reset code before setting a new password.")
                return@launch
            }

            state = state.copy(isBusy = true)

            try {
                repository.resetPassword(token, password)
                state = state.copy(
                    isBusy = false,
                    authStage = AuthStage.LOGIN,
                    pendingResetEmail = "",
                    resetToken = null
                )
                showMessage("Password updated. You can sign in now.")
            } catch (_: Exception) {
                state = state.copy(isBusy = false)
                showMessage("We couldn't reset your password yet.")
            }
        }
    }

    fun handleAuthStageChange(stage: AuthStage) {
        state = when (stage) {
            AuthStage.LOGIN -> state.copy(
                authStage = stage,
                pendingResetEmail = "",
                resetToken = null
            )
            else -> state.copy(authStage = stage)
        }
    }

    fun handleTabSelected(tab: HomeTab) {
        state = state.copy(currentTab = tab)
        if (tab == HomeTab.PROFILE) {
            requestAutomaticRefresh()
        }
    }

    fun handleCreateNotebook() {
        state = state.copy(
            activeNotebookUuid = "new",
            activeNotebookOriginTab = state.currentTab
        )
    }

    fun handleOpenQuiz(uuid: String) {
        openQuiz(uuid, returnToNotebookUuid = null)
    }

    fun handleOpenQuizFromNotebook(uuid: String) {
        openQuiz(uuid, returnToNotebookUuid = state.activeNotebookUuid)
    }

    fun handleOpenFlashcardDeck(uuid: String) {
        openFlashcardDeck(uuid, returnToNotebookUuid = null)
    }

    fun handleOpenFlashcardDeckFromNotebook(uuid: String) {
        openFlashcardDeck(uuid, returnToNotebookUuid = state.activeNotebookUuid)
    }

    private fun openQuiz(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            state = state.copy(isBusy = true)
            val canAttemptRemote = localInfrastructure.connectivityMonitor.currentState().isConnected

            if (canAttemptRemote) {
                runCatching { repository.getQuiz(uuid) }
                    .onSuccess { quiz ->
                        state = state.copy(
                            isBusy = false,
                            activeQuiz = quiz,
                            activeFlashcardDeck = null,
                            activeNotebookUuid = null,
                            studyReturnNotebookUuid = returnToNotebookUuid
                        )
                        syncStudyContext(quiz.notebookUuid)
                        return@launch
                    }
                    .onFailure {
                        val lostConnection = !localInfrastructure.connectivityMonitor.currentState().isConnected
                        if (lostConnection) {
                            val offlineQuiz = localInfrastructure.offlineRepository.getOfflineQuiz(uuid)
                            if (offlineQuiz != null) {
                                state = state.copy(
                                    isBusy = false,
                                    activeQuiz = offlineQuiz,
                                    activeFlashcardDeck = null,
                                    activeNotebookUuid = null,
                                    studyReturnNotebookUuid = returnToNotebookUuid
                                )
                                return@launch
                            }
                        }
                        state = state.copy(isBusy = false)
                        showMessage("We couldn't open that quiz yet.")
                    }
                return@launch
            }

            val offlineQuiz = localInfrastructure.offlineRepository.getOfflineQuiz(uuid)
            if (offlineQuiz != null) {
                state = state.copy(
                    isBusy = false,
                    activeQuiz = offlineQuiz,
                    activeFlashcardDeck = null,
                    activeNotebookUuid = null,
                    studyReturnNotebookUuid = returnToNotebookUuid
                )
                return@launch
            }

            state = state.copy(isBusy = false)
            showMessage("That quiz isn't available offline yet.")
        }
    }

    private fun openFlashcardDeck(uuid: String, returnToNotebookUuid: String?) {
        scope.launch {
            state = state.copy(isBusy = true)
            val canAttemptRemote = localInfrastructure.connectivityMonitor.currentState().isConnected

            if (canAttemptRemote) {
                runCatching { repository.getFlashcardDeck(uuid) }
                    .onSuccess { deck ->
                        state = state.copy(
                            isBusy = false,
                            activeFlashcardDeck = deck,
                            activeQuiz = null,
                            activeNotebookUuid = null,
                            studyReturnNotebookUuid = returnToNotebookUuid
                        )
                        syncStudyContext(deck.notebookUuid)
                        return@launch
                    }
                    .onFailure {
                        val lostConnection = !localInfrastructure.connectivityMonitor.currentState().isConnected
                        if (lostConnection) {
                            val offlineDeck = localInfrastructure.offlineRepository.getOfflineFlashcardDeck(uuid)
                            if (offlineDeck != null) {
                                state = state.copy(
                                    isBusy = false,
                                    activeFlashcardDeck = offlineDeck,
                                    activeQuiz = null,
                                    activeNotebookUuid = null,
                                    studyReturnNotebookUuid = returnToNotebookUuid
                                )
                                return@launch
                            }
                        }
                        state = state.copy(isBusy = false)
                        showMessage("We couldn't open that deck yet.")
                    }
                return@launch
            }

            val offlineDeck = localInfrastructure.offlineRepository.getOfflineFlashcardDeck(uuid)
            if (offlineDeck != null) {
                state = state.copy(
                    isBusy = false,
                    activeFlashcardDeck = offlineDeck,
                    activeQuiz = null,
                    activeNotebookUuid = null,
                    studyReturnNotebookUuid = returnToNotebookUuid
                )
                return@launch
            }

            state = state.copy(isBusy = false)
            showMessage("That deck isn't available offline yet.")
        }
    }

    fun handleOpenNotebook(uuid: String) {
        state = state.copy(
            activeNotebookUuid = uuid,
            activeNotebookOriginTab = state.currentTab
        )
    }

    fun handleCloseNotebookEditor() {
        val restoreTab = state.activeNotebookOriginTab ?: state.currentTab
        val shouldRefresh = state.isAuthenticated

        state = state.copy(
            activeNotebookUuid = null,
            activeNotebookOriginTab = null,
            currentTab = restoreTab
        )

        if (shouldRefresh) {
            scope.launch {
                syncHome(setBusy = false)
            }
        }
    }

    fun handleExitStudySession() {
        val returnNotebookUuid = state.studyReturnNotebookUuid
        state = state.copy(
            activeQuiz = null,
            activeFlashcardDeck = null,
            activeNotebookUuid = returnNotebookUuid,
            studyReturnNotebookUuid = null
        )
    }

    fun handleRecordQuizAttempt(uuid: String, score: Int) {
        scope.launch {
            runCatching {
                repository.recordQuizAttempt(uuid, score)
                syncHome(setBusy = false)
            }.onFailure {
                queueQuizAttempt(uuid, score)
                showMessage("Quiz score saved offline. We'll sync it when you're back online.")
            }
        }
    }

    fun handleRecordFlashcardAttempt(uuid: String, mastery: Int) {
        scope.launch {
            runCatching {
                repository.recordFlashcardAttempt(uuid, mastery)
                syncHome(setBusy = false)
            }.onFailure {
                queueFlashcardAttempt(uuid, mastery)
                showMessage("Deck progress saved offline. We'll sync it when you're back online.")
            }
        }
    }

    fun handleAddToQueue(notebook: NotebookSummary) {
        if (state.playbackQueue.any { it.uuid == notebook.uuid }) return
        state = state.copy(playbackQueue = state.playbackQueue + notebook)
        scope.launch {
            runCatching { repository.addToQueue(notebook.uuid) }
        }
    }

    fun handleRemoveFromQueue(notebookUuid: String) {
        state = state.copy(playbackQueue = state.playbackQueue.filter { it.uuid != notebookUuid })
        scope.launch {
            runCatching { repository.removeFromQueue(notebookUuid) }
        }
    }

    fun handleClearQueue() {
        state = state.copy(playbackQueue = emptyList())
        scope.launch {
            runCatching { repository.clearQueue() }
        }
    }

    fun handleSkipNext() {
        val queue = state.playbackQueue
        if (queue.isEmpty()) return
        val next = queue.first()
        state = state.copy(playbackQueue = queue.drop(1))
        scope.launch {
            runCatching { repository.removeFromQueue(next.uuid) }
            runCatching { repository.getNotebook(next.uuid) }
                .onSuccess { detail ->
                    audioClient.play(buildNotebookTtsRequest(notebook = detail, html = detail.content, offlineOnly = false))
                }
                .onFailure { showMessage("Couldn't load \"${next.title}\" for playback.") }
        }
    }

    fun handleAppForegrounded() {
        requestAutomaticRefresh()
    }

    fun handleLogout() {
        scope.launch {
            repository.logout()
            state = AppState(isBootstrapping = false)
            showMessage("Signed out.")
        }
    }

    private suspend fun syncHome(setBusy: Boolean) {
        if (setBusy) {
            state = state.copy(isBusy = true)
        }

        try {
            val bundle = repository.loadHome()
            state = state.copy(
                isBootstrapping = false,
                isBusy = false,
                isAuthenticated = true,
                user = bundle.user,
                homeData = bundle.homeData,
                playbackQueue = bundle.homeData.playbackQueue
            )
        } catch (error: HttpException) {
            if (error.code() == 401) {
                repository.logout()
                state = AppState(isBootstrapping = false)
                showMessage("Your session expired. Sign in again to keep going.")
            } else {
                loadOfflineHome("Showing your offline library while live data catches up.")
            }
        } catch (_: Exception) {
            loadOfflineHome("Showing your offline library while live data catches up.")
        }
    }

    private suspend fun loadOfflineHome(notice: String) {
        val offlineNotebooks = localInfrastructure.database.notebookDao()
            .getAllNotebooksOnce()
            .map { it.toDocument() }
        val offlineStudyCollections = localInfrastructure.offlineRepository.getOfflineStudyCollections()

        val notebookSummaries = offlineNotebooks.map { document ->
            NotebookSummary(
                uuid = document.uuid,
                title = document.title,
                wordCount = document.wordCount,
                createdAt = document.createdAt,
                updatedAt = document.updatedAt,
                lastReviewedAt = document.lastReviewedAt,
                version = document.version,
                categoryId = document.categoryId,
                categoryName = document.categoryName
            )
        }

        val recentlyReviewed = notebookSummaries
            .filter { !it.lastReviewedAt.isNullOrBlank() }
            .sortedByDescending { it.lastReviewedAt }
            .take(6)

        val lastSyncAtMillis = localInfrastructure.preferencesStore.preferences
            .firstOrNull()
            ?.lastSyncAtMillis

        state = state.copy(
            isBootstrapping = false,
            isBusy = false,
            isAuthenticated = repository.hasSession(),
            user = state.user ?: fallbackUser(),
            homeData = HomeData(
                notebooks = notebookSummaries,
                recentlyEdited = notebookSummaries.take(6),
                recentlyReviewed = recentlyReviewed,
                quizzes = offlineStudyCollections.quizzes,
                flashcards = offlineStudyCollections.flashcards,
                playlists = offlineStudyCollections.playlists,
                syncNotice = notice,
                syncedAtLabel = lastSyncAtMillis?.let(::formatSyncLabel)
            )
        )
    }

    private fun fallbackUser(): UserProfile {
        return UserProfile(
            username = repository.sessionUsername().ifBlank { "BrainBox User" },
            email = "",
            createdAt = null
        )
    }

    private fun syncStudyContext(notebookUuid: String?) {
        if (notebookUuid.isNullOrBlank()) {
            return
        }

        scope.launch {
            runCatching {
                repository.markNotebookReviewed(notebookUuid)
                val bundle = repository.loadHome()
                state = state.copy(
                    user = bundle.user,
                    homeData = bundle.homeData
                )
            }
        }
    }

    private fun observePlaybackState() {
        scope.launch {
            audioStore.snapshotFlow.collect { snapshot ->
                val wasPlaying = state.playbackState.isPlaying
                state = state.copy(playbackState = snapshot.toPlaybackUiState())
                if (wasPlaying && snapshot.status == BrainBoxAudioPlaybackStatus.ENDED
                        && state.playbackQueue.isNotEmpty()) {
                    handleSkipNext()
                }
            }
        }
    }

    private fun observeOfflineSyncState() {
        scope.launch {
            var previousSyncState: OfflineSyncState? = null
            combine(
                localInfrastructure.offlineRepository.observePendingMutations(),
                localInfrastructure.preferencesStore.preferences
            ) { pendingMutations, preferences ->
                OfflineSyncState(
                    pendingMutationCount = pendingMutations.size,
                    hasPendingMutations = pendingMutations.isNotEmpty(),
                    lastSyncAtMillis = preferences.lastSyncAtMillis,
                    lastSyncLabel = preferences.lastSyncAtMillis?.let(::formatSyncLabel)
                )
            }.collect { offlineSyncState ->
                val hadPendingMutations = previousSyncState?.hasPendingMutations == true
                val syncTimestampChanged =
                    previousSyncState?.lastSyncAtMillis != offlineSyncState.lastSyncAtMillis

                state = state.copy(
                    offlineSyncState = offlineSyncState,
                    homeData = state.homeData.copy(syncedAtLabel = offlineSyncState.lastSyncLabel)
                )
                previousSyncState = offlineSyncState

                val shouldRefreshHome =
                    state.isAuthenticated &&
                        syncTimestampChanged &&
                        (hadPendingMutations || offlineSyncState.hasPendingMutations)

                if (shouldRefreshHome) {
                    syncHome(setBusy = false)
                }
            }
        }
    }

    private suspend fun queueQuizAttempt(uuid: String, score: Int) {
        localInfrastructure.offlineRepository.queueMutation(
            PendingMutation(
                clientMutationId = UUID.randomUUID().toString(),
                entityType = OfflineEntityType.QUIZ,
                entityUuid = uuid,
                operation = PendingMutationOperation.RECORD_QUIZ_ATTEMPT,
                payloadJson = gson.toJson(QuizAttemptQueuePayload(score)),
                queuedAt = System.currentTimeMillis(),
                priority = 1
            )
        )
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
    }

    private suspend fun queueFlashcardAttempt(uuid: String, mastery: Int) {
        localInfrastructure.offlineRepository.queueMutation(
            PendingMutation(
                clientMutationId = UUID.randomUUID().toString(),
                entityType = OfflineEntityType.FLASHCARD_DECK,
                entityUuid = uuid,
                operation = PendingMutationOperation.RECORD_FLASHCARD_ATTEMPT,
                payloadJson = gson.toJson(FlashcardAttemptQueuePayload(mastery)),
                queuedAt = System.currentTimeMillis(),
                priority = 1
            )
        )
        BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
    }

    private fun requestAutomaticRefresh(force: Boolean = false) {
        if (!state.isAuthenticated || state.isBusy) {
            return
        }

        val now = System.currentTimeMillis()
        if (!force && now - lastAutomaticRefreshAtMillis < AUTOMATIC_REFRESH_THROTTLE_MILLIS) {
            return
        }

        lastAutomaticRefreshAtMillis = now
        scope.launch {
            BrainBoxSyncWorkScheduler.enqueueWhenOnline(appContext)
            syncHome(setBusy = false)
        }
    }

    private fun formatSyncLabel(timestampMillis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH)
        return "Updated ${Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).format(formatter)}"
    }

    private fun showMessage(message: String) {
        onMessage(message)
    }

    private companion object {
        const val AUTOMATIC_REFRESH_THROTTLE_MILLIS = 30_000L
    }
}
