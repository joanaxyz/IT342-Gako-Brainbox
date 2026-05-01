package edu.cit.gako.brainbox.features.home.flashcards.data

import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardAttemptRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckCreateRequest
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckDetail
import edu.cit.gako.brainbox.features.home.flashcards.data.dto.FlashcardDeckSummary
import edu.cit.gako.brainbox.platform.network.requireData
import java.util.UUID

class FlashcardRepository(
    private val apiService: FlashcardApiService
) {
    suspend fun getFlashcards(): List<FlashcardDeckSummary> {
        return apiService.getFlashcardsEnvelope()
            .requireData("We couldn't load flashcards.")
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
