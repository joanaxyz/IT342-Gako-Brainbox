package edu.cit.gako.brainbox.features.home

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.features.home.dashboard.DashboardScreen
import edu.cit.gako.brainbox.features.home.flashcards.FlashcardsScreen
import edu.cit.gako.brainbox.features.home.library.LibraryScreen
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.features.playback.ui.CollapsedPlaybar
import edu.cit.gako.brainbox.features.playback.ui.GlobalPlaybar
import edu.cit.gako.brainbox.features.playback.ui.player.nextPlaybackRate
import edu.cit.gako.brainbox.features.home.playlists.PlaylistsScreen
import edu.cit.gako.brainbox.features.home.profile.ProfileScreen
import edu.cit.gako.brainbox.features.home.quizzes.QuizzesScreen
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.shared.ui.theme.Accent
import edu.cit.gako.brainbox.shared.ui.theme.AccentBg
import edu.cit.gako.brainbox.shared.ui.theme.Cream

@Composable
internal fun HomeScene(
    state: AppState,
    dependencies: HomeDependencies,
    onTabSelected: (HomeTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit,
    onPlayNotebook: (NotebookSummary, Boolean) -> Unit = { _, _ -> },
    onSelectQueuePlaylist: (String) -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onTogglePlaybackLoop: () -> Unit = {},
    onTogglePlaybackShuffle: () -> Unit = {},
    onStartQueue: () -> Unit = {}
) {
    val context = LocalContext.current
    val audioClient = remember(context.applicationContext) { BrainBoxAudioClient(context.applicationContext) }
    var isPlaybarExpanded by rememberSaveable { mutableStateOf(false) }
    val shouldUseFullscreenPlayer =
        (state.playbackState.isVisible || state.playbackQueue.isNotEmpty()) && isPlaybarExpanded

    Scaffold(
        containerColor = Cream,
        topBar = {
            if (!shouldUseFullscreenPlayer) {
                HomeTopBar(currentTab = state.currentTab)
            }
        },
        bottomBar = {
            if (!shouldUseFullscreenPlayer) {
                HomeBottomBar(
                    currentTab = state.currentTab,
                    onTabSelected = onTabSelected
                )
            }
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
                    state.playbackState.isVisible || state.playbackQueue.isNotEmpty() -> 140.dp
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
                    onPlayNotebook = { notebook -> onPlayNotebook(notebook, true) }
                )
                HomeTab.LIBRARY -> LibraryScreen(
                    repository = dependencies.libraryRepository,
                    notebooks = state.homeData.notebooks,
                    contentPadding = contentPadding,
                    onOpenNotebook = onOpenNotebook
                )
                HomeTab.QUIZZES -> QuizzesScreen(
                    quizzes = state.homeData.quizzes,
                    notebooks = state.homeData.notebooks,
                    contentPadding = contentPadding,
                    onOpenQuiz = onOpenQuiz
                )
                HomeTab.FLASHCARDS -> FlashcardsScreen(
                    flashcards = state.homeData.flashcards,
                    notebooks = state.homeData.notebooks,
                    contentPadding = contentPadding,
                    onOpenFlashcardDeck = onOpenFlashcardDeck
                )
                HomeTab.PLAYLISTS -> PlaylistsScreen(
                    repository = dependencies.playlistRepository,
                    notebooks = state.homeData.notebooks,
                    playlists = state.homeData.playlists,
                    playbackState = state.playbackState,
                    contentPadding = contentPadding,
                    onPlayNotebook = { notebook -> onPlayNotebook(notebook, false) },
                    onFeatureRequest = onFeatureRequest
                )
                HomeTab.PROFILE -> ProfileScreen(
                    user = state.user,
                    homeData = state.homeData,
                    contentPadding = contentPadding,
                    onLogout = onLogout
                )
            }

            val shouldShowPlaybarOverlay =
                state.playbackState.isVisible || state.playbackQueue.isNotEmpty()

            if (shouldShowPlaybarOverlay) {
                if (isPlaybarExpanded) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.BottomCenter)
                    ) {
                        GlobalPlaybar(
                            playbackState = state.playbackState,
                            queueSize = state.playbackQueue.size,
                            playbackQueue = state.playbackQueue,
                            playlists = state.homeData.playlists,
                            activePlaylistUuid = state.playbackPlaylistUuid,
                            activePlaylistTitle = state.playbackPlaylistTitle,
                            currentQueueIndex = state.playbackPlaylistCurrentIndex,
                            isLooping = state.isPlaybackLooping,
                            isShuffling = state.isPlaybackShuffling,
                            onResume = audioClient::resume,
                            onPause = audioClient::pause,
                            onReplay = {
                                audioClient.seekToChunk(state.playbackState.currentChunkIndex.coerceAtLeast(0))
                            },
                            onCycleRate = {
                                audioClient.setSpeechRate(nextPlaybackRate(state.playbackState.speechRate))
                            },
                            onStartQueue = onStartQueue,
                            onSkipNext = onSkipNext,
                            onSkipPrevious = onSkipPrevious,
                            onToggleLoop = onTogglePlaybackLoop,
                            onToggleShuffle = onTogglePlaybackShuffle,
                            onSelectPlaylist = onSelectQueuePlaylist,
                            onCollapse = { isPlaybarExpanded = false },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    CollapsedPlaybar(
                        playbackState = state.playbackState,
                        title = state.playbackState.notebookTitle.ifBlank {
                            state.playbackPlaylistTitle ?: "Playlist ready"
                        },
                        onExpand = { isPlaybarExpanded = true },
                        onPlayPause = {
                            if (state.playbackState.isPlaying) audioClient.pause()
                            else if (state.playbackState.canResume) audioClient.resume()
                            else onStartQueue()
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
