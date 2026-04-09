package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.network.models.ApiEnvelope
import edu.cit.gako.brainbox.network.models.NotebookSummary
import edu.cit.gako.brainbox.network.models.PlaylistSummary
import edu.cit.gako.brainbox.network.models.UserProfile
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface HomeApiService {
    @GET("api/user/me")
    suspend fun getUserProfile(): UserProfile

    @GET("api/user/me")
    suspend fun getUserProfileEnvelope(): ApiEnvelope<UserProfile>

    @GET("api/notebooks")
    suspend fun getNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/notebooks/recently-edited")
    suspend fun getRecentlyEditedNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/notebooks/recently-reviewed")
    suspend fun getRecentlyReviewedNotebooksEnvelope(): ApiEnvelope<List<NotebookSummary>>

    @GET("api/playlists")
    suspend fun getPlaylistsEnvelope(): ApiEnvelope<List<PlaylistSummary>>

    @PATCH("api/notebooks/update-review/{uuid}")
    suspend fun updateReviewEnvelope(@Path("uuid") uuid: String): ApiEnvelope<Any?>
}

