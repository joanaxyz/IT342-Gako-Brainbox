package edu.cit.gako.brainbox.features.playback.ui.player

import edu.cit.gako.brainbox.features.playback.ui.PlaybackUiState
import org.junit.Assert.assertEquals
import org.junit.Test

class PlaybackUiFormattersTest {
    @Test
    fun chunkLabelClampsIntoAvailableRange() {
        val state = PlaybackUiState(currentChunkIndex = 10, totalChunks = 3)

        assertEquals("3 / 3", buildChunkProgressLabel(state))
    }

    @Test
    fun playbackRateCyclesThroughSupportedRates() {
        assertEquals(1.25f, nextPlaybackRate(1f), 0.001f)
        assertEquals(0.5f, nextPlaybackRate(2f), 0.001f)
    }
}
