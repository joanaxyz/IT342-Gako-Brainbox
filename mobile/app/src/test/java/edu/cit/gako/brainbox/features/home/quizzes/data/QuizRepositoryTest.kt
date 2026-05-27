package edu.cit.gako.brainbox.features.home.quizzes.data

import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizQuestion
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizAttemptRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.platform.network.ApiEnvelope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class QuizRepositoryTest {
    @Test
    fun getQuizzesSanitizesMalformedRowsWithoutFailingFetch() = runBlocking {
        val repository = QuizRepository(
            StubQuizApiService(
                summaries = listOf(
                    NetworkQuizSummary(
                        uuid = "quiz-1",
                        title = null,
                        questionCount = -4,
                        attempts = -2
                    ),
                    NetworkQuizSummary(
                        uuid = "",
                        title = "Missing id"
                    )
                )
            )
        )

        val quizzes = repository.getQuizzes()

        assertEquals(1, quizzes.size)
        assertEquals("quiz-1", quizzes.first().uuid)
        assertEquals("Untitled quiz", quizzes.first().title)
        assertEquals(0, quizzes.first().questionCount)
        assertEquals(0L, quizzes.first().attempts)
    }

    @Test
    fun getQuizUsesRouteUuidAndFiltersInvalidQuestions() = runBlocking {
        val repository = QuizRepository(
            StubQuizApiService(
                detail = NetworkQuizDetail(
                    uuid = null,
                    title = null,
                    questions = listOf(
                        NetworkQuizQuestion(
                            type = "multiple-choice",
                            text = null,
                            options = listOf(" A ", null, ""),
                            correctIndex = 99
                        ),
                        NetworkQuizQuestion(
                            type = "multiple-choice",
                            text = "No options",
                            options = null,
                            correctIndex = 0
                        )
                    )
                )
            )
        )

        val quiz = repository.getQuiz("quiz-1")

        assertEquals("quiz-1", quiz.uuid)
        assertEquals("Untitled quiz", quiz.title)
        assertEquals(1, quiz.questionCount)
        assertEquals(1, quiz.questions.size)
        assertEquals("Untitled question", quiz.questions.first().text)
        assertEquals(listOf("A"), quiz.questions.first().options)
        assertEquals(0, quiz.questions.first().correctIndex)
    }

    private class StubQuizApiService(
        private val summaries: List<NetworkQuizSummary> = emptyList(),
        private val detail: NetworkQuizDetail = NetworkQuizDetail(uuid = "quiz-1", title = "Quiz")
    ) : QuizApiService {
        override suspend fun getQuizzesEnvelope(): ApiEnvelope<List<NetworkQuizSummary>> =
            ApiEnvelope(success = true, data = summaries)

        override suspend fun createQuizEnvelope(request: QuizCreateRequest): ApiEnvelope<NetworkQuizDetail> =
            ApiEnvelope(success = true, data = detail)

        override suspend fun updateQuizEnvelope(
            uuid: String,
            request: QuizCreateRequest
        ): ApiEnvelope<NetworkQuizDetail> = ApiEnvelope(success = true, data = detail)

        override suspend fun deleteQuizEnvelope(uuid: String): ApiEnvelope<Unit> =
            ApiEnvelope(success = true)

        override suspend fun getQuizEnvelope(uuid: String): ApiEnvelope<NetworkQuizDetail> =
            ApiEnvelope(success = true, data = detail)

        override suspend fun recordQuizAttemptEnvelope(
            uuid: String,
            request: QuizAttemptRequest
        ): ApiEnvelope<NetworkQuizDetail> = ApiEnvelope(success = true, data = detail)
    }
}
