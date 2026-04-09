package edu.cit.gako.brainbox.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.home.DashboardScreen
import edu.cit.gako.brainbox.home.FlashcardsScreen
import edu.cit.gako.brainbox.home.LibraryScreen
import edu.cit.gako.brainbox.home.PlaylistsScreen
import edu.cit.gako.brainbox.home.ProfileScreen
import edu.cit.gako.brainbox.home.QuizzesScreen
import edu.cit.gako.brainbox.home.HomeBottomBar
import edu.cit.gako.brainbox.home.HomeTopBar
import edu.cit.gako.brainbox.ui.theme.Accent
import edu.cit.gako.brainbox.ui.theme.AccentBg
import edu.cit.gako.brainbox.ui.theme.Cream

@Composable
internal fun HomeScene(
    state: AppState,
    onTabSelected: (HomeTab) -> Unit,
    onOpenQuiz: (String) -> Unit,
    onOpenFlashcardDeck: (String) -> Unit,
    onRefreshHome: () -> Unit,
    onLogout: () -> Unit,
    onFeatureRequest: (String) -> Unit
) {
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
                bottom = 112.dp
            )

            when (state.currentTab) {
                HomeTab.DASHBOARD -> DashboardScreen(
                    user = state.user,
                    homeData = state.homeData,
                    contentPadding = contentPadding,
                    onGoToTab = onTabSelected,
                    onOpenQuiz = onOpenQuiz,
                    onOpenFlashcardDeck = onOpenFlashcardDeck,
                    onFeatureRequest = onFeatureRequest
                )
                HomeTab.LIBRARY -> LibraryScreen(
                    notebooks = state.homeData.notebooks,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
                    onFeatureRequest = onFeatureRequest
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
                    playlists = state.homeData.playlists,
                    syncNotice = state.homeData.syncNotice,
                    syncedAtLabel = state.homeData.syncedAtLabel,
                    contentPadding = contentPadding,
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


