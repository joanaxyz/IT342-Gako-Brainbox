package edu.cit.gako.brainbox.data

import edu.cit.gako.brainbox.network.ApiService
import edu.cit.gako.brainbox.network.models.FlashcardAttemptRequest
import edu.cit.gako.brainbox.network.models.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.network.models.FlashcardDeckDetail
import edu.cit.gako.brainbox.network.models.QuizCreateRequest
import edu.cit.gako.brainbox.network.models.QuizAttemptRequest
import edu.cit.gako.brainbox.network.models.QuizDetail
import java.util.UUID

internal class BrainBoxStudyRepository(
    private val apiService: ApiService
) {
    suspend fun createQuiz(request: QuizCreateRequest): QuizDetail {
        return apiService.createQuizEnvelope(request)
            .requireData("We couldn't create that quiz yet.")
    }

    suspend fun getQuiz(uuid: String): QuizDetail {
        return apiService.getQuizEnvelope(uuid)
            .requireData("We couldn't load that quiz.")
    }

    suspend fun recordQuizAttempt(uuid: String, score: Int): QuizDetail {
        return apiService.recordQuizAttemptEnvelope(
            uuid,
            QuizAttemptRequest(score = score, clientMutationId = UUID.randomUUID().toString())
        )
            .requireData("We couldn't save your quiz score.")
    }

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail {
        return apiService.getFlashcardEnvelope(uuid)
            .requireData("We couldn't load that deck.")
    }

    suspend fun createFlashcardDeck(request: FlashcardDeckCreateRequest): FlashcardDeckDetail {
        return apiService.createFlashcardEnvelope(request)
            .requireData("We couldn't create that flashcard deck yet.")
    }

    suspend fun recordFlashcardAttempt(uuid: String, mastery: Int): FlashcardDeckDetail {
        return apiService.recordFlashcardAttemptEnvelope(
            uuid,
            FlashcardAttemptRequest(mastery = mastery, clientMutationId = UUID.randomUUID().toString())
        )
            .requireData("We couldn't save your flashcard progress.")
    }
}

