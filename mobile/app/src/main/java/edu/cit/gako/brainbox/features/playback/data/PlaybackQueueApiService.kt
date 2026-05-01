package edu.cit.gako.brainbox.features.playback.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.playback.data.dto.PlaybackQueueResponse
import edu.cit.gako.brainbox.features.playback.data.dto.QueueAddNotebookBody
import edu.cit.gako.brainbox.features.playback.data.dto.QueueReorderBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaybackQueueApiService {

    @GET("api/playback-queues/current")
    suspend fun getQueueEnvelope(): ApiEnvelope<PlaybackQueueResponse>

    @PUT("api/playback-queues/current/playlist/{playlistUuid}")
    suspend fun setQueuePlaylistEnvelope(@Path("playlistUuid") playlistUuid: String): ApiEnvelope<PlaybackQueueResponse>

    @POST("api/playback-queues/current/notebooks")
    suspend fun addNotebookEnvelope(@Body body: QueueAddNotebookBody): ApiEnvelope<PlaybackQueueResponse>

    @DELETE("api/playback-queues/current/notebooks/{notebookUuid}")
    suspend fun removeNotebookEnvelope(@Path("notebookUuid") notebookUuid: String): ApiEnvelope<PlaybackQueueResponse>

    @DELETE("api/playback-queues/current")
    suspend fun clearQueueEnvelope(): ApiEnvelope<Unit?>

    @PUT("api/playback-queues/current/reorder")
    suspend fun reorderQueueEnvelope(@Body body: QueueReorderBody): ApiEnvelope<PlaybackQueueResponse>
}
