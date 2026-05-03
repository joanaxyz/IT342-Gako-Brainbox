package edu.cit.gako.brainbox.features.home.quizzes.data

import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizAttemptRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface QuizApiService {
    @GET("api/quizzes")
    suspend fun getQuizzesEnvelope(): ApiEnvelope<List<QuizSummary>>

    @POST("api/quizzes")
    suspend fun createQuizEnvelope(
        @Body request: QuizCreateRequest
    ): ApiEnvelope<QuizDetail>

    @PUT("api/quizzes/{uuid}")
    suspend fun updateQuizEnvelope(
        @Path("uuid") uuid: String,
        @Body request: QuizCreateRequest
    ): ApiEnvelope<QuizDetail>

    @DELETE("api/quizzes/{uuid}")
    suspend fun deleteQuizEnvelope(@Path("uuid") uuid: String): ApiEnvelope<Unit>

    @GET("api/quizzes/{uuid}")
    suspend fun getQuizEnvelope(@Path("uuid") uuid: String): ApiEnvelope<QuizDetail>

    @POST("api/quizzes/{uuid}/attempts")
    suspend fun recordQuizAttemptEnvelope(
        @Path("uuid") uuid: String,
        @Body request: QuizAttemptRequest
    ): ApiEnvelope<QuizDetail>
}
