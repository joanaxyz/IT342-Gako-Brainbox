package edu.cit.gako.brainbox.features.playback.ui

import edu.cit.gako.brainbox.features.playback.ui.player.buildLiveSubtitleWords
import edu.cit.gako.brainbox.features.playback.ui.player.findLiveSubtitleWordIndex
import org.junit.Assert.assertEquals
import org.junit.Test

class GlobalPlaybarTest {
    @Test
    fun subtitleWordsKeepFullTextOffsets() {
        val words = buildLiveSubtitleWords("Adapter wraps a facade")

        assertEquals("Adapter", words[0].text)
        assertEquals(0, words[0].start)
        assertEquals(7, words[0].endExclusive)
        assertEquals("facade", words[3].text)
        assertEquals(16, words[3].start)
        assertEquals(22, words[3].endExclusive)
    }

    @Test
    fun subtitleActiveIndexFollowsFinalWordInFullText() {
        val text = "Common Integration Patterns Adapter Pattern wraps a system interface " +
            "to match a target interface. Facade Pattern creates a simple facade"
        val words = buildLiveSubtitleWords(text)
        val activeStart = text.lastIndexOf("facade")

        val activeIndex = findLiveSubtitleWordIndex(
            words = words,
            activeWordRange = activeStart until activeStart + "facade".length
        )

        assertEquals("facade", words[activeIndex].text)
    }

    @Test
    fun subtitleHasNoActiveIndexWhenPlaybackEnded() {
        val words = buildLiveSubtitleWords("Adapter wraps a facade")

        assertEquals(-1, findLiveSubtitleWordIndex(words, null))
    }
}
