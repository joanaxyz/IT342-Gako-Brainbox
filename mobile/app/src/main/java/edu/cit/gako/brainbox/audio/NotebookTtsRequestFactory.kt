package edu.cit.gako.brainbox.audio

import android.text.Html
import edu.cit.gako.brainbox.network.models.NotebookDetail

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
        offlineOnly = offlineOnly
    )
}

fun String.htmlToPlaybackText(): String {
    return Html.fromHtml(this, Html.FROM_HTML_MODE_COMPACT).toString().trim().ifBlank {
        "This notebook is empty."
    }
}

private fun splitIntoTtsChunks(text: String, maxChars: Int = 420): List<BrainBoxTtsChunk> {
    val normalized = text.replace(Regex("\\s+"), " ").trim()
    if (normalized.isBlank()) {
        return listOf(
            BrainBoxTtsChunk(
                id = "chunk-0",
                text = "This notebook is empty.",
                startCharIndex = 0,
                endCharIndex = 22
            )
        )
    }

    val chunks = mutableListOf<BrainBoxTtsChunk>()
    var cursor = 0
    var chunkIndex = 0

    while (cursor < normalized.length) {
        val rawEnd = (cursor + maxChars).coerceAtMost(normalized.length)
        val splitEnd = if (rawEnd == normalized.length) {
            rawEnd
        } else {
            val sentenceBreak = normalized.lastIndexOfAny(charArrayOf('.', '!', '?'), startIndex = rawEnd)
            val whitespaceBreak = normalized.lastIndexOf(' ', rawEnd)
            maxOf(cursor + 1, sentenceBreak + 1, whitespaceBreak)
        }
        val chunkText = normalized.substring(cursor, splitEnd).trim()
        if (chunkText.isNotBlank()) {
            chunks += BrainBoxTtsChunk(
                id = "chunk-$chunkIndex",
                text = chunkText,
                startCharIndex = cursor,
                endCharIndex = cursor + chunkText.length
            )
            chunkIndex += 1
        }
        cursor = splitEnd.coerceAtLeast(cursor + 1)
    }

    return chunks.ifEmpty {
        listOf(
            BrainBoxTtsChunk(
                id = "chunk-0",
                text = normalized,
                startCharIndex = 0,
                endCharIndex = normalized.length
            )
        )
    }
}
