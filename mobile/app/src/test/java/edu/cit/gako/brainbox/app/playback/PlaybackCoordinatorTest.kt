package edu.cit.gako.brainbox.app.playback

import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackCoordinatorTest {
    @Test
    fun normalizeQueueIndexClampsIntoAvailableQueue() {
        assertEquals(0, normalizeQueueIndex(-4, queueSize = 3))
        assertEquals(1, normalizeQueueIndex(1, queueSize = 3))
        assertEquals(2, normalizeQueueIndex(7, queueSize = 3))
    }

    @Test
    fun normalizeQueueIndexFallsBackToZeroForEmptyQueue() {
        assertEquals(0, normalizeQueueIndex(4, queueSize = 0))
    }

    @Test
    fun resolveQueueTargetIndexMovesSequentiallyWithinBounds() {
        assertEquals(2, resolveQueueTargetIndex(1, queueSize = 4, direction = 1, shuffle = false))
        assertEquals(0, resolveQueueTargetIndex(0, queueSize = 4, direction = -1, shuffle = false))
        assertEquals(3, resolveQueueTargetIndex(3, queueSize = 4, direction = 1, shuffle = false))
    }

    @Test
    fun resolveQueueTargetIndexUsesShuffleCandidateThatIsNotCurrent() {
        val targetIndex = resolveQueueTargetIndex(
            currentIndex = 1,
            queueSize = 4,
            direction = 1,
            shuffle = true,
            pickShuffleIndex = { candidates ->
                assertEquals(listOf(0, 2, 3), candidates)
                3
            }
        )

        assertEquals(3, targetIndex)
    }
}
