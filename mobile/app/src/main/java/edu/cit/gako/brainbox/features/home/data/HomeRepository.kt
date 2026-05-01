package edu.cit.gako.brainbox.features.home.data

import edu.cit.gako.brainbox.app.HomeBundle
import edu.cit.gako.brainbox.app.HomeData
import edu.cit.gako.brainbox.features.auth.data.AuthRepository
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.home.playlists.data.PlaylistRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.platform.network.SessionManager
import edu.cit.gako.brainbox.features.home.profile.data.dto.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class HomeRepository(
    private val authRepository: AuthRepository,
    private val notebookRepository: NotebookRepository,
    private val quizRepository: QuizRepository,
    private val flashcardRepository: FlashcardRepository,
    private val playlistRepository: PlaylistRepository,
    private val sessionManager: SessionManager
) {
    suspend fun loadHome(): HomeBundle = supervisorScope {
        val profileDeferred = async {
            loadSection { authRepository.loadProfile() }
        }
        val notebooksDeferred = async {
            loadSection { notebookRepository.getNotebooks() }
        }
        val editedDeferred = async {
            loadSection { notebookRepository.getRecentlyEditedNotebooks() }
        }
        val reviewedDeferred = async {
            loadSection { notebookRepository.getRecentlyReviewedNotebooks() }
        }
        val quizzesDeferred = async {
            loadSection { quizRepository.getQuizzes() }
        }
        val flashcardsDeferred = async {
            loadSection { flashcardRepository.getFlashcards() }
        }
        val playlistsDeferred = async {
            loadSection { playlistRepository.getPlaylists() }
        }

        val profileResult = profileDeferred.await()
        val notebooksResult = notebooksDeferred.await()
        val editedResult = editedDeferred.await()
        val reviewedResult = reviewedDeferred.await()
        val quizzesResult = quizzesDeferred.await()
        val flashcardsResult = flashcardsDeferred.await()
        val playlistsResult = playlistsDeferred.await()

        val allFailed = listOf(
            profileResult,
            notebooksResult,
            editedResult,
            reviewedResult,
            quizzesResult,
            flashcardsResult,
            playlistsResult
        ).all { it.isFailure }

        if (allFailed) {
            throw (
                profileResult.exceptionOrNull()
                    ?: notebooksResult.exceptionOrNull()
                    ?: IllegalStateException("We couldn't load the home data.")
                )
        }

        val notebooks = notebooksResult.getOrElse {
            emptyList()
        }
        val recentlyEdited = editedResult.getOrElse {
            emptyList()
        }
        val recentlyReviewed = reviewedResult.getOrElse {
            emptyList()
        }
        val quizzes = quizzesResult.getOrElse {
            emptyList()
        }
        val flashcards = flashcardsResult.getOrElse {
            emptyList()
        }
        val playlists = playlistsResult.getOrElse {
            emptyList()
        }

        val user = profileResult.getOrElse { fallbackUser() }
        HomeBundle(
            user = user,
            homeData = HomeData(
                notebooks = notebooks,
                recentlyEdited = recentlyEdited,
                recentlyReviewed = recentlyReviewed,
                quizzes = quizzes,
                flashcards = flashcards,
                playlists = playlists
            )
        )
    }

    private suspend fun <T> loadSection(block: suspend () -> T): Result<T> = runCatching { block() }

    private fun fallbackUser(): UserProfile {
        return UserProfile(
            username = sessionManager.fetchUsername().orEmpty().ifBlank { "BrainBox User" },
            email = "",
            createdAt = null
        )
    }

}

