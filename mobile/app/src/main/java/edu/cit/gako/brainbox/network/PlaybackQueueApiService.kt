package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.network.models.ApiEnvelope
import edu.cit.gako.brainbox.network.models.PlaybackQueueResponse
import edu.cit.gako.brainbox.network.models.QueueAddNotebookBody
import edu.cit.gako.brainbox.network.models.QueueReorderBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlaybackQueueApiService {

    @GET("api/queue")
    suspend fun getQueueEnvelope(): ApiEnvelope<PlaybackQueueResponse>

    @POST("api/queue/notebooks")
    suspend fun addNotebookEnvelope(@Body body: QueueAddNotebookBody): ApiEnvelope<PlaybackQueueResponse>

    @DELETE("api/queue/notebooks/{notebookUuid}")
    suspend fun removeNotebookEnvelope(@Path("notebookUuid") notebookUuid: String): ApiEnvelope<PlaybackQueueResponse>

    @DELETE("api/queue")
    suspend fun clearQueueEnvelope(): ApiEnvelope<Unit?>

    @PUT("api/queue/reorder")
    suspend fun reorderQueueEnvelope(@Body body: QueueReorderBody): ApiEnvelope<PlaybackQueueResponse>
}
