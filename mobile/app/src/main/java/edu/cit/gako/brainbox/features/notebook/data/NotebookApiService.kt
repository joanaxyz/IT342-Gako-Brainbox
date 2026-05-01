package edu.cit.gako.brainbox.features.notebook.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryCreateRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDeleteRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.CategoryDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookContentSaveRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookDetail
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookSummary
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookUpdateRequest
import edu.cit.gako.brainbox.features.notebook.data.dto.NotebookVersionItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotebookApiService {
    @GET("api/notebooks")
    suspend fun getNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/notebooks/recently-edited")
    suspend fun getRecentlyEditedNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/notebooks/recently-reviewed")
    suspend fun getRecentlyReviewedNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/notebooks/{uuid}")
    suspend fun getNotebookEnvelope(@Path("uuid") uuid: String): ApiEnvelope<NotebookDetail>

    @POST("api/notebooks")
    suspend fun createNotebookEnvelope(
        @Body request: NotebookUpdateRequest
    ): Response<ApiEnvelope<NotebookDetail>>

    @PUT("api/notebooks/{uuid}")
    suspend fun updateNotebookEnvelope(
        @Path("uuid") uuid: String,
        @Body request: NotebookUpdateRequest
    ): Response<ApiEnvelope<NotebookDetail>>

    @PUT("api/notebooks/{uuid}/content")
    suspend fun saveNotebookContentEnvelope(
        @Path("uuid") uuid: String,
        @Body request: NotebookContentSaveRequest
    ): Response<ApiEnvelope<NotebookDetail>>

    @PATCH("api/notebooks/{uuid}/review")
    suspend fun updateNotebookReviewEnvelope(
        @Path("uuid") uuid: String,
        @Body request: NotebookUpdateRequest? = null
    ): Response<ApiEnvelope<Any?>>

    @HTTP(method = "DELETE", path = "api/notebooks/{uuid}", hasBody = true)
    suspend fun deleteNotebookEnvelope(
        @Path("uuid") uuid: String,
        @Body request: NotebookUpdateRequest? = null
    ): Response<ApiEnvelope<Any?>>

    @GET("api/notebooks/{uuid}/versions")
    suspend fun getNotebookVersionsEnvelope(
        @Path("uuid") uuid: String
    ): ApiEnvelope<List<NotebookVersionItem>>

    @GET("api/notebooks/{uuid}/versions/{versionId}")
    suspend fun getNotebookVersionEnvelope(
        @Path("uuid") uuid: String,
        @Path("versionId") versionId: Long
    ): ApiEnvelope<NotebookVersionItem>

    @POST("api/notebooks/{uuid}/versions")
    suspend fun createNotebookVersionEnvelope(
        @Path("uuid") uuid: String,
        @Body request: NotebookContentSaveRequest
    ): ApiEnvelope<NotebookVersionItem>

    @POST("api/notebooks/{uuid}/versions/{versionId}/restore")
    suspend fun restoreNotebookVersionEnvelope(
        @Path("uuid") uuid: String,
        @Path("versionId") versionId: Long
    ): Response<ApiEnvelope<NotebookDetail>>

    @GET("api/categories")
    suspend fun getCategoriesEnvelope(): ApiEnvelope<List<CategoryDetail>>

    @POST("api/categories")
    suspend fun createCategoryEnvelope(
        @Body request: CategoryCreateRequest
    ): ApiEnvelope<CategoryDetail>

    @HTTP(method = "DELETE", path = "api/categories/{id}", hasBody = true)
    suspend fun deleteCategoryEnvelope(
        @Path("id") id: Long,
        @Body request: CategoryDeleteRequest
    ): ApiEnvelope<Unit?>
}
