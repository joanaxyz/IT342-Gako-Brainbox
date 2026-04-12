package edu.cit.gako.brainbox.app

import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.FlashcardDeckSummary
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.network.models.QuizDetail
import edu.cit.gako.brainbox.network.models.QuizSummary
import edu.cit.gako.brainbox.network.models.UserProfile

enum class AuthStage {
    LOGIN,
    REGISTER,
    FORGOT_EMAIL,
    FORGOT_CODE,
    RESET_PASSWORD
}

enum class HomeTab(val label: String) {
    DASHBOARD("Dashboard"),
    LIBRARY("Library"),
    QUIZZES("Quizzes"),
    FLASHCARDS("Flashcards"),
    PLAYLISTS("Playlists"),
    PROFILE("Profile")
}

data class HomeData(
    val notebooks: List<NotebookSummary> = emptyList(),
    val recentlyEdited: List<NotebookSummary> = emptyList(),
    val recentlyReviewed: List<NotebookSummary> = emptyList(),
    val quizzes: List<QuizSummary> = emptyList(),
    val flashcards: List<FlashcardDeckSummary> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList(),
    val syncNotice: String? = null,
    val syncedAtLabel: String? = null
)

data class HomeBundle(
    val user: UserProfile,
    val homeData: HomeData
)

data class AppState(
    val isBootstrapping: Boolean = true,
    val isBusy: Boolean = false,
    val isAuthenticated: Boolean = false,
    val authStage: AuthStage = AuthStage.LOGIN,
    val pendingResetEmail: String = "",
    val resetToken: String? = null,
    val currentTab: HomeTab = HomeTab.DASHBOARD,
    val user: UserProfile? = null,
    val homeData: HomeData = HomeData(),
    val activeQuiz: QuizDetail? = null,
    val activeFlashcardDeck: FlashcardDeckDetail? = null,
    val activeNotebookUuid: String? = null,
    val activeNotebookOriginTab: HomeTab? = null,
    val studyReturnNotebookUuid: String? = null,
    val playbackState: PlaybackUiState = PlaybackUiState(),
    val offlineSyncState: OfflineSyncState = OfflineSyncState()
)
