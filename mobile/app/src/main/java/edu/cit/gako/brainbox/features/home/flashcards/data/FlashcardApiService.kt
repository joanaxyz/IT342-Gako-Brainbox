package edu.cit.gako.brainbox.features.home.flashcards.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface FlashcardApiService {
    @GET("api/flashcards")
    suspend fun getFlashcardsEnvelope(): ApiEnvelope<List<FlashcardDeckSummary>>

    @POST("api/flashcards")
    suspend fun createFlashcardEnvelope(
        @Body request: FlashcardDeckCreateRequest
    ): ApiEnvelope<FlashcardDeckDetail>

    @PUT("api/flashcards/{uuid}")
    suspend fun updateFlashcardEnvelope(
        @Path("uuid") uuid: String,
        @Body request: FlashcardDeckCreateRequest
    ): ApiEnvelope<FlashcardDeckDetail>

    @DELETE("api/flashcards/{uuid}")
    suspend fun deleteFlashcardEnvelope(@Path("uuid") uuid: String): ApiEnvelope<Unit>

    @GET("api/flashcards/{uuid}")
    suspend fun getFlashcardEnvelope(@Path("uuid") uuid: String): ApiEnvelope<FlashcardDeckDetail>

    @POST("api/flashcards/{uuid}/attempts")
    suspend fun recordFlashcardAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: FlashcardAttemptRequest
    ): ApiEnvelope<FlashcardDeckDetail>
}
