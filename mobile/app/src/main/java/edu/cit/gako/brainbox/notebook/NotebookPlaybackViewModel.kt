package edu.cit.gako.brainbox.notebook

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import edu.cit.gako.brainbox.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.audio.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.audio.BrainBoxAudioStore
import edu.cit.gako.brainbox.audio.BrainBoxTtsRequest
import edu.cit.gako.brainbox.audio.PlaybackUiState
import edu.cit.gako.brainbox.audio.buildNotebookTtsRequest
import edu.cit.gako.brainbox.audio.htmlToPlaybackText
import edu.cit.gako.brainbox.audio.toPlaybackUiState
import edu.cit.gako.brainbox.network.models.NotebookDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private data class NotebookPlaybackDocument(
    val notebook: NotebookDetail? = null,
    val html: String = "",
    val offlineOnly: Boolean = false,
    val request: BrainBoxTtsRequest? = null
)

class NotebookPlaybackViewModel(
    appContext: Context,
    audioStore: BrainBoxAudioStore
) : ViewModel() {
    private val audioClient = BrainBoxAudioClient(appContext)
    private val notebookState = MutableStateFlow(NotebookPlaybackDocument())

    val playbackUiState: StateFlow<PlaybackUiState> = audioStore.snapshotFlow
        .map { it.toPlaybackUiState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), BrainBoxAudioSnapshot().toPlaybackUiState())

    val reviewUiState: StateFlow<ReviewUiState> = combine(notebookState, playbackUiState) { notebookDoc, playback ->
        val plainText = notebookDoc.html.htmlToPlaybackText()
        ReviewUiState(
            plainText = plainText,
            headings = extractOutlineItems(notebookDoc.html),
            headingTargets = buildHeadingTargets(
                headings = extractOutlineItems(notebookDoc.html),
                plainText = plainText,
                request = notebookDoc.request
            ),
            activeWordRange = if (playback.notebookId == notebookDoc.notebook?.uuid) playback.activeWordRange else null,
            activeChunkIndex = playback.currentChunkIndex,
            totalChunks = playback.totalChunks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ReviewUiState())

    fun bindNotebook(notebook: NotebookDetail?, html: String, offlineOnly: Boolean) {
        notebookState.value = NotebookPlaybackDocument(
            notebook = notebook,
            html = html,
            offlineOnly = offlineOnly,
            request = notebook?.let { buildNotebookTtsRequest(it, html, offlineOnly) }
        )
    }

    fun playNotebook(notebook: NotebookDetail, html: String, offlineOnly: Boolean) {
        val request = buildNotebookTtsRequest(notebook = notebook, html = html, offlineOnly = offlineOnly)
        notebookState.value = NotebookPlaybackDocument(
            notebook = notebook,
            html = html,
            offlineOnly = offlineOnly,
            request = request
        )
        audioClient.play(request)
    }

    fun togglePlayback() {
        if (playbackUiState.value.isPlaying) {
            audioClient.pause()
        } else {
            audioClient.resume()
        }
    }

    fun stopPlayback() {
        audioClient.stop()
    }

    fun setSpeechRate(rate: Float) {
        audioClient.setSpeechRate(rate)
    }

    fun seekToChunk(chunkIndex: Int) {
        audioClient.seekToChunk(chunkIndex)
    }

    companion object {
        fun factory(
            appContext: Context,
            audioStore: BrainBoxAudioStore
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NotebookPlaybackViewModel(
                        appContext = appContext,
                        audioStore = audioStore
                    ) as T
                }
            }
        }
    }
}

private fun buildHeadingTargets(
    headings: List<String>,
    plainText: String,
    request: BrainBoxTtsRequest?
): List<ReviewHeadingTarget> {
    val chunks = request?.chunks.orEmpty()
    if (headings.isEmpty()) {
        return emptyList()
    }

    return headings.mapIndexed { index, heading ->
        val fallbackChunkIndex = if (chunks.isEmpty()) {
            0
        } else {
            ((index.toFloat() / headings.size.toFloat()) * chunks.lastIndex)
                .toInt()
                .coerceIn(0, chunks.lastIndex)
        }
        val headingPosition = plainText.indexOf(heading, ignoreCase = true)
        val chunkIndex = if (headingPosition >= 0 && chunks.isNotEmpty()) {
            chunks.indexOfFirst { headingPosition <= it.endCharIndex }
                .takeIf { it >= 0 }
                ?: fallbackChunkIndex
        } else {
            fallbackChunkIndex
        }
        ReviewHeadingTarget(
            title = heading,
            chunkIndex = chunkIndex
        )
    }
}
