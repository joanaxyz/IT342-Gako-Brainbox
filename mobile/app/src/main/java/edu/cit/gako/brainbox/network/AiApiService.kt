package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.network.models.AiConfigListResponse
import edu.cit.gako.brainbox.network.models.AiConfigRequest
import edu.cit.gako.brainbox.network.models.AiConfigResponse
import edu.cit.gako.brainbox.network.models.AiQueryRequest
import edu.cit.gako.brainbox.network.models.AiQueryResponse
import edu.cit.gako.brainbox.network.models.ApiEnvelope
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AiApiService {
    @POST("api/ai/query")
    suspend fun queryAiEnvelope(
        @Body request: AiQueryRequest
    ): ApiEnvelope<AiQueryResponse>

    @GET("api/ai/config")
    suspend fun getAiConfigEnvelope(): ApiEnvelope<AiConfigResponse>

    @GET("api/ai/config/list")
    suspend fun listAiConfigsEnvelope(): ApiEnvelope<AiConfigListResponse>

    @PUT("api/ai/config")
    suspend fun saveAiConfigEnvelope(
        @Body request: AiConfigRequest
    ): ApiEnvelope<AiConfigResponse>

    @PUT("api/ai/config/{configId}/select")
    suspend fun selectAiConfigEnvelope(
        @Path("configId") configId: Long
    ): ApiEnvelope<AiConfigResponse>

    @DELETE("api/ai/config/{configId}")
    suspend fun deleteAiConfigEnvelope(
        @Path("configId") configId: Long
    ): ApiEnvelope<Any?>
}
