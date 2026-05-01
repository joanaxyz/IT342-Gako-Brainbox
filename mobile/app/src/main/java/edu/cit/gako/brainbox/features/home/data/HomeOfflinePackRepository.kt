package edu.cit.gako.brainbox.features.home.data

import edu.cit.gako.brainbox.features.notebook.data.NotebookRepository
import edu.cit.gako.brainbox.features.notebook.offline.BrainBoxOfflineRepository
import edu.cit.gako.brainbox.features.notebook.offline.OfflinePackDownloadResult
import edu.cit.gako.brainbox.features.notebook.offline.downloadOfflinePack
import edu.cit.gako.brainbox.features.notebook.offline.removeOfflinePackAssets
import edu.cit.gako.brainbox.features.playback.tts.BrainBoxOfflineAudioCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HomeOfflinePackRepository(
    private val notebooks: NotebookRepository,
    private val offlineRepository: BrainBoxOfflineRepository,
    private val audioCache: BrainBoxOfflineAudioCache
) {
    fun observeActiveOfflineNotebookUuids(): Flow<Set<String>> =
        offlineRepository.observeActiveOfflinePacks()
            .map { packs -> packs.map { it.notebookUuid }.toSet() }

    suspend fun downloadNotebookPack(notebookUuid: String): Result<OfflinePackDownloadResult> =
        downloadOfflinePack(
            notebookUuid = notebookUuid,
            repository = notebooks,
            offlineRepository = offlineRepository,
            audioCache = audioCache
        )

    suspend fun removeNotebookPackAssets(notebookUuid: String) {
        removeOfflinePackAssets(
            notebookUuid = notebookUuid,
            offlineRepository = offlineRepository,
            audioCache = audioCache
        )
    }
}
