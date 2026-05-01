package edu.cit.gako.brainbox.features.playback.data

import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.playback.data.dto.PlaybackQueueResponse
import edu.cit.gako.brainbox.features.playback.data.dto.QueueAddNotebookBody
import edu.cit.gako.brainbox.features.playback.data.dto.QueueReorderBody

class PlaybackQueueRepository(private val apiService: PlaybackQueueApiService) {

    suspend fun getQueue(): List<NotebookSummary> =
        apiService.getQueueEnvelope()
            .requireData("We couldn't load the playback queue.")
            .items

    suspend fun getQueueState(): PlaybackQueueResponse =
        apiService.getQueueEnvelope()
            .requireData("We couldn't load the playback queue.")

    suspend fun setQueuePlaylist(playlistUuid: String): PlaybackQueueResponse =
        apiService.setQueuePlaylistEnvelope(playlistUuid)
            .requireData("We couldn't select that playlist.")

    suspend fun addNotebook(notebookUuid: String): List<NotebookSummary> =
        apiService.addNotebookEnvelope(QueueAddNotebookBody(notebookUuid))
            .requireData("We couldn't add that notebook to the queue.")
            .items

    suspend fun removeNotebook(notebookUuid: String): List<NotebookSummary> =
        apiService.removeNotebookEnvelope(notebookUuid)
            .requireData("We couldn't remove that notebook from the queue.")
            .items

    suspend fun clearQueue() {
        apiService.clearQueueEnvelope()
    }

    suspend fun reorderQueue(notebookUuids: List<String>): List<NotebookSummary> =
        apiService.reorderQueueEnvelope(QueueReorderBody(notebookUuids))
            .requireData("We couldn't reorder the queue.")
            .items
}
