package edu.cit.gako.brainbox.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import edu.cit.gako.brainbox.app.playback.PlaybackCoordinator
import edu.cit.gako.brainbox.features.auth.AuthCoordinator
import edu.cit.gako.brainbox.app.study.StudySessionCoordinator
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import retrofit2.HttpException


class BrainBoxAppController(
    appGraph: BrainBoxAppGraph,
    private val scope: CoroutineScope,
    private val onMessage: (String) -> Unit
) {
    private val authRepository = appGraph.authRepository
    private val homeRepository = appGraph.homeRepository
    private val notebookRepository = appGraph.notebookRepository
    private val quizRepository = appGraph.quizRepository
    private val flashcardRepository = appGraph.flashcardRepository
    private var lastAutomaticRefreshAtMillis = 0L

    private val playbackCoordinator = PlaybackCoordinator(
        appContext = appGraph.context,
        scope = scope,
        notebookRepository = notebookRepository,
        audioStore = appGraph.audioStore,
        getState = { state },
        setState = { state = it },
        showMessage = ::showMessage
    )

    private val authCoordinator = AuthCoordinator(
        authRepository = authRepository,
        scope = scope,
        getState = { state },
        setState = { state = it },
        prepareAudioService = playbackCoordinator::prepareAudioService,
        syncHome = { setBusy -> syncHome(setBusy) },
        showMessage = ::showMessage
    )

    private val studyCoordinator = StudySessionCoordinator(
        quizRepository = quizRepository,
        flashcardRepository = flashcardRepository,
        scope = scope,
        getState = { state },
        setState = { state = it },
        syncHome = { setBusy -> syncHome(setBusy) },
        syncStudyContext = ::syncStudyContext,
        showMessage = ::showMessage
    )

    var state by mutableStateOf(AppState())
        private set

    init {
        playbackCoordinator.start()
    }

    fun bootstrap() {
        scope.launch {
            if (!authRepository.hasSession()) {
                state = state.copy(isBootstrapping = false)
                return@launch
            }

            playbackCoordinator.prepareAudioService()
            syncHome(setBusy = false)
        }
    }

    fun handleGoogleLogin(idToken: String) = authCoordinator.handleGoogleLogin(idToken)

    fun handleLogin(username: String, password: String) = authCoordinator.handleLogin(username, password)

    fun handleRegister(username: String, email: String, password: String) =
        authCoordinator.handleRegister(username, email, password)

    fun handleSendResetCode(email: String) = authCoordinator.handleSendResetCode(email)

    fun handleVerifyResetCode(code: String) = authCoordinator.handleVerifyResetCode(code)

    fun handleResetPassword(password: String) = authCoordinator.handleResetPassword(password)

    fun handleAuthStageChange(stage: AuthStage) = authCoordinator.handleAuthStageChange(stage)

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

    fun handleOpenQuiz(uuid: String) = studyCoordinator.handleOpenQuiz(uuid)

    fun handleOpenQuizFromNotebook(uuid: String) = studyCoordinator.handleOpenQuizFromNotebook(uuid)

    fun handleOpenFlashcardDeck(uuid: String) = studyCoordinator.handleOpenFlashcardDeck(uuid)

    fun handleOpenFlashcardDeckFromNotebook(uuid: String) =
        studyCoordinator.handleOpenFlashcardDeckFromNotebook(uuid)
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

    fun handleExitStudySession() = studyCoordinator.handleExitStudySession()

    fun handleRecordQuizAttempt(uuid: String, score: Int) =
        studyCoordinator.handleRecordQuizAttempt(uuid, score)

    fun handleRecordFlashcardAttempt(uuid: String, mastery: Int) =
        studyCoordinator.handleRecordFlashcardAttempt(uuid, mastery)

    fun handlePlayNotebook(notebook: NotebookSummary, switchToListenTab: Boolean = false) =
        playbackCoordinator.handlePlayNotebook(notebook, switchToListenTab)

    fun handlePlayCollection(
        playlistUuid: String?,
        orderedNotebooks: List<NotebookSummary>,
        playlistOrder: List<NotebookSummary>
    ) = playbackCoordinator.handlePlayCollection(playlistUuid, orderedNotebooks, playlistOrder)

    fun handleStartQueue() = playbackCoordinator.handleStartQueue()

    fun handleSkipNext() = playbackCoordinator.handleSkipNext()

    fun handleSkipPrevious() = playbackCoordinator.handleSkipPrevious()

    fun handleSelectQueuePlaylist(playlistUuid: String) =
        playbackCoordinator.handleSelectQueuePlaylist(playlistUuid)

    fun handleTogglePlaybackLoop() = playbackCoordinator.handleTogglePlaybackLoop()

    fun handleTogglePlaybackShuffle() = playbackCoordinator.handleTogglePlaybackShuffle()

    fun handleAppForegrounded() {
        requestAutomaticRefresh()
    }

    fun handleLogout() = authCoordinator.handleLogout()

    private suspend fun syncHome(setBusy: Boolean) {
        if (setBusy) {
            state = state.copy(isBusy = true)
        }

        try {
            val bundle = homeRepository.loadHome()
            state = state.copy(
                isBootstrapping = false,
                isBusy = false,
                isAuthenticated = true,
                user = bundle.user,
                homeData = bundle.homeData,
                playbackQueue = bundle.homeData.playbackQueue,
                playbackPlaylistUuid = bundle.homeData.playbackPlaylistUuid,
                playbackPlaylistTitle = bundle.homeData.playbackPlaylistTitle,
                playbackPlaylistCurrentIndex = bundle.homeData.playbackPlaylistCurrentIndex
            )
            playbackCoordinator.installPlaylistPlaybackContext(
                bundle.homeData.playbackPlaylistUuid,
                bundle.homeData.playbackQueue,
                bundle.homeData.playbackQueue
            )
        } catch (error: HttpException) {
            if (error.code() == 401) {
                authRepository.logout()
                state = AppState(isBootstrapping = false)
                showMessage("Your session expired. Sign in again to keep going.")
            } else {
                state = state.copy(isBootstrapping = false, isBusy = false)
                showMessage("We couldn't refresh BrainBox. Check your connection and try again.")
            }
        } catch (_: Exception) {
            state = state.copy(isBootstrapping = false, isBusy = false)
            showMessage("We couldn't refresh BrainBox. Check your connection and try again.")
        }
    }

    private fun syncStudyContext(notebookUuid: String?) {
        if (notebookUuid.isNullOrBlank()) {
            return
        }

        scope.launch {
            runCatching {
                notebookRepository.markNotebookReviewed(notebookUuid)
                val bundle = homeRepository.loadHome()
                state = state.copy(
                    user = bundle.user,
                    homeData = bundle.homeData
                )
            }
        }
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
            syncHome(setBusy = false)
        }
    }

    private fun showMessage(message: String) {
        onMessage(message)
    }

    private companion object {
        const val AUTOMATIC_REFRESH_THROTTLE_MILLIS = 30_000L
    }
}
