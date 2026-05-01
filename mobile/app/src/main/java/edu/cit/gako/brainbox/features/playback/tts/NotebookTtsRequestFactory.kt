package edu.cit.gako.brainbox.features.playback.tts

import android.text.Html
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsChunk
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest

fun buildNotebookTtsRequest(
    notebook: NotebookDetail,
    html: String,
    offlineOnly: Boolean
): BrainBoxTtsRequest {
    val plainText = html.htmlToPlaybackText()
    val chunks = splitIntoTtsChunks(plainText)
    return BrainBoxTtsRequest(
        notebookId = notebook.uuid,
        notebookTitle = notebook.title,
        chunks = chunks,
        fullText = plainText,
        offlineOnly = offlineOnly
    )
}

fun String.htmlToPlaybackText(): String {
    val plainText = Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString()
    return normalizePlaybackText(plainText).ifBlank {
        "This notebook is empty."
    }
}

private fun normalizePlaybackText(text: String): String {
    return text
        .replace('\u00A0', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
}

private fun splitIntoTtsChunks(text: String, maxChars: Int = 220): List<BrainBoxTtsChunk> {
    val normalized = normalizePlaybackText(text)
    if (normalized.isBlank()) {
        return listOf(
            BrainBoxTtsChunk(
                id = "chunk-0",
                text = "This notebook is empty.",
                startCharIndex = 0,
                endCharIndex = "This notebook is empty.".length
            )
        )
    }

    val sentencePieces = mutableListOf<IntRange>()
    val sentenceRegex = Regex("[^.!?]+(?:[.!?]+|$)")
    sentenceRegex.findAll(normalized).forEach { match ->
        val rawSentence = match.value
        val rawStart = match.range.first
        val leadingTrim = rawSentence.length - rawSentence.trimStart().length
        val trailingTrim = rawSentence.length - rawSentence.trimEnd().length
        val start = rawStart + leadingTrim
        val endExclusive = match.range.last + 1 - trailingTrim

        if (endExclusive <= start) {
            return@forEach
        }

        if ((endExclusive - start) > maxChars) {
            sentencePieces += splitLongSentence(normalized, start, endExclusive, maxChars)
        } else {
            sentencePieces += IntRange(start, endExclusive - 1)
        }
    }

    val mergedChunks = mutableListOf<IntRange>()
    sentencePieces.forEach { piece ->
        val current = mergedChunks.lastOrNull()
        if (current == null) {
            mergedChunks += piece
        } else if ((piece.last + 1 - current.first) <= maxChars) {
            mergedChunks[mergedChunks.lastIndex] = IntRange(current.first, piece.last)
        } else {
            mergedChunks += piece
        }
    }

    return mergedChunks.mapIndexed { index, range ->
        BrainBoxTtsChunk(
            id = "chunk-$index",
            text = normalized.substring(range.first, range.last + 1),
            startCharIndex = range.first,
            endCharIndex = range.last + 1
        )
    }
}

private fun splitLongSentence(
    normalized: String,
    start: Int,
    endExclusive: Int,
    maxChars: Int
): List<IntRange> {
    val pieces = mutableListOf<IntRange>()
    val wordRegex = Regex("\\S+\\s*")
    var currentStart = -1
    var currentEndExclusive = -1

    wordRegex.findAll(normalized.substring(start, endExclusive)).forEach { match ->
        val rawStart = start + match.range.first
        val trimmedToken = match.value.trim()
        if (trimmedToken.isBlank()) {
            return@forEach
        }

        val leadingTrim = match.value.length - match.value.trimStart().length
        val tokenStart = rawStart + leadingTrim
        val tokenEndExclusive = tokenStart + trimmedToken.length

        if (currentStart < 0) {
            currentStart = tokenStart
            currentEndExclusive = tokenEndExclusive
            return@forEach
        }

        if ((tokenEndExclusive - currentStart) > maxChars) {
            pieces += IntRange(currentStart, currentEndExclusive - 1)
            currentStart = tokenStart
        }

        currentEndExclusive = tokenEndExclusive
    }

    if (currentStart >= 0 && currentEndExclusive > currentStart) {
        pieces += IntRange(currentStart, currentEndExclusive - 1)
    }

    return pieces
}
