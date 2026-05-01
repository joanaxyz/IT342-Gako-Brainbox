package edu.cit.gako.brainbox.features.notebook.offline

import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.playback.tts.BrainBoxOfflineAudioCache
import edu.cit.gako.brainbox.features.playback.tts.buildNotebookTtsRequest

data class OfflinePackDownloadResult(
    val notebookUuid: String,
    val notebookTitle: String,
    val quizCount: Int,
    val flashcardCount: Int,
    val playlistCount: Int,
    val audioPrepared: Boolean,
    val audioChunkCount: Int
)

suspend fun downloadOfflinePack(
    notebookUuid: String,
    repository: NotebookRepository,
    offlineRepository: BrainBoxOfflineRepository,
    audioCache: BrainBoxOfflineAudioCache,
    pinned: Boolean = true
): Result<OfflinePackDownloadResult> {
    return runCatching {
        val bundle = repository.getOfflineBundle(listOf(notebookUuid))
        val item = bundle.notebooks.firstOrNull { it.notebook.uuid == notebookUuid }
            ?: error("We couldn't find the notebook bundle for offline use.")
        offlineRepository.saveOfflineBundle(
            bundleItem = item,
            pinned = pinned
        )
        val audioPreparation = audioCache.prepare(
            buildNotebookTtsRequest(
                notebook = item.notebook,
                html = item.notebook.content,
                offlineOnly = true
            )
        )
        OfflinePackDownloadResult(
            notebookUuid = item.notebook.uuid,
            notebookTitle = item.notebook.title,
            quizCount = item.quizzes.size,
            flashcardCount = item.flashcards.size,
            playlistCount = item.playlists.size,
            audioPrepared = audioPreparation.isSuccess,
            audioChunkCount = audioPreparation.getOrNull()?.chunkCount ?: 0
        )
    }
}

suspend fun removeOfflinePackAssets(
    notebookUuid: String,
    offlineRepository: BrainBoxOfflineRepository,
    audioCache: BrainBoxOfflineAudioCache
) {
    offlineRepository.removeOfflinePack(notebookUuid)
    audioCache.deleteNotebookAudio(notebookUuid)
}
