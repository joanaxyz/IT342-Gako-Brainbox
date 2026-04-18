package edu.cit.gako.brainbox.data.offline

import edu.cit.gako.brainbox.data.BrainBoxRepository

data class OfflinePackDownloadResult(
    val notebookUuid: String,
    val notebookTitle: String,
    val quizCount: Int,
    val flashcardCount: Int,
    val playlistCount: Int
)

suspend fun downloadOfflinePack(
    notebookUuid: String,
    repository: BrainBoxRepository,
    offlineRepository: BrainBoxOfflineRepository,
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
        OfflinePackDownloadResult(
            notebookUuid = item.notebook.uuid,
            notebookTitle = item.notebook.title,
            quizCount = item.quizzes.size,
            flashcardCount = item.flashcards.size,
            playlistCount = item.playlists.size
        )
    }
}
