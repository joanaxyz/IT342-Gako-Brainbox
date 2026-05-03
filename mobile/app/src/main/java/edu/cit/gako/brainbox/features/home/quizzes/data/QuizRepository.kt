package edu.cit.gako.brainbox.features.home.quizzes.data

import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizAttemptRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import java.util.UUID

class QuizRepository(
    private val apiService: QuizApiService
) {
    suspend fun getQuizzes(): List<QuizSummary> {
        return apiService.getQuizzesEnvelope()
            .requireData("We couldn't load quizzes.")
    }

    suspend fun createQuiz(request: QuizCreateRequest): QuizDetail {
        return apiService.createQuizEnvelope(request)
            .requireData("We couldn't create that quiz yet.")
    }

    suspend fun updateQuiz(uuid: String, request: QuizCreateRequest): QuizDetail {
        return apiService.updateQuizEnvelope(uuid, request)
            .requireData("We couldn't save that quiz yet.")
    }

    suspend fun deleteQuiz(uuid: String) {
        apiService.deleteQuizEnvelope(uuid)
            .requireSuccess("We couldn't delete that quiz yet.")
    }

    suspend fun getQuiz(uuid: String): QuizDetail {
        return apiService.getQuizEnvelope(uuid)
            .requireData("We couldn't load that quiz.")
    }

    suspend fun recordQuizAttempt(
        uuid: String,
        score: Int,
        clientMutationId: String = UUID.randomUUID().toString()
    ): QuizDetail {
        return apiService.recordQuizAttemptEnvelope(
            uuid,
            QuizAttemptRequest(score = score, clientMutationId = clientMutationId)
        )
            .requireData("We couldn't save your quiz score.")
    }
}
