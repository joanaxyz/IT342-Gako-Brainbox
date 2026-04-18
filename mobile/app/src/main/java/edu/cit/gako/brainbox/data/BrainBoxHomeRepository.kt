package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.app.HomeBundle
import edu.cit.gako.brainbox.app.HomeData
import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.SessionManager
import edu.cit.gako.brainbox.network.models.ApiEnvelope
import edu.cit.gako.brainbox.network.models.UserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class BrainBoxHomeRepository(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun loadHome(): HomeBundle = supervisorScope {
        val profileDeferred = async {
            loadSection("We couldn't load your profile.") { getUserProfileEnvelope() }
        }
        val notebooksDeferred = async {
            loadSection("We couldn't load notebooks.") { getNotebooksEnvelope() }
        }
        val editedDeferred = async {
            loadSection("We couldn't load recently edited notebooks.") {
                getRecentlyEditedNotebooksEnvelope()
            }
        }
        val reviewedDeferred = async {
            loadSection("We couldn't load recently reviewed notebooks.") {
                getRecentlyReviewedNotebooksEnvelope()
            }
        }
        val quizzesDeferred = async {
            loadSection("We couldn't load quizzes.") { getQuizzesEnvelope() }
        }
        val flashcardsDeferred = async {
            loadSection("We couldn't load flashcards.") { getFlashcardsEnvelope() }
        }
        val playlistsDeferred = async {
            loadSection("We couldn't load playlists.") { getPlaylistsEnvelope() }
        }
        val queueDeferred = async {
            loadSection("We couldn't load the playback queue.") { getQueueEnvelope() }
        }

        val profileResult = profileDeferred.await()
        val notebooksResult = notebooksDeferred.await()
        val editedResult = editedDeferred.await()
        val reviewedResult = reviewedDeferred.await()
        val quizzesResult = quizzesDeferred.await()
        val flashcardsResult = flashcardsDeferred.await()
        val playlistsResult = playlistsDeferred.await()
        val queueResult = queueDeferred.await()

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
                    ?: IllegalStateException("We couldn't sync the home data.")
                )
        }

        val issues = mutableListOf<String>()
        val notebooks = notebooksResult.getOrElse {
            issues += "library"
            emptyList()
        }
        val recentlyEdited = editedResult.getOrElse {
            issues += "recent edits"
            emptyList()
        }
        val recentlyReviewed = reviewedResult.getOrElse {
            issues += "continue learning"
            emptyList()
        }
        val quizzes = quizzesResult.getOrElse {
            issues += "quizzes"
            emptyList()
        }
        val flashcards = flashcardsResult.getOrElse {
            issues += "flashcards"
            emptyList()
        }
        val playlists = playlistsResult.getOrElse {
            issues += "playlists"
            emptyList()
        }
        val playbackQueue = queueResult.getOrNull()?.items ?: emptyList()

        val user = profileResult.getOrElse { fallbackUser() }
        val notice = when (issues.size) {
            0 -> null
            1 -> "The ${issues.first()} section is still syncing."
            else -> "Some sections are still syncing on mobile."
        }

        HomeBundle(
            user = user,
            homeData = HomeData(
                notebooks = notebooks,
                recentlyEdited = recentlyEdited,
                recentlyReviewed = recentlyReviewed,
                quizzes = quizzes,
                flashcards = flashcards,
                playlists = playlists,
                playbackQueue = playbackQueue,
                syncNotice = notice,
                syncedAtLabel = "Updated ${timestampLabel()}"
            )
        )
    }

    suspend fun markNotebookReviewed(uuid: String) {
        apiService.updateReviewEnvelope(uuid)
            .requireSuccess("We couldn't mark that notebook as reviewed.")
    }

    private suspend fun <T> loadSection(
        fallbackMessage: String,
        block: suspend ApiService.() -> ApiEnvelope<T>
    ): Result<T> {
        return runCatching {
            apiService.block().requireData(fallbackMessage)
        }
    }

    private fun fallbackUser(): UserProfile {
        return UserProfile(
            username = sessionManager.fetchUsername().orEmpty().ifBlank { "BrainBox User" },
            email = "",
            createdAt = null
        )
    }

    private fun timestampLabel(): String {
        return ZonedDateTime.now().format(
            DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.ENGLISH)
        )
    }
}

