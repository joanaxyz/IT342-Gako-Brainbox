package com.it342.brainbox.network

import com.it342.brainbox.network.models.ApiEnvelope
import com.it342.brainbox.network.models.FlashcardAttemptRequest
import com.it342.brainbox.network.models.FlashcardDeckDetail
import com.it342.brainbox.network.models.FlashcardDeckSummary
import com.it342.brainbox.network.models.QuizAttemptRequest
import com.it342.brainbox.network.models.QuizDetail
import com.it342.brainbox.network.models.QuizSummary
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudyApiService {
    @GET("api/quizzes")
    suspend fun getQuizzesEnvelope(): ApiEnvelope<List<QuizSummary>>

    @GET("api/quizzes/{uuid}")
    suspend fun getQuizEnvelope(@Path("uuid") uuid: String): ApiEnvelope<QuizDetail>

    @POST("api/quizzes/{uuid}/attempts")
    suspend fun recordQuizAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: QuizAttemptRequest
    ): ApiEnvelope<QuizDetail>

    @GET("api/flashcards")
    suspend fun getFlashcardsEnvelope(): ApiEnvelope<List<FlashcardDeckSummary>>

    @GET("api/flashcards/{uuid}")
    suspend fun getFlashcardEnvelope(@Path("uuid") uuid: String): ApiEnvelope<FlashcardDeckDetail>

    @POST("api/flashcards/{uuid}/attempts")
    suspend fun recordFlashcardAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: FlashcardAttemptRequest
    ): ApiEnvelope<FlashcardDeckDetail>
}
