package edu.cit.gako.brainbox.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import edu.cit.gako.brainbox.features.home.HomeDependencies
import edu.cit.gako.brainbox.features.auth.AuthScene
import edu.cit.gako.brainbox.features.home.HomeScene
import edu.cit.gako.brainbox.features.notebook.editor.NotebookEditorScreen
import edu.cit.gako.brainbox.features.home.flashcards.FlashcardStudyScreen
import edu.cit.gako.brainbox.features.home.quizzes.QuizStudyScreen
import edu.cit.gako.brainbox.features.playback.ui.PlaybackOverlay

@Composable
fun BrainBoxApp(
    state: AppState,
    homeDependencies: HomeDependencies,
    onLogin: (String, String) -> Unit,
    onGoogleLogin: (String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onSendResetCode: (String) -> Unit,
    onVerifyResetCode: (String) -> Unit,
    onResetPassword: (String) -> Unit,
    onAuthStageChange: (AuthStage) -> Unit,
    onTabSelected: (HomeTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onCloseNotebookEditor: () -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenQuizFromNotebook: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onOpenFlashcardDeckFromNotebook: (String) -> Unit,
    onExitStudySession: () -> Unit,
    onRecordQuizAttempt: (String, Int) -> Unit,
    onRecordFlashcardAttempt: (String, Int) -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit,
    onPlayNotebook: (edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary, Boolean) -> Unit = { _, _ -> },
    onSelectQueuePlaylist: (String) -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onTogglePlaybackLoop: () -> Unit = {},
    onTogglePlaybackShuffle: () -> Unit = {},
    onStartQueue: () -> Unit = {}
) {
    when {
        state.isBootstrapping -> LoadingScreen()
        !state.isAuthenticated -> AuthScene(
            state = state,
            onLogin = onLogin,
            onGoogleLogin = onGoogleLogin,
            onRegister = onRegister,
            onSendResetCode = onSendResetCode,
            onVerifyResetCode = onVerifyResetCode,
            onResetPassword = onResetPassword,
            onAuthStageChange = onAuthStageChange,
            onFeatureRequest = onFeatureRequest
        )
        state.activeNotebookUuid != null -> PlaybackScreen(
            state = state,
            onSelectQueuePlaylist = onSelectQueuePlaylist,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onTogglePlaybackLoop = onTogglePlaybackLoop,
            onTogglePlaybackShuffle = onTogglePlaybackShuffle,
            onStartQueue = onStartQueue
        ) {
            NotebookEditorScreen(
                notebookUuid = state.activeNotebookUuid,
                onClose = onCloseNotebookEditor,
                onOpenQuiz = onOpenQuizFromNotebook,
                onOpenFlashcardDeck = onOpenFlashcardDeckFromNotebook
            )
        }
        state.activeQuiz != null -> PlaybackScreen(
            state = state,
            onSelectQueuePlaylist = onSelectQueuePlaylist,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onTogglePlaybackLoop = onTogglePlaybackLoop,
            onTogglePlaybackShuffle = onTogglePlaybackShuffle,
            onStartQueue = onStartQueue
        ) {
            QuizStudyScreen(
                quiz = state.activeQuiz,
                onExit = onExitStudySession,
                onRecordAttempt = onRecordQuizAttempt
            )
        }
        state.activeFlashcardDeck != null -> PlaybackScreen(
            state = state,
            onSelectQueuePlaylist = onSelectQueuePlaylist,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onTogglePlaybackLoop = onTogglePlaybackLoop,
            onTogglePlaybackShuffle = onTogglePlaybackShuffle,
            onStartQueue = onStartQueue
        ) {
            FlashcardStudyScreen(
                deck = state.activeFlashcardDeck,
                onExit = onExitStudySession,
                onRecordAttempt = onRecordFlashcardAttempt
            )
        }
        else -> HomeScene(
            state = state,
            dependencies = homeDependencies,
            onTabSelected = onTabSelected,
            onCreateNotebook = onCreateNotebook,
            onOpenNotebook = onOpenNotebook,
            onOpenQuiz = onOpenQuiz,
            onOpenFlashcardDeck = onOpenFlashcardDeck,
            onLogout = onLogout,
            onFeatureRequest = onFeatureRequest,
            onPlayNotebook = onPlayNotebook,
            onSelectQueuePlaylist = onSelectQueuePlaylist,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onTogglePlaybackLoop = onTogglePlaybackLoop,
            onTogglePlaybackShuffle = onTogglePlaybackShuffle,
            onStartQueue = onStartQueue
        )
    }
}

@Composable
private fun PlaybackScreen(
    state: AppState,
    onSelectQueuePlaylist: (String) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onTogglePlaybackLoop: () -> Unit,
    onTogglePlaybackShuffle: () -> Unit,
    onStartQueue: () -> Unit,
    content: @Composable () -> Unit
) {
    var isPlaybarExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        content()
        PlaybackOverlay(
            playbackState = state.playbackState,
            playbackQueue = state.playbackQueue,
            playlists = state.homeData.playlists,
            activePlaylistUuid = state.playbackPlaylistUuid,
            activePlaylistTitle = state.playbackPlaylistTitle,
            currentQueueIndex = state.playbackPlaylistCurrentIndex,
            isLooping = state.isPlaybackLooping,
            isShuffling = state.isPlaybackShuffling,
            isExpanded = isPlaybarExpanded,
            onExpandedChange = { isPlaybarExpanded = it },
            onStartQueue = onStartQueue,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious,
            onToggleLoop = onTogglePlaybackLoop,
            onToggleShuffle = onTogglePlaybackShuffle,
            onSelectPlaylist = onSelectQueuePlaylist,
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        )
    }
}
