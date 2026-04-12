package edu.cit.gako.brainbox.network.models

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class NotebookDetail(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("content") val content: String = "",
    @SerializedName("wordCount") val wordCount: Int? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("lastReviewedAt") val lastReviewedAt: String? = null,
    @SerializedName("version") val version: Long? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("categoryName") val categoryName: String? = null
)

data class CategoryDetail(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class NotebookUpdateRequest(
    @SerializedName("title") val title: String? = null,
    @SerializedName("categoryId") val categoryId: Long? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("baseVersion") val baseVersion: Long? = null,
    @SerializedName("clientMutationId") val clientMutationId: String? = null
)

data class NotebookContentSaveRequest(
    @SerializedName("content") val content: String,
    @SerializedName("baseVersion") val baseVersion: Long? = null,
    @SerializedName("clientMutationId") val clientMutationId: String? = null
)

data class NotebookVersionItem(
    @SerializedName("id") val id: Long,
    @SerializedName("content") val content: String? = null,
    @SerializedName("version") val version: String? = null
)

data class OfflineNotebookBundleRequest(
    @SerializedName("notebookUuids") val notebookUuids: List<String>
)

data class PlaylistReference(
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("notebookUuid") val notebookUuid: String? = null
)

data class OfflineNotebookBundleItem(
    @SerializedName("notebook") val notebook: NotebookDetail,
    @SerializedName("quizzes") val quizzes: List<QuizDetail> = emptyList(),
    @SerializedName("flashcards") val flashcards: List<FlashcardDeckDetail> = emptyList(),
    @SerializedName("playlists") val playlists: List<PlaylistSummary> = emptyList()
)

data class OfflineNotebookBundle(
    @SerializedName("notebooks") val notebooks: List<OfflineNotebookBundleItem> = emptyList(),
    @SerializedName("missingUuids") val missingUuids: List<String> = emptyList()
)

data class ConflictDetailsEnvelope(
    @SerializedName("serverNotebook") val serverNotebook: NotebookDetail? = null,
    @SerializedName("details") val rawDetails: JsonElement? = null
)
