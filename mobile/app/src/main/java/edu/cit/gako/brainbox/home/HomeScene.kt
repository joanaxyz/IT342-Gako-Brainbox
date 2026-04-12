package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Cream

@Composable
internal fun HomeScene(
    state: AppState,
    onTabSelected: (HomeTab) -> Unit,
    onCreateNotebook: () -> Unit,
    onOpenNotebook: (String) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onRefreshHome: () -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit
) {
    val context = LocalContext.current
    val audioClient = remember(context.applicationContext) { BrainBoxAudioClient(context.applicationContext) }

    Scaffold(
        containerColor = Cream,
        topBar = {
            HomeTopBar(currentTab = state.currentTab)
        },
        bottomBar = {
            Column {
                GlobalPlaybar(
                    playbackState = state.playbackState,
                    onResume = audioClient::resume,
                    onPause = audioClient::pause,
                    onReplay = {
                        audioClient.seekToChunk(state.playbackState.currentChunkIndex.coerceAtLeast(0))
                    },
                    onStop = audioClient::stop
                )
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
                bottom = if (state.playbackState.isVisible) 176.dp else 112.dp
            )

            when (state.currentTab) {
                HomeTab.DASHBOARD -> DashboardScreen(
                    user = state.user,
                    homeData = state.homeData,
                    contentPadding = contentPadding,
                    onGoToTab = onTabSelected,
                    onCreateNotebook = onCreateNotebook,
                    onOpenNotebook = onOpenNotebook,
                    onOpenQuiz = onOpenQuiz,
                    onOpenFlashcardDeck = onOpenFlashcardDeck,
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
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onOpenNotebook = onOpenNotebook,
                    onFeatureRequest = onFeatureRequest
                )
                HomeTab.PROFILE -> ProfileScreen(
                    user = state.user,
                    homeData = state.homeData,
                    contentPadding = contentPadding,
                    onRefreshHome = onRefreshHome,
                    onLogout = onLogout
                )
            }
        }
    }
}
