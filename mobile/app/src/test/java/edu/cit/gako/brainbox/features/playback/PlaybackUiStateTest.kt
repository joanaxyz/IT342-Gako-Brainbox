package edu.cit.gako.brainbox.features.playback.ui

import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsChunk
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaybackUiStateTest {
    @Test
    fun highlightsMiddleWord() {
        val uiState = snapshotAt(
            offset = 6,
            status = BrainBoxAudioPlaybackStatus.PLAYING
        ).toPlaybackUiState()

        assertEquals(6..9, uiState.activeWordRange)
        assertEquals(6..9, uiState.currentChunkWordRange)
    }

    @Test
    fun keepsPreviousWordAtBoundary() {
        val uiState = snapshotAt(
            offset = 5,
            status = BrainBoxAudioPlaybackStatus.PLAYING
        ).toPlaybackUiState()

        assertEquals(0..4, uiState.activeWordRange)
        assertEquals(0..4, uiState.currentChunkWordRange)
    }

    @Test
    fun highlightsFinalWordWhilePlaying() {
        val uiState = snapshotAt(
            offset = 11,
            status = BrainBoxAudioPlaybackStatus.PLAYING
        ).toPlaybackUiState()

        assertEquals(11..15, uiState.activeWordRange)
        assertEquals(11..15, uiState.currentChunkWordRange)
    }

    @Test
    fun clearsHighlightWhenPlaybackEndsAtFinalOffset() {
        val uiState = snapshotAt(
            offset = TEST_TEXT.length,
            status = BrainBoxAudioPlaybackStatus.ENDED
        ).toPlaybackUiState()

        assertEquals(TEST_TEXT.length, uiState.currentCharOffset)
        assertEquals(1f, uiState.progressFraction, 0.0001f)
        assertNull(uiState.activeWordRange)
        assertNull(uiState.currentChunkWordRange)
    }

    @Test
    fun finishedPlaybackCanResumeForReplay() {
        val uiState = snapshotAt(
            offset = TEST_TEXT.length,
            status = BrainBoxAudioPlaybackStatus.ENDED
        ).toPlaybackUiState()

        assertEquals(true, uiState.canResume)
    }

    @Test
    fun showsNextChunkTextWhenChunkAdvances() {
        val uiState = BrainBoxAudioSnapshot(
            request = MULTI_CHUNK_REQUEST,
            status = BrainBoxAudioPlaybackStatus.PLAYING,
            currentChunkIndex = 1,
            currentCharOffset = 12,
            speechRate = 1f
        ).toPlaybackUiState()

        assertEquals("Gamma delta", uiState.currentChunkText)
        assertEquals(12..16, uiState.activeWordRange)
        assertEquals(0..4, uiState.currentChunkWordRange)
    }

    @Test
    fun keepsFinalWordVisibleWhileWaitingForNextChunk() {
        val uiState = BrainBoxAudioSnapshot(
            request = MULTI_CHUNK_REQUEST,
            status = BrainBoxAudioPlaybackStatus.PLAYING,
            currentChunkIndex = 0,
            currentCharOffset = 11,
            speechRate = 1f
        ).toPlaybackUiState()

        assertEquals("Alpha beta.", uiState.currentChunkText)
        assertEquals(6..10, uiState.activeWordRange)
        assertEquals(6..10, uiState.currentChunkWordRange)
    }

    @Test
    fun clearsHighlightWhenPlayingAtFinalFullOffset() {
        val uiState = snapshotAt(
            offset = TEST_TEXT.length,
            status = BrainBoxAudioPlaybackStatus.PLAYING
        ).toPlaybackUiState()

        assertNull(uiState.activeWordRange)
        assertNull(uiState.currentChunkWordRange)
    }

    private fun snapshotAt(
        offset: Int,
        status: BrainBoxAudioPlaybackStatus
    ): BrainBoxAudioSnapshot {
        return BrainBoxAudioSnapshot(
            request = TEST_REQUEST,
            status = status,
            currentChunkIndex = 0,
            currentCharOffset = offset,
            speechRate = 1f
        )
    }

    private companion object {
        private const val TEST_TEXT = "Alpha beta gamma"

        private val TEST_REQUEST = BrainBoxTtsRequest(
            notebookId = "notebook-1",
            notebookTitle = "Notebook",
            fullText = TEST_TEXT,
            chunks = listOf(
                BrainBoxTtsChunk(
                    id = "chunk-0",
                    text = TEST_TEXT,
                    startCharIndex = 0,
                    endCharIndex = TEST_TEXT.length
                )
            )
        )

        private const val MULTI_CHUNK_TEXT = "Alpha beta. Gamma delta"

        private val MULTI_CHUNK_REQUEST = BrainBoxTtsRequest(
            notebookId = "notebook-1",
            notebookTitle = "Notebook",
            fullText = MULTI_CHUNK_TEXT,
            chunks = listOf(
                BrainBoxTtsChunk(
                    id = "chunk-0",
                    text = "Alpha beta.",
                    startCharIndex = 0,
                    endCharIndex = 11
                ),
                BrainBoxTtsChunk(
                    id = "chunk-1",
                    text = "Gamma delta",
                    startCharIndex = 12,
                    endCharIndex = MULTI_CHUNK_TEXT.length
                )
            )
        )
    }
}
