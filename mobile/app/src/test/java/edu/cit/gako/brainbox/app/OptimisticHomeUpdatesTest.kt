package edu.cit.gako.brainbox.app

import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.playlists.data.withNotebookInPlaylist
import edu.cit.gako.brainbox.features.home.playlists.data.withPlaylistQueueOrder
import edu.cit.gako.brainbox.features.home.playlists.data.withoutNotebookInPlaylist
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class OptimisticHomeUpdatesTest {
    private val science = CategoryDetail(id = 10L, name = "Science")
    private val math = CategoryDetail(id = 20L, name = "Math")
    private val notebookOne = NotebookSummary(
        uuid = "notebook-1",
        title = "Biology",
        categoryId = science.id,
        categoryName = science.name
    )
    private val notebookTwo = NotebookSummary(uuid = "notebook-2", title = "History")
    private val notebookThree = NotebookSummary(
        uuid = "notebook-3",
        title = "Algebra",
        categoryId = math.id,
        categoryName = math.name
    )

    @Test
    fun notebookCategoryMoveUpdatesHomeListsAndQueues() {
        val homeData = HomeData(
            notebooks = listOf(notebookOne, notebookTwo),
            recentlyEdited = listOf(notebookOne),
            recentlyReviewed = listOf(notebookOne),
            playlists = listOf(PlaylistSummary(uuid = "playlist-1", title = "Study", queue = listOf(notebookOne))),
            playbackQueue = listOf(notebookOne)
        )

        val moved = homeData.withNotebookCategory(notebookOne.uuid, math)

        assertEquals(math.id, moved.notebooks.first().categoryId)
        assertEquals(math.name, moved.recentlyEdited.first().categoryName)
        assertEquals(math.id, moved.playlists.first().queue.first().categoryId)
        assertEquals(math.name, moved.playbackQueue.first().categoryName)
    }

    @Test
    fun categoryDeleteCanUncategorizeOrDeleteAffectedNotebooks() {
        val homeData = HomeData(
            notebooks = listOf(notebookOne, notebookTwo, notebookThree),
            playlists = listOf(
                PlaylistSummary(
                    uuid = "playlist-1",
                    title = "Study",
                    currentIndex = 2,
                    queue = listOf(notebookOne, notebookTwo, notebookThree)
                )
            ),
            playbackQueue = listOf(notebookOne, notebookThree)
        )

        val uncategorized = homeData.withoutCategory(science.id, deleteNotebooks = false)
        val deleted = homeData.withoutCategory(science.id, deleteNotebooks = true)

        assertNull(uncategorized.notebooks.first { it.uuid == notebookOne.uuid }.categoryId)
        assertNull(uncategorized.playlists.first().queue.first { it.uuid == notebookOne.uuid }.categoryName)
        assertFalse(deleted.notebooks.any { it.uuid == notebookOne.uuid })
        assertFalse(deleted.playlists.first().queue.any { it.uuid == notebookOne.uuid })
        assertEquals(1, deleted.playlists.first().currentIndex)
    }

    @Test
    fun playlistHelpersAddRemoveAndReorderQueues() {
        val playlists = listOf(
            PlaylistSummary(
                uuid = "playlist-1",
                title = "Study",
                queue = listOf(notebookOne, notebookTwo)
            )
        )

        val added = playlists.withNotebookInPlaylist("playlist-1", notebookThree)
        val reordered = added.withPlaylistQueueOrder(
            playlistUuid = "playlist-1",
            notebookUuids = listOf(notebookThree.uuid, notebookOne.uuid, notebookTwo.uuid)
        )
        val removed = reordered.withoutNotebookInPlaylist("playlist-1", notebookOne.uuid)

        assertEquals(listOf(notebookOne.uuid, notebookTwo.uuid, notebookThree.uuid), added.first().queue.map { it.uuid })
        assertEquals(listOf(notebookThree.uuid, notebookOne.uuid, notebookTwo.uuid), reordered.first().queue.map { it.uuid })
        assertEquals(listOf(notebookThree.uuid, notebookTwo.uuid), removed.first().queue.map { it.uuid })
    }

    @Test
    fun studyAttemptsUpdateVisibleStats() {
        val homeData = HomeData(
            quizzes = listOf(QuizSummary(uuid = "quiz-1", title = "Quiz", attempts = 2L, bestScore = 70)),
            flashcards = listOf(
                FlashcardDeckSummary(uuid = "deck-1", title = "Deck", attempts = 4L, bestMastery = 60)
            )
        )

        val withQuizAttempt = homeData.withQuizAttempt("quiz-1", 85)
        val withFlashcardAttempt = homeData.withFlashcardAttempt("deck-1", 55)

        assertEquals(3L, withQuizAttempt.quizzes.first().attempts)
        assertEquals(85, withQuizAttempt.quizzes.first().bestScore)
        assertEquals(5L, withFlashcardAttempt.flashcards.first().attempts)
        assertEquals(60, withFlashcardAttempt.flashcards.first().bestMastery)
    }
}
