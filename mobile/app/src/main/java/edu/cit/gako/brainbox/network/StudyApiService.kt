package edu.cit.gako.brainbox.network

import edu.cit.gako.brainbox.network.models.ApiEnvelope
import edu.cit.gako.brainbox.network.models.FlashcardAttemptRequest
import edu.cit.gako.brainbox.network.models.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.FlashcardDeckSummary
import edu.cit.gako.brainbox.network.models.QuizCreateRequest
import edu.cit.gako.brainbox.network.models.QuizAttemptRequest
import edu.cit.gako.brainbox.network.models.QuizDetail
import edu.cit.gako.brainbox.network.models.QuizSummary
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudyApiService {
    @GET("api/quizzes")
    suspend fun getQuizzesEnvelope(): ApiEnvelope<List<QuizSummary>>

    @POST("api/quizzes")
    suspend fun createQuizEnvelope(
        @Body request: QuizCreateRequest
    ): ApiEnvelope<QuizDetail>

    @GET("api/quizzes/{uuid}")
    suspend fun getQuizEnvelope(@Path("uuid") uuid: String): ApiEnvelope<QuizDetail>

    @POST("api/quizzes/{uuid}/attempts")
    suspend fun recordQuizAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: QuizAttemptRequest
    ): ApiEnvelope<QuizDetail>

    @GET("api/flashcards")
    suspend fun getFlashcardsEnvelope(): ApiEnvelope<List<FlashcardDeckSummary>>

    @POST("api/flashcards")
    suspend fun createFlashcardEnvelope(
        @Body request: FlashcardDeckCreateRequest
    ): ApiEnvelope<FlashcardDeckDetail>

    @GET("api/flashcards/{uuid}")
    suspend fun getFlashcardEnvelope(@Path("uuid") uuid: String): ApiEnvelope<FlashcardDeckDetail>

    @POST("api/flashcards/{uuid}/attempts")
    suspend fun recordFlashcardAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: FlashcardAttemptRequest
    ): ApiEnvelope<FlashcardDeckDetail>
}

