package edu.cit.gako.brainbox.features.playback.tts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TtsTextTimingTest {
    @Test
    fun nextSpeakableOffsetSkipsWhitespaceAndPunctuation() {
        assertEquals(4, nextSpeakableRelativeOffset("... Start", 0))
    }

    @Test
    fun endOfSpeakableTextIgnoresTrailingPunctuation() {
        assertTrue(isAtEndOfSpeakableText("Done.", 4))
        assertFalse(isAtEndOfSpeakableText("Done. Next", 4))
    }

    @Test
    fun sentenceBoundaryDetectsNearbyPunctuation() {
        assertTrue(isLikelySentenceBoundaryPause("First. Second", 6))
    }
}
