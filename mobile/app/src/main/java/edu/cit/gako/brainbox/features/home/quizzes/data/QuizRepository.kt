package edu.cit.gako.brainbox.features.home.quizzes.data

import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizAttemptRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizCreateRequest
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizQuestion
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.QuizSummary
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizDetail
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizQuestion
import edu.cit.gako.brainbox.features.home.quizzes.data.dto.NetworkQuizSummary
import edu.cit.gako.brainbox.platform.network.requireData
import edu.cit.gako.brainbox.platform.network.requireSuccess
import java.util.UUID

class QuizRepository(
    private val apiService: QuizApiService
) {
    suspend fun getQuizzes(): List<QuizSummary> {
        return apiService.getQuizzesEnvelope()
            .requireData("We couldn't load quizzes.")
            .mapNotNull { it.toSummary() }
    }

    suspend fun createQuiz(request: QuizCreateRequest): QuizDetail {
        return apiService.createQuizEnvelope(request)
            .requireData("We couldn't create that quiz yet.")
            .toDetail(fallbackUuid = null)
            ?: throw IllegalStateException("We couldn't create that quiz yet.")
    }

    suspend fun updateQuiz(uuid: String, request: QuizCreateRequest): QuizDetail {
        return apiService.updateQuizEnvelope(uuid, request)
            .requireData("We couldn't save that quiz yet.")
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't save that quiz yet.")
    }

    suspend fun deleteQuiz(uuid: String) {
        apiService.deleteQuizEnvelope(uuid)
            .requireSuccess("We couldn't delete that quiz yet.")
    }

    suspend fun getQuiz(uuid: String): QuizDetail {
        return apiService.getQuizEnvelope(uuid)
            .requireData("We couldn't load that quiz.")
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't load that quiz.")
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
            .toDetail(fallbackUuid = uuid)
            ?: throw IllegalStateException("We couldn't save your quiz score.")
    }
}

private const val UNTITLED_QUIZ = "Untitled quiz"
private const val UNTITLED_QUESTION = "Untitled question"
private const val TRUE_FALSE_TYPE = "true-false"

private fun NetworkQuizSummary.toSummary(): QuizSummary? {
    val safeUuid = uuid.safeRequired() ?: return null
    val validQuestions = questions.orEmpty().mapNotNull { it?.toQuestion() }

    return QuizSummary(
        uuid = safeUuid,
        title = title.safeTitle(),
        description = description.safeOptional(),
        difficulty = difficulty.safeOptional(),
        notebookUuid = notebookUuid.safeOptional(),
        notebookTitle = notebookTitle.safeOptional(),
        questionCount = (questionCount ?: validQuestions.size).coerceAtLeast(0),
        estimatedTime = estimatedTime.safeOptional(),
        bestScore = bestScore,
        attempts = (attempts ?: 0L).coerceAtLeast(0L),
        createdAt = createdAt.safeOptional(),
        updatedAt = updatedAt.safeOptional()
    )
}

private fun NetworkQuizDetail.toDetail(fallbackUuid: String?): QuizDetail? {
    val safeUuid = uuid.safeRequired() ?: fallbackUuid.safeRequired() ?: return null
    val validQuestions = questions.orEmpty().mapNotNull { it?.toQuestion() }

    return QuizDetail(
        uuid = safeUuid,
        title = title.safeTitle(),
        description = description.safeOptional(),
        difficulty = difficulty.safeOptional(),
        notebookUuid = notebookUuid.safeOptional(),
        notebookTitle = notebookTitle.safeOptional(),
        questionCount = validQuestions.size,
        estimatedTime = estimatedTime.safeOptional(),
        bestScore = bestScore,
        attempts = (attempts ?: 0L).coerceAtLeast(0L),
        questions = validQuestions,
        createdAt = createdAt.safeOptional(),
        updatedAt = updatedAt.safeOptional()
    )
}

private fun NetworkQuizQuestion.toQuestion(): QuizQuestion? {
    val safeType = if (type == TRUE_FALSE_TYPE) TRUE_FALSE_TYPE else "multiple-choice"
    val safeOptions = if (safeType == TRUE_FALSE_TYPE) {
        listOf("True", "False")
    } else {
        options.orEmpty()
            .mapNotNull { it.safeOptional() }
    }

    if (safeOptions.isEmpty()) {
        return null
    }

    return QuizQuestion(
        type = safeType,
        text = text.safeTitle(fallback = UNTITLED_QUESTION),
        options = safeOptions,
        correctIndex = (correctIndex ?: 0).coerceIn(0, safeOptions.lastIndex)
    )
}

private fun String?.safeTitle(fallback: String = UNTITLED_QUIZ): String =
    this?.trim()?.takeIf { it.isNotBlank() } ?: fallback

private fun String?.safeOptional(): String? =
    this?.trim()?.takeIf { it.isNotBlank() }

private fun String?.safeRequired(): String? = safeOptional()
