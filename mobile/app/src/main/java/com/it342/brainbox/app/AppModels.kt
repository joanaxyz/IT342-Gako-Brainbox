package com.it342.brainbox.app

import com.it342.brainbox.network.models.FlashcardDeckDetail
import com.it342.brainbox.network.models.FlashcardDeckSummary
import com.it342.brainbox.network.models.NotebookSummary
import com.it342.brainbox.network.models.PlaylistSummary
import com.it342.brainbox.network.models.QuizDetail
import com.it342.brainbox.network.models.QuizSummary
import com.it342.brainbox.network.models.UserProfile

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
    val activeFlashcardDeck: FlashcardDeckDetail? = null
)
