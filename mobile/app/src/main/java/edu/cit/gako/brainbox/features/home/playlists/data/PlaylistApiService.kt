package edu.cit.gako.brainbox.features.home.playlists.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistAddNotebookBody
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistCreateRequest
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistReorderBody
import edu.cit.gako.brainbox.features.home.playlists.data.dto.PlaylistSummary
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaylistApiService {
    @GET("api/playlists")
    suspend fun getPlaylistsEnvelope(): ApiEnvelope<List<PlaylistSummary>>

    @POST("api/playlists")
    suspend fun createPlaylistEnvelope(@Body request: PlaylistCreateRequest): ApiEnvelope<PlaylistSummary>

    @DELETE("api/playlists/{uuid}")
    suspend fun deletePlaylistEnvelope(@Path("uuid") uuid: String): ApiEnvelope<Unit?>

    @POST("api/playlists/{uuid}/notebooks")
    suspend fun addNotebookToPlaylistEnvelope(
        @Path("uuid") uuid: String,
        @Body body: PlaylistAddNotebookBody
    ): ApiEnvelope<PlaylistSummary>

    @DELETE("api/playlists/{uuid}/notebooks/{notebookUuid}")
    suspend fun removeNotebookFromPlaylistEnvelope(
        @Path("uuid") uuid: String,
        @Path("notebookUuid") notebookUuid: String
    ): ApiEnvelope<PlaylistSummary>

    @PUT("api/playlists/{uuid}/reorder")
    suspend fun reorderPlaylistEnvelope(
        @Path("uuid") uuid: String,
        @Body body: PlaylistReorderBody
    ): ApiEnvelope<PlaylistSummary>

    @PATCH("api/playlists/{uuid}/current-index")
    suspend fun setPlaylistCurrentIndexEnvelope(
        @Path("uuid") uuid: String,
        @Query("index") index: Int
    ): ApiEnvelope<PlaylistSummary>
}
