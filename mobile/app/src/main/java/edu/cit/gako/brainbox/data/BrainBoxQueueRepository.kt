package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.QueueAddNotebookBody
import edu.cit.gako.brainbox.network.models.QueueReorderBody

internal class BrainBoxQueueRepository(private val apiService: ApiService) {

    suspend fun getQueue(): List<NotebookSummary> =
        apiService.getQueueEnvelope()
            .requireData("We couldn't load the playback queue.")
            .items

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
