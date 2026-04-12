package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.network.models.ApiEnvelope
import edu.cit.gako.brainbox.network.models.CategoryDetail
import edu.cit.gako.brainbox.network.models.NotebookContentSaveRequest
import edu.cit.gako.brainbox.network.models.NotebookDetail
import edu.cit.gako.brainbox.network.models.NotebookUpdateRequest
import edu.cit.gako.brainbox.network.models.NotebookVersionItem
import edu.cit.gako.brainbox.network.models.OfflineNotebookBundle
import edu.cit.gako.brainbox.network.models.OfflineNotebookBundleRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface NotebookApiService {
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

    @PATCH("api/notebooks/update-review/{uuid}")
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

    @POST("api/mobile/offline-bundles/notebooks")
    suspend fun getOfflineNotebookBundleEnvelope(
        @Body request: OfflineNotebookBundleRequest
    ): ApiEnvelope<OfflineNotebookBundle>
}
