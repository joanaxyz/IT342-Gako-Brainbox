package edu.cit.gako.brainbox.features.playback.tts

import kotlin.math.roundToLong

private const val ESTIMATED_CHARS_PER_SECOND = 15f

internal fun estimatedTtsDurationMs(characterCount: Int, speechRate: Float): Long {
    if (characterCount <= 0) {
        return 0L
    }
    return (
        characterCount.toFloat() /
            (ESTIMATED_CHARS_PER_SECOND * speechRate.coerceAtLeast(0.25f)) *
            1000f
        ).roundToLong()
}

internal fun isAtEndOfSpeakableText(text: String, relativeEndOffset: Int): Boolean {
    val safeEndOffset = relativeEndOffset.coerceIn(0, text.length)
    return text
        .substring(safeEndOffset)
        .none { it.isLetterOrDigit() }
}

internal fun isLikelySentenceBoundaryPause(text: String, relativeOffset: Int): Boolean {
    val safeOffset = relativeOffset.coerceIn(0, text.length)

    if (safeOffset > 0) {
        for (index in (safeOffset - 1) downTo 0) {
            val char = text[index]
            if (char.isSentencePunctuation()) {
                return true
            }
            if (char.isLetterOrDigit()) {
                break
            }
        }
    }

    for (index in safeOffset..text.lastIndex) {
        val char = text[index]
        if (char.isSentencePunctuation()) {
            return true
        }
        if (char.isLetterOrDigit()) {
            break
        }
    }

    return false
}

private fun Char.isSentencePunctuation(): Boolean {
    return this == '.' || this == '!' || this == '?' || this == ';' || this == ':'
}

internal fun nextSpeakableRelativeOffset(text: String, relativeOffset: Int): Int {
    val safeOffset = relativeOffset.coerceIn(0, text.length)
    val nextSpeakableIndex = text.indexOfFirstFrom(safeOffset) { it.isLetterOrDigit() }
    return if (nextSpeakableIndex >= 0) nextSpeakableIndex else text.length
}

private inline fun String.indexOfFirstFrom(startIndex: Int, predicate: (Char) -> Boolean): Int {
    for (index in startIndex.coerceIn(0, length)..lastIndex) {
        if (predicate(this[index])) {
            return index
        }
    }
    return -1
}
