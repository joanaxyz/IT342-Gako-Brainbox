package edu.cit.gako.brainbox.app

import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardApiService
import edu.cit.gako.brainbox.features.home.flashcards.data.FlashcardRepository
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizApiService
import edu.cit.gako.brainbox.features.home.quizzes.data.QuizRepository
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizAttemptRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class StudyAttemptClientMutationIdTest {
    @Test
    fun quizAttemptUsesCallerMutationId() = runBlocking {
        var capturedRequest: QuizAttemptRequest? = null
        val repository = QuizRepository(
            object : QuizApiService {
                override suspend fun getQuizzesEnvelope(): ApiEnvelope<List<QuizSummary>> = unused()
                override suspend fun createQuizEnvelope(request: QuizCreateRequest): ApiEnvelope<QuizDetail> =
                    unused()

                override suspend fun updateQuizEnvelope(
                    uuid: String,
                    request: QuizCreateRequest
                ): ApiEnvelope<QuizDetail> = unused()

                override suspend fun deleteQuizEnvelope(uuid: String): ApiEnvelope<Unit> = unused()

                override suspend fun getQuizEnvelope(uuid: String): ApiEnvelope<QuizDetail> = unused()

                override suspend fun recordQuizAttemptEnvelope(
                    uuid: String,
                    request: QuizAttemptRequest
                ): ApiEnvelope<QuizDetail> {
                    capturedRequest = request
                    return ApiEnvelope(
                        success = true,
                        data = QuizDetail(uuid = uuid, title = "Quiz")
                    )
                }
            }
        )

        repository.recordQuizAttempt(
            uuid = "quiz-1",
            score = 86,
            clientMutationId = "attempt-mutation-1"
        )

        assertEquals(86, capturedRequest?.score)
        assertEquals("attempt-mutation-1", capturedRequest?.clientMutationId)
    }

    @Test
    fun flashcardAttemptUsesCallerMutationId() = runBlocking {
        var capturedRequest: FlashcardAttemptRequest? = null
        val repository = FlashcardRepository(
            object : FlashcardApiService {
                override suspend fun getFlashcardsEnvelope(): ApiEnvelope<List<FlashcardDeckSummary>> =
                    unused()

                override suspend fun getFlashcardEnvelope(uuid: String): ApiEnvelope<FlashcardDeckDetail> =
                    unused()

                override suspend fun createFlashcardEnvelope(
                    request: FlashcardDeckCreateRequest
                ): ApiEnvelope<FlashcardDeckDetail> = unused()

                override suspend fun updateFlashcardEnvelope(
                    uuid: String,
                    request: FlashcardDeckCreateRequest
                ): ApiEnvelope<FlashcardDeckDetail> = unused()

                override suspend fun deleteFlashcardEnvelope(uuid: String): ApiEnvelope<Unit> = unused()

                override suspend fun recordFlashcardAttemptEnvelope(
                    uuid: String,
                    request: FlashcardAttemptRequest
                ): ApiEnvelope<FlashcardDeckDetail> {
                    capturedRequest = request
                    return ApiEnvelope(
                        success = true,
                        data = FlashcardDeckDetail(uuid = uuid, title = "Deck")
                    )
                }
            }
        )

        repository.recordFlashcardAttempt(
            uuid = "deck-1",
            mastery = 72,
            clientMutationId = "attempt-mutation-2"
        )

        assertEquals(72, capturedRequest?.mastery)
        assertEquals("attempt-mutation-2", capturedRequest?.clientMutationId)
    }

    private fun <T> unused(): T = error("Not used by this test.")
}
