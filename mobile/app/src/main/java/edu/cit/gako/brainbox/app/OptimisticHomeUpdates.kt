package edu.cit.gako.brainbox.app

import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.library.data.withNotebookCategory
import edu.cit.gako.brainbox.features.home.library.data.withoutCategory
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import kotlin.math.max

internal fun List<PlaylistSummary>.withQueuedNotebookCategory(
    notebookUuid: String,
    category: CategoryDetail?
): List<PlaylistSummary> =
    map { playlist ->
        playlist.copy(queue = playlist.queue.withNotebookCategory(notebookUuid, category))
    }

internal fun List<PlaylistSummary>.withoutQueuedCategory(
    categoryId: Long,
    deleteNotebooks: Boolean
): List<PlaylistSummary> =
    map { playlist ->
        val nextQueue = playlist.queue.withoutCategory(categoryId, deleteNotebooks)
        playlist.copy(
            queue = nextQueue,
            currentIndex = normalizePlaylistIndex(playlist.currentIndex, nextQueue.size)
        )
    }

internal fun HomeData.withNotebookCategory(
    notebookUuid: String,
    category: CategoryDetail?
): HomeData =
    copy(
        notebooks = notebooks.withNotebookCategory(notebookUuid, category),
        recentlyEdited = recentlyEdited.withNotebookCategory(notebookUuid, category),
        recentlyReviewed = recentlyReviewed.withNotebookCategory(notebookUuid, category),
        playlists = playlists.withQueuedNotebookCategory(notebookUuid, category),
        playbackQueue = playbackQueue.withNotebookCategory(notebookUuid, category)
    )

internal fun HomeData.withoutCategory(
    categoryId: Long,
    deleteNotebooks: Boolean
): HomeData =
    copy(
        notebooks = notebooks.withoutCategory(categoryId, deleteNotebooks),
        recentlyEdited = recentlyEdited.withoutCategory(categoryId, deleteNotebooks),
        recentlyReviewed = recentlyReviewed.withoutCategory(categoryId, deleteNotebooks),
        playlists = playlists.withoutQueuedCategory(categoryId, deleteNotebooks),
        playbackQueue = playbackQueue.withoutCategory(categoryId, deleteNotebooks)
    )

internal fun HomeData.withQuizAttempt(uuid: String, score: Int): HomeData =
    copy(quizzes = quizzes.map { quiz -> quiz.withAttempt(uuid, score) })

internal fun HomeData.withFlashcardAttempt(uuid: String, mastery: Int): HomeData =
    copy(flashcards = flashcards.map { deck -> deck.withAttempt(uuid, mastery) })

private fun QuizSummary.withAttempt(uuid: String, score: Int): QuizSummary =
    if (this.uuid == uuid) {
        copy(bestScore = max(score, bestScore ?: score), attempts = attempts + 1)
    } else {
        this
    }

private fun FlashcardDeckSummary.withAttempt(uuid: String, mastery: Int): FlashcardDeckSummary =
    if (this.uuid == uuid) {
        copy(bestMastery = max(mastery, bestMastery ?: mastery), attempts = attempts + 1)
    } else {
        this
    }

private fun normalizePlaylistIndex(currentIndex: Int, queueSize: Int): Int =
    if (queueSize <= 0) 0 else currentIndex.coerceIn(0, queueSize - 1)
