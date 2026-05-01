package edu.cit.gako.brainbox.features.playback.data.dto

import com.google.gson.annotations.SerializedName
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary

data class PlaybackQueueResponse(
    @SerializedName("playlistUuid") val playlistUuid: String? = null,
    @SerializedName("playlistTitle") val playlistTitle: String? = null,
    @SerializedName("currentIndex") val currentIndex: Int = 0,
    @SerializedName("items") val items: List<NotebookSummary> = emptyList()
)

data class QueueAddNotebookBody(
    @SerializedName("notebookUuid") val notebookUuid: String
)

data class QueueReorderBody(
    @SerializedName("notebookUuids") val notebookUuids: List<String>
)
