package edu.cit.gako.brainbox.app.playback

import android.content.Context
import edu.cit.gako.brainbox.app.AppState
import edu.cit.gako.brainbox.app.HomeTab
import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioClient
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioResumePoint
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioPlaybackStatus
import edu.cit.gako.brainbox.features.playback.audio.BrainBoxAudioStore
import edu.cit.gako.brainbox.features.playback.model.BrainBoxAudioSnapshot
import edu.cit.gako.brainbox.features.playback.model.BrainBoxTtsRequest
import edu.cit.gako.brainbox.features.playback.tts.buildNotebookTtsRequest
import edu.cit.gako.brainbox.features.playback.ui.toPlaybackUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PlaylistPlaybackContext(
    val playlistUuid: String,
    val playbackNotebookUuids: List<String>,
    val playlistNotebookUuids: List<String>
)

internal class PlaybackCoordinator(
    appContext: Context,
    private val scope: CoroutineScope,
    private val notebookRepository: NotebookRepository,
    private val audioStore: BrainBoxAudioStore,
    private val getState: () -> AppState,
    private val setState: (AppState) -> Unit,
    private val showMessage: (String) -> Unit
) {
    private val audioClient = BrainBoxAudioClient(appContext)
    private var playlistPlaybackContext: PlaylistPlaybackContext? = null
    private var hasPreparedAudioService = false

    fun start() {
        observePlaybackState()
    }

    fun prepareAudioService() {
        if (hasPreparedAudioService) {
            return
        }
        hasPreparedAudioService = true
        runCatching { audioClient.prepare() }
            .onFailure { hasPreparedAudioService = false }
    }

    fun installPlaylistPlaybackContext(
        playlistUuid: String?,
        playbackOrder: List<NotebookSummary>,
        playlistOrder: List<NotebookSummary>
    ) {
        playlistPlaybackContext = buildPlaylistPlaybackContext(playlistUuid, playbackOrder, playlistOrder)
    }

    fun handlePlayNotebook(notebook: NotebookSummary, switchToListenTab: Boolean = false) {
        playNotebook(
            notebook = notebook,
            switchToListenTab = switchToListenTab,
            forceReload = false,
            playlistContext = null
        )
    }

    fun handlePlayCollection(
        playlistUuid: String?,
        orderedNotebooks: List<NotebookSummary>,
        playlistOrder: List<NotebookSummary>
    ) {
        if (orderedNotebooks.isEmpty()) {
            return
        }

        val state = getState()
        val playbackOrder = orderedNotebooks.distinctBy { it.uuid }
        val playlistContext = buildPlaylistPlaybackContext(playlistUuid, playbackOrder, playlistOrder)
        val playlistTitle = state.homeData.playlists
            .firstOrNull { it.uuid == playlistUuid }
            ?.title

        playlistPlaybackContext = playlistContext
        applyPlaybackQueue(
            queue = playlistOrder.ifEmpty { playbackOrder },
            playlistUuid = playlistUuid,
            playlistTitle = playlistTitle,
            currentIndex = 0
        )

        playNotebook(
            notebook = playbackOrder.first(),
            switchToListenTab = true,
            forceReload = true,
            playlistContext = playlistContext
        )
    }

    fun handleStartQueue() {
        prepareAudioService()
        advanceQueuePlayback(openListenTab = true)
    }

    fun handleSkipNext() {
        advanceQueuePlayback(openListenTab = false)
    }

    fun handleSkipPrevious() {
        advanceQueuePlayback(openListenTab = false, direction = -1)
    }

    fun handleSelectQueuePlaylist(playlistUuid: String) {
        val state = getState()
        val playlist = state.homeData.playlists.firstOrNull { it.uuid == playlistUuid } ?: return
        playlistPlaybackContext = buildPlaylistPlaybackContext(playlistUuid, playlist.queue, playlist.queue)
        applyPlaybackQueue(
            queue = playlist.queue,
            playlistUuid = playlist.uuid,
            playlistTitle = playlist.title,
            currentIndex = 0
        )
    }

    fun handleTogglePlaybackLoop() {
        val state = getState()
        setState(state.copy(isPlaybackLooping = !state.isPlaybackLooping))
    }

    fun handleTogglePlaybackShuffle() {
        val state = getState()
        val nextShuffle = !state.isPlaybackShuffling
        setState(state.copy(isPlaybackShuffling = nextShuffle))
    }

    private fun observePlaybackState() {
        scope.launch {
            audioStore.snapshotFlow.collect { snapshot ->
                val state = getState()
                val wasPlaying = state.playbackState.isPlaying
                setState(state.copy(playbackState = snapshot.toPlaybackUiState()))
                val nextState = getState()
                if (wasPlaying && snapshot.status == BrainBoxAudioPlaybackStatus.ENDED) {
                    if (nextState.isPlaybackLooping) {
                        val currentNotebook = nextState.playbackState.notebookId?.let { notebookUuid ->
                            nextState.playbackQueue.firstOrNull { it.uuid == notebookUuid }
                                ?: nextState.homeData.notebooks.firstOrNull { it.uuid == notebookUuid }
                        }
                        if (currentNotebook != null) {
                            playNotebook(
                                notebook = currentNotebook,
                                switchToListenTab = false,
                                forceReload = true,
                                playlistContext = playlistPlaybackContext
                            )
                        }
                    } else if (nextState.playbackQueue.isNotEmpty()) {
                        handleSkipNext()
                    } else {
                        clearPlaylistPlaybackContext()
                    }
                } else if (snapshot.status == BrainBoxAudioPlaybackStatus.ENDED) {
                    clearPlaylistPlaybackContext()
                }
            }
        }
    }

    private fun applyPlaybackQueue(
        queue: List<NotebookSummary>,
        playlistUuid: String? = getState().playbackPlaylistUuid,
        playlistTitle: String? = getState().playbackPlaylistTitle,
        currentIndex: Int = getState().playbackPlaylistCurrentIndex
    ) {
        val safeCurrentIndex = normalizeQueueIndex(currentIndex, queue.size)
        val state = getState()
        setState(
            state.copy(
                playbackQueue = queue,
                playbackPlaylistUuid = playlistUuid,
                playbackPlaylistTitle = playlistTitle,
                playbackPlaylistCurrentIndex = safeCurrentIndex,
                homeData = state.homeData.copy(
                    playbackQueue = queue,
                    playbackPlaylistUuid = playlistUuid,
                    playbackPlaylistTitle = playlistTitle,
                    playbackPlaylistCurrentIndex = safeCurrentIndex
                )
            )
        )
    }

    private fun advanceQueuePlayback(openListenTab: Boolean, direction: Int = 1) {
        val state = getState()
        val queue = state.playbackQueue
        if (queue.isEmpty()) {
            return
        }

        val currentIndex = state.playbackState.notebookId
            ?.let { notebookId -> queue.indexOfFirst { it.uuid == notebookId } }
            ?.takeIf { it >= 0 }
            ?: state.playbackPlaylistCurrentIndex.coerceIn(0, queue.lastIndex)
        val targetIndex = resolveQueueTargetIndex(
            currentIndex = currentIndex,
            queueSize = queue.size,
            direction = direction,
            shuffle = state.isPlaybackShuffling
        )
        if (targetIndex == currentIndex && state.playbackState.isVisible) {
            return
        }

        val next = queue[targetIndex]
        val contextForNext = playlistPlaybackContext?.takeIf { next.uuid in it.playbackNotebookUuids } ?: run {
            state.playbackPlaylistUuid?.let { playlistUuid ->
                buildPlaylistPlaybackContext(playlistUuid, queue, queue)
            }
        }

        applyPlaybackQueue(currentIndex = targetIndex, queue = queue)
        playNotebook(
            notebook = next,
            switchToListenTab = openListenTab,
            forceReload = true,
            playlistContext = contextForNext
        )
    }

    private fun playNotebook(
        notebook: NotebookSummary,
        switchToListenTab: Boolean,
        forceReload: Boolean,
        playlistContext: PlaylistPlaybackContext?
    ) {
        val state = getState()
        val isSameNotebook = state.playbackState.notebookId == notebook.uuid && state.playbackState.isVisible
        if (!forceReload && isSameNotebook) {
            when {
                state.playbackState.isPlaying -> audioClient.pause()
                state.playbackState.canResume -> audioClient.resume()
            }
            if (switchToListenTab) {
                setState(getState().copy(currentTab = HomeTab.PLAYLISTS))
            }
            return
        }

        if (playlistContext == null) {
            clearPlaylistPlaybackContext()
        } else {
            playlistPlaybackContext = playlistContext
        }

        if (switchToListenTab) {
            setState(getState().copy(currentTab = HomeTab.PLAYLISTS))
        }

        prepareAudioService()
        scope.launch {
            val resolvedNotebook = resolveNotebookForPlayback(notebook.uuid)
            if (resolvedNotebook == null) {
                if (playlistContext != null) {
                    clearPlaylistPlaybackContext()
                }
                showMessage("Couldn't load \"${notebook.title}\" for playback.")
                return@launch
            }

            val detail = resolvedNotebook
            runCatching { notebookRepository.markNotebookReviewed(detail.uuid) }
            val request = withContext(Dispatchers.Default) {
                buildNotebookTtsRequest(detail, detail.content)
                    .withResumePoint(audioStore.resumePointFor(detail.uuid))
            }
            publishLoadingPlaybackState(request)
            audioClient.play(request)
            playlistContext?.let { context ->
                persistPlaylistCurrentIndex(context, detail.uuid)
            }
        }
    }

    private suspend fun resolveNotebookForPlayback(uuid: String): NotebookDetail? =
        runCatching { notebookRepository.getNotebook(uuid) }.getOrNull()

    private fun BrainBoxTtsRequest.withResumePoint(resumePoint: BrainBoxAudioResumePoint?): BrainBoxTtsRequest {
        if (resumePoint == null || resumePoint.notebookId != notebookId || chunks.isEmpty()) {
            return this
        }

        val chunkIndex = resumePoint.currentChunkIndex.coerceIn(0, chunks.lastIndex)
        val chunk = chunks[chunkIndex]
        return copy(
            startChunkIndex = chunkIndex,
            startCharOffset = resumePoint.currentCharOffset.coerceIn(chunk.startCharIndex, chunk.endCharIndex),
            speechRate = resumePoint.speechRate.coerceIn(0.25f, 3.0f)
        )
    }

    private fun buildPlaylistPlaybackContext(
        playlistUuid: String?,
        playbackOrder: List<NotebookSummary>,
        playlistOrder: List<NotebookSummary>
    ): PlaylistPlaybackContext? {
        if (playlistUuid.isNullOrBlank()) {
            return null
        }

        val playbackNotebookUuids = playbackOrder.map { it.uuid }.distinct()
        val playlistNotebookUuids = playlistOrder.map { it.uuid }.distinct()
        if (playbackNotebookUuids.isEmpty() || playlistNotebookUuids.isEmpty()) {
            return null
        }

        return PlaylistPlaybackContext(
            playlistUuid = playlistUuid,
            playbackNotebookUuids = playbackNotebookUuids,
            playlistNotebookUuids = playlistNotebookUuids
        )
    }

    private fun persistPlaylistCurrentIndex(
        context: PlaylistPlaybackContext,
        notebookUuid: String
    ) {
        val index = context.playlistNotebookUuids.indexOf(notebookUuid)
        if (index < 0) {
            return
        }

        applyPlaybackQueue(currentIndex = index, queue = getState().playbackQueue)
        applyPlaylistCurrentIndex(context.playlistUuid, index)
    }

    private fun applyPlaylistCurrentIndex(playlistUuid: String, index: Int) {
        val state = getState()
        setState(
            state.copy(
                playbackPlaylistCurrentIndex = if (state.playbackPlaylistUuid == playlistUuid) {
                    index
                } else {
                    state.playbackPlaylistCurrentIndex
                },
                homeData = state.homeData.copy(
                    playlists = state.homeData.playlists.map { playlist ->
                        if (playlist.uuid == playlistUuid) {
                            playlist.copy(currentIndex = index)
                        } else {
                            playlist
                        }
                    }
                )
            )
        )
    }

    private fun clearPlaylistPlaybackContext() {
        playlistPlaybackContext = null
    }

    private fun publishLoadingPlaybackState(request: BrainBoxTtsRequest) {
        val snapshot = BrainBoxAudioSnapshot(
            request = request,
            status = BrainBoxAudioPlaybackStatus.LOADING,
            currentChunkIndex = request.startChunkIndex,
            currentCharOffset = request.startCharOffset,
            speechRate = request.speechRate,
            updatedAtEpochMs = System.currentTimeMillis()
        )
        audioStore.publishSnapshot(snapshot)
        setState(getState().copy(playbackState = snapshot.toPlaybackUiState()))
    }
}

internal fun normalizeQueueIndex(index: Int, queueSize: Int): Int {
    return if (queueSize <= 0) 0 else index.coerceIn(0, queueSize - 1)
}

internal fun resolveQueueTargetIndex(
    currentIndex: Int,
    queueSize: Int,
    direction: Int,
    shuffle: Boolean,
    pickShuffleIndex: (List<Int>) -> Int = { candidates -> candidates.random() }
): Int {
    if (queueSize <= 0) {
        return 0
    }

    val safeCurrentIndex = normalizeQueueIndex(currentIndex, queueSize)
    if (direction > 0 && shuffle && queueSize > 1) {
        val candidates = (0 until queueSize).filter { it != safeCurrentIndex }
        return normalizeQueueIndex(pickShuffleIndex(candidates), queueSize)
    }

    return normalizeQueueIndex(safeCurrentIndex + direction, queueSize)
}

