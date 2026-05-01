package edu.cit.gako.brainbox.app

import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile


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
    PLAYLISTS("Playlist"),
    PROFILE("Profile")
}

data class HomeData(
    val notebooks: List<NotebookSummary> = emptyList(),
    val recentlyEdited: List<NotebookSummary> = emptyList(),
    val recentlyReviewed: List<NotebookSummary> = emptyList(),
    val quizzes: List<QuizSummary> = emptyList(),
    val flashcards: List<FlashcardDeckSummary> = emptyList(),
    val playlists: List<PlaylistSummary> = emptyList(),
    val playbackQueue: List<NotebookSummary> = emptyList(),
    val playbackPlaylistUuid: String? = null,
    val playbackPlaylistTitle: String? = null,
    val playbackPlaylistCurrentIndex: Int = 0
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
    val playbackQueue: List<NotebookSummary> = emptyList(),
    val playbackPlaylistUuid: String? = null,
    val playbackPlaylistTitle: String? = null,
    val playbackPlaylistCurrentIndex: Int = 0,
    val isPlaybackLooping: Boolean = false,
    val isPlaybackShuffling: Boolean = false
)
