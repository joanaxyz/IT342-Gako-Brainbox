package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.BrainBoxAppGraph
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.audio.buildNotebookTtsRequest
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Cream
import kotlinx.coroutines.launch

@Composable
internal fun HomeScene(
    state: AppState,
    onTabSelected: (HomeTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit,
    onAddToQueue: (edu.cit.gako.brainbox.network.models.NotebookSummary) -> Unit = {},
    onRemoveFromQueue: (String) -> Unit = {},
    onSkipNext: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val audioClient = remember(context.applicationContext) { BrainBoxAudioClient(context.applicationContext) }
    val appGraph = remember(context.applicationContext) { BrainBoxAppGraph.from(context.applicationContext) }
    val repository = remember(appGraph) { appGraph.repository }
    var isPlaybarExpanded by rememberSaveable { mutableStateOf(true) }

    fun playNotebookFromDashboard(notebook: NotebookSummary) {
        if (state.playbackState.notebookId == notebook.uuid && state.playbackState.isVisible) {
            when {
                state.playbackState.isPlaying -> audioClient.pause()
                state.playbackState.canResume -> audioClient.resume()
            }
            return
        }
        scope.launch {
            runCatching { repository.getNotebook(notebook.uuid) }
                .onSuccess { audioClient.play(buildNotebookTtsRequest(it, it.content, false)) }
                .onFailure { onFeatureRequest("Couldn't load \"${notebook.title}\" for audio.") }
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            HomeTopBar(currentTab = state.currentTab)
        },
        bottomBar = {
            HomeBottomBar(
                currentTab = state.currentTab,
                onTabSelected = onTabSelected
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.isBusy) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter),
                    color = Accent,
                    trackColor = AccentBg
                )
            }

            val contentPadding = PaddingValues(
                start = 20.dp,
                top = if (state.isBusy) 18.dp else 12.dp,
                end = 20.dp,
                bottom = when {
                    state.playbackState.isVisible && isPlaybarExpanded -> 152.dp
                    else -> 112.dp
                }
            )

            when (state.currentTab) {
                HomeTab.DASHBOARD -> DashboardScreen(
                    user = state.user,
                    homeData = state.homeData,
                    playbackState = state.playbackState,
                    contentPadding = contentPadding,
                    onGoToTab = onTabSelected,
                    onCreateNotebook = onCreateNotebook,
                    onOpenNotebook = onOpenNotebook,
                    onOpenQuiz = onOpenQuiz,
                    onOpenFlashcardDeck = onOpenFlashcardDeck,
                    onPlayNotebook = ::playNotebookFromDashboard,
                    onFeatureRequest = onFeatureRequest
                )
                HomeTab.LIBRARY -> LibraryScreen(
                    notebooks = state.homeData.notebooks,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onOpenNotebook = onOpenNotebook
                )
                HomeTab.QUIZZES -> QuizzesScreen(
                    quizzes = state.homeData.quizzes,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onOpenQuiz = onOpenQuiz
                )
                HomeTab.FLASHCARDS -> FlashcardsScreen(
                    flashcards = state.homeData.flashcards,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onOpenFlashcardDeck = onOpenFlashcardDeck
                )
                HomeTab.PLAYLISTS -> PlaylistsScreen(
                    notebooks = state.homeData.notebooks,
                    playlists = state.homeData.playlists,
                    playbackState = state.playbackState,
                    playbackQueue = state.playbackQueue,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onOpenNotebook = onOpenNotebook,
                    onAddToQueue = onAddToQueue,
                    onRemoveFromQueue = onRemoveFromQueue,
                    onFeatureRequest = onFeatureRequest
                )
                HomeTab.PROFILE -> ProfileScreen(
                    user = state.user,
                    homeData = state.homeData,
                    contentPadding = contentPadding,
                    onLogout = onLogout
                )
            }

            if (state.playbackState.isVisible && !state.playbackState.notebookId.isNullOrBlank()) {
                if (isPlaybarExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    ) {
                        GlobalPlaybar(
                            playbackState = state.playbackState,
                            queueSize = state.playbackQueue.size,
                            playbackQueue = state.playbackQueue,
                            onResume = audioClient::resume,
                            onPause = audioClient::pause,
                            onReplay = {
                                audioClient.seekToChunk(state.playbackState.currentChunkIndex.coerceAtLeast(0))
                            },
                            onSkipNext = onSkipNext,
                            onRemoveFromQueue = onRemoveFromQueue,
                            onCollapse = { isPlaybarExpanded = false }
                        )
                    }
                } else {
                    CollapsedPlaybar(
                        playbackState = state.playbackState,
                        onExpand = { isPlaybarExpanded = true },
                        onPlayPause = {
                            if (state.playbackState.isPlaying) audioClient.pause()
                            else if (state.playbackState.canResume) audioClient.resume()
                            else audioClient.seekToChunk(state.playbackState.currentChunkIndex.coerceAtLeast(0))
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 12.dp)
                    )
                }
            }
        }
    }
}
