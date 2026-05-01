package edu.cit.gako.brainbox.features.notebook.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryCreateRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDeleteRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookContentSaveRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookUpdateRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookVersionItem
import edu.cit.gako.brainbox.features.notebook.data.dto.OfflineNotebookBundle
import edu.cit.gako.brainbox.features.notebook.data.dto.OfflineNotebookBundleRequest
import java.util.UUID
import retrofit2.Response

class NotebookRepository(
    private val apiService: NotebookApiService,
    private val gson: Gson = Gson()
) {
    suspend fun getNotebooks(): List<NotebookSummary> {
        return apiService.getNotebooksEnvelope()
            .requireData("We couldn't load notebooks.")
    }

    suspend fun getRecentlyEditedNotebooks(): List<NotebookSummary> {
        return apiService.getRecentlyEditedNotebooksEnvelope()
            .requireData("We couldn't load recently edited notebooks.")
    }

    suspend fun getRecentlyReviewedNotebooks(): List<NotebookSummary> {
        return apiService.getRecentlyReviewedNotebooksEnvelope()
            .requireData("We couldn't load recently reviewed notebooks.")
    }

    suspend fun getNotebook(uuid: String): NotebookDetail {
        return apiService.getNotebookEnvelope(uuid)
            .requireData("We couldn't load that notebook.")
            .normalize()
    }

    suspend fun createNotebook(
        title: String,
        categoryId: Long? = null,
        content: String = ""
    ): NotebookMutationResult {
        return parseNotebookMutation(
            apiService.createNotebookEnvelope(
                NotebookUpdateRequest(
                    title = title,
                    categoryId = categoryId,
                    content = content,
                    clientMutationId = UUID.randomUUID().toString()
                )
            ),
            fallbackMessage = "We couldn't create that notebook yet."
        )
    }

    suspend fun updateNotebook(
        uuid: String,
        title: String? = null,
        categoryId: Long? = null,
        baseVersion: Long? = null,
        clientMutationId: String = UUID.randomUUID().toString()
    ): NotebookMutationResult {
        return parseNotebookMutation(
            apiService.updateNotebookEnvelope(
                uuid,
                NotebookUpdateRequest(
                    title = title,
                    categoryId = categoryId,
                    baseVersion = baseVersion,
                    clientMutationId = clientMutationId
                )
            ),
            fallbackMessage = "We couldn't update that notebook yet."
        )
    }

    suspend fun saveNotebookContent(
        uuid: String,
        content: String,
        baseVersion: Long? = null,
        clientMutationId: String = UUID.randomUUID().toString()
    ): NotebookMutationResult {
        return parseNotebookMutation(
            apiService.saveNotebookContentEnvelope(
                uuid,
                NotebookContentSaveRequest(
                    content = content,
                    baseVersion = baseVersion,
                    clientMutationId = clientMutationId
                )
            ),
            fallbackMessage = "We couldn't save your notebook yet."
        )
    }

    suspend fun createNotebookVersion(
        uuid: String,
        content: String
    ): NotebookVersionItem {
        return apiService.createNotebookVersionEnvelope(
            uuid,
            NotebookContentSaveRequest(content = content)
        ).requireData("We couldn't create a version checkpoint.")
    }

    suspend fun getNotebookVersions(uuid: String): List<NotebookVersionItem> {
        return apiService.getNotebookVersionsEnvelope(uuid)
            .requireData("We couldn't load notebook history.")
            .sortedByDescending { it.version.orEmpty() }
    }

    suspend fun restoreNotebookVersion(uuid: String, versionId: Long): NotebookMutationResult {
        return parseNotebookMutation(
            apiService.restoreNotebookVersionEnvelope(uuid, versionId),
            fallbackMessage = "We couldn't restore that version."
        )
    }

    suspend fun getCategories(): List<CategoryDetail> {
        return apiService.getCategoriesEnvelope()
            .requireData("We couldn't load categories.")
    }

    suspend fun createCategory(name: String): CategoryDetail {
        return apiService.createCategoryEnvelope(CategoryCreateRequest(name))
            .requireData("We couldn't create that category.")
    }

    suspend fun deleteCategory(categoryId: Long, deleteNotebooks: Boolean = false) {
        apiService.deleteCategoryEnvelope(
            categoryId,
            CategoryDeleteRequest(deleteNotebooks = deleteNotebooks)
        ).requireSuccess("We couldn't delete that category.")
    }

    suspend fun getOfflineBundle(notebookUuids: List<String>): OfflineNotebookBundle {
        return apiService.getOfflineNotebookBundleEnvelope(
            OfflineNotebookBundleRequest(notebookUuids.distinct().filter { it.isNotBlank() })
        ).requireData("We couldn't prepare those notebooks for offline use.")
    }

    suspend fun markNotebookReviewed(
        uuid: String,
        baseVersion: Long? = null,
        clientMutationId: String = UUID.randomUUID().toString()
    ): NotebookMutationResult {
        return parseMutationWithoutNotebook(
            apiService.updateNotebookReviewEnvelope(
                uuid,
                NotebookUpdateRequest(
                    baseVersion = baseVersion,
                    clientMutationId = clientMutationId
                )
            ),
            fallbackMessage = "We couldn't mark that notebook as reviewed."
        )
    }

    suspend fun deleteNotebook(
        uuid: String,
        baseVersion: Long? = null,
        clientMutationId: String = UUID.randomUUID().toString()
    ): NotebookMutationResult {
        return parseMutationWithoutNotebook(
            apiService.deleteNotebookEnvelope(
                uuid,
                NotebookUpdateRequest(
                    baseVersion = baseVersion,
                    clientMutationId = clientMutationId
                )
            ),
            fallbackMessage = "We couldn't delete that notebook."
        )
    }

    private fun parseNotebookMutation(
        response: Response<ApiEnvelope<NotebookDetail>>,
        fallbackMessage: String
    ): NotebookMutationResult {
        if (response.isSuccessful) {
            val notebook = response.body()?.data?.normalize()
                ?: return NotebookMutationResult.Failure(fallbackMessage)
            return NotebookMutationResult.Success(notebook)
        }

        if (response.code() == 409) {
            val latestNotebook = parseConflictNotebook(response.errorBody()?.string())
            return NotebookMutationResult.Conflict(
                message = "Your notebook changed somewhere else before this save finished.",
                latestNotebook = latestNotebook
            )
        }

        return NotebookMutationResult.Failure(
            parseFailureMessage(response.errorBody()?.string(), fallbackMessage)
        )
    }

    private fun parseMutationWithoutNotebook(
        response: Response<ApiEnvelope<Any?>>,
        fallbackMessage: String
    ): NotebookMutationResult {
        if (response.isSuccessful) {
            return NotebookMutationResult.Success(null)
        }

        if (response.code() == 409) {
            val latestNotebook = parseConflictNotebook(response.errorBody()?.string())
            return NotebookMutationResult.Conflict(
                message = "Your notebook changed somewhere else before this action finished.",
                latestNotebook = latestNotebook
            )
        }

        return NotebookMutationResult.Failure(
            parseFailureMessage(response.errorBody()?.string(), fallbackMessage)
        )
    }

    private fun parseConflictNotebook(rawBody: String?): NotebookDetail? {
        if (rawBody.isNullOrBlank()) {
            return null
        }

        return runCatching {
            val root = gson.fromJson(rawBody, JsonObject::class.java)
            val errorObject = root.getAsJsonObject("error")
            parseNotebookFromDetails(errorObject?.get("details"))
        }.getOrNull()
    }

    private fun parseNotebookFromDetails(details: JsonElement?): NotebookDetail? {
        if (details == null || details.isJsonNull) {
            return null
        }

        if (details.isJsonObject) {
            val objectValue = details.asJsonObject

            listOf("serverNotebook", "notebook", "latestNotebook").forEach { key ->
                if (objectValue.has(key)) {
                    return gson.fromJson(objectValue.get(key), NotebookDetail::class.java).normalize()
                }
            }

            return runCatching {
                gson.fromJson(objectValue, NotebookDetail::class.java).normalize()
            }.getOrNull()
        }

        return null
    }

    private fun parseFailureMessage(rawBody: String?, fallbackMessage: String): String {
        if (rawBody.isNullOrBlank()) {
            return fallbackMessage
        }

        return runCatching {
            val root = gson.fromJson(rawBody, JsonObject::class.java)
            root.getAsJsonObject("error")?.get("message")?.asString
        }.getOrNull().orEmpty().ifBlank { fallbackMessage }
    }
}

sealed interface NotebookMutationResult {
    data class Success(val notebook: NotebookDetail?) : NotebookMutationResult
    data class Conflict(
        val message: String,
        val latestNotebook: NotebookDetail?
    ) : NotebookMutationResult
    data class Failure(val message: String) : NotebookMutationResult
}

internal fun NotebookDetail.normalize(): NotebookDetail {
    return copy(
        title = title.ifBlank { "Untitled notebook" },
        content = content.ifBlank { "<p></p>" }
    )
}
