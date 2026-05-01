package edu.cit.gako.brainbox.features.home.playlists.data.dto

import com.google.gson.annotations.SerializedName
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary

data class PlaylistSummary(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("currentIndex") val currentIndex: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("queue") val queue: List<NotebookSummary> = emptyList()
)

data class PlaylistCreateRequest(
    @SerializedName("title") val title: String
)

data class PlaylistAddNotebookBody(
    @SerializedName("notebookUuid") val notebookUuid: String
)

data class PlaylistReorderBody(
    @SerializedName("notebookUuids") val notebookUuids: List<String>
)
