package com.it342.brainbox.data

import com.it342.brainbox.network.ApiService
import com.it342.brainbox.network.models.FlashcardAttemptRequest
import com.it342.brainbox.network.models.FlashcardDeckDetail
import com.it342.brainbox.network.models.QuizAttemptRequest
import com.it342.brainbox.network.models.QuizDetail

internal class BrainBoxStudyRepository(
    private val apiService: ApiService
) {
    suspend fun getQuiz(uuid: String): QuizDetail {
        return apiService.getQuizEnvelope(uuid)
            .requireData("We couldn't load that quiz.")
    }

    suspend fun recordQuizAttempt(uuid: String, score: Int): QuizDetail {
        return apiService.recordQuizAttemptEnvelope(uuid, QuizAttemptRequest(score))
            .requireData("We couldn't save your quiz score.")
    }

    suspend fun getFlashcardDeck(uuid: String): FlashcardDeckDetail {
        return apiService.getFlashcardEnvelope(uuid)
            .requireData("We couldn't load that deck.")
    }

    suspend fun recordFlashcardAttempt(uuid: String, mastery: Int): FlashcardDeckDetail {
        return apiService.recordFlashcardAttemptEnvelope(uuid, FlashcardAttemptRequest(mastery))
            .requireData("We couldn't save your flashcard progress.")
    }
}
