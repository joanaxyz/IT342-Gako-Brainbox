package edu.cit.gako.brainbox.audio

import android.util.Base64

object BrainBoxAudioCodec {
    private const val FIELD_SEPARATOR = "|"

    fun encodeRequest(request: BrainBoxTtsRequest): String {
        val header = listOf(
            encodeText(request.notebookId),
            encodeText(request.notebookTitle),
            encodeText(request.languageTag.orEmpty()),
            encodeText(request.voiceName.orEmpty()),
            request.speechRate.toString(),
            request.startChunkIndex.toString(),
            request.startCharOffset.toString(),
            request.offlineOnly.toString()
        ).joinToString(FIELD_SEPARATOR)

        val chunkLines = request.chunks.joinToString("\n") { chunk ->
            listOf(
                encodeText(chunk.id),
                chunk.startCharIndex.toString(),
                chunk.endCharIndex.toString(),
                encodeText(chunk.text)
            ).joinToString(FIELD_SEPARATOR)
        }

        return if (chunkLines.isBlank()) {
            header
        } else {
            buildString {
                append(header)
                append('\n')
                append(chunkLines)
            }
        }
    }

    fun decodeRequest(encoded: String?): BrainBoxTtsRequest? {
        if (encoded.isNullOrBlank()) return null

        val lines = encoded.split('\n').filter { it.isNotBlank() }
        if (lines.isEmpty()) return null

        val header = lines.first().split(FIELD_SEPARATOR)
        if (header.size < 8) return null

        val chunks = lines.drop(1).mapNotNull { line ->
            val parts = line.split(FIELD_SEPARATOR)
            if (parts.size < 4) return@mapNotNull null
            BrainBoxTtsChunk(
                id = decodeText(parts[0]),
                startCharIndex = parts[1].toIntOrNull() ?: 0,
                endCharIndex = parts[2].toIntOrNull() ?: 0,
                text = decodeText(parts[3])
            )
        }

        return BrainBoxTtsRequest(
            notebookId = decodeText(header[0]),
            notebookTitle = decodeText(header[1]),
            languageTag = decodeText(header[2]).ifBlank { null },
            voiceName = decodeText(header[3]).ifBlank { null },
            speechRate = header[4].toFloatOrNull() ?: 1.0f,
            startChunkIndex = header[5].toIntOrNull() ?: 0,
            startCharOffset = header[6].toIntOrNull() ?: 0,
            offlineOnly = header[7].toBooleanStrictOrNull() ?: false,
            chunks = chunks
        )
    }

    private fun encodeText(value: String): String {
        return Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun decodeText(value: String): String {
        if (value.isBlank()) return ""
        return String(Base64.decode(value, Base64.NO_WRAP), Charsets.UTF_8)
    }
}
